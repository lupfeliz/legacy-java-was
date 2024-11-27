/**
 * @File        : ScssFilter.java
 * @Author      : 정재백
 * @Since       : 2024-04-16 
 * @Description : 공통적으로 사용할 웹필터
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.IOUtils.file;
import static com.ntiple.commons.IOUtils.istream;
import static com.ntiple.commons.IOUtils.mkdirs;
import static com.ntiple.commons.IOUtils.ostream;
import static com.ntiple.commons.IOUtils.passthrough;
import static com.ntiple.commons.IOUtils.readAsString;
import static com.ntiple.commons.IOUtils.safeclose;
import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.StringUtil.cat;
import static com.ntiple.commons.WebUtil.cleanXSS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Component
public class CommonFilter implements Filter {

  @Autowired private Settings settings;

  private static final Pattern PTN_RSRC_WORK = Pattern.compile(
    cat("(" ,
    "^(.*[.]scss)([?].+){0,1}$|",
    "^.*(/assets/scripts/[^.]+[.]js)([?].+){0,1}$",
    ")")
    );
  private static final Pattern PTN_RESOURCE = Pattern.compile("[.](js|css|scss|woff|svg)$");
  private static final long CACHE_INTERVAL = 1000 * 60 * 60;

  private File cachepath = null;

  @Override public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest req = cast(sreq, HttpServletRequest.class);
    HttpServletResponse res = cast(sres, HttpServletResponse.class);
    boolean processed = false;
    String uri = req.getRequestURI();
    String cbase = req.getContextPath();
    if (cbase.length () > 0 && uri.startsWith(cbase) && !uri.equals(cbase)) { uri = cat(uri.substring(req.getContextPath().length())); }
    // log.debug("URI:{} / {}", uri, cbase);
    req.setAttribute("cbase", cbase);
    req.setAttribute("uri", uri);
    Matcher mat = PTN_RSRC_WORK.matcher(uri);
    if (mat.find()) {
      long curtime = System.currentTimeMillis();
      InputStream istream = null;
      OutputStream ostream = null;
      FileWriter fw = null;
      try {
        URL rsc = req.getServletContext().getResource(uri);
        // log.debug("RSC:{}", rsc);
        File rfile = null;
        if (rsc != null && (rfile = file(rsc.getFile())) != null && rfile.exists()) {
          String content = "";
          if (cachepath == null) {
            if (!"".equals(settings.getCacheDir())) {
              cachepath = file(settings.getCacheDir());
            } else {
              cachepath = file(System.getProperty("java.io.tmpdir"));
            }
            if (!cachepath.exists()) { mkdirs(cachepath); }
          }
          File cache = file(cachepath, cat(rfile.getName(), ".cache"));
          log.debug("PATH:{}, {}, {} [{}/{}]", uri, rfile, cache, (curtime - cache.lastModified()), CACHE_INTERVAL);
          if (!cache.exists() ||
            (curtime - cache.lastModified()) > CACHE_INTERVAL ||
            rfile.lastModified() > cache.lastModified()) {
            log.debug("READ-USING-CONVERTER");
            if (rfile.getName().endsWith(".js")) {
              content = JSMinifier.getInstance().work(rfile);
            } else if (rfile.getName().endsWith(".scss")) {
              content = ScssWorker.getInstance().work(rfile);
            }
            if (!cache.getParentFile().exists()) { mkdirs(cache.getParentFile()); }
            ostream = ostream(cache);
            ostream.write(content.getBytes(UTF8));
            fw = new FileWriter(rfile, true);
            fw.append("");
          } else {
            log.debug("READ-FROM-CACHE");
            content = readAsString(istream = istream(cache));
          }
          String ctype = "plain/text";
          if (rfile.getName().endsWith(".js")) {
            ctype = "text/javascript";
          } else if (rfile.getName().endsWith(".scss")) {
            ctype = "text/css";
          }
          /** 리소스 캐시 갱신주기 : 30초 TODO: 프로파일별로 다르게 설정할것 */
          res.setHeader("Cache-Control", "max-age=30");
          res.setContentLength(content.getBytes(UTF8).length);
          res.setCharacterEncoding(UTF8);
          res.setContentType(ctype);
          res.getWriter().write(content);
          processed = true;
        }
      } catch (Exception e) {
        log.debug("E:", e);
      } finally {
        safeclose(ostream);
        safeclose(istream);
        safeclose(fw);
      }
    }
    if (PTN_RESOURCE.matcher(uri).find()) {
      /** 리소스 캐시 갱신주기 : 30초 TODO: 프로파일별로 다르게 설정할것 */
      res.setHeader("Cache-Control", "max-age=30");
    }
    if (!processed) {
      chain.doFilter(sreq, sres);
    }
  }
  @Override public void destroy() { }
  @Override public void init(FilterConfig arg0) throws ServletException { }

  public static class XSSInputStream extends ServletInputStream {
    private InputStream delegator;
    private boolean finished = false;
    public XSSInputStream(InputStream delegator) { this.delegator = delegator; }
    public XSSInputStream(String str, String enc) {
      try {
        delegator = new ByteArrayInputStream(str.getBytes(enc));
      } catch (Exception e) {
        log.debug("ERROR:{}", e);
      }
    }
    @Override public int read() throws IOException {
      int ret = this.delegator.read();
      if (!finished && ret == -1) { this.finished = true; }
      return ret;
    }
    @Override public int read(byte[] b) throws IOException {
      int ret = delegator.read(b);
      if (!finished && ret == -1) { this.finished = true; }
      return ret;
    }
    @Override public int read(byte[] b, int off, int len) throws IOException {
      int ret = delegator.read(b, off, len);
      if (!finished && ret == -1) { this.finished = true; }
      return ret;
    }
    @Override public int hashCode() { return delegator.hashCode(); }
    @Override public boolean equals(Object obj) { return delegator.equals(obj); }
    @Override public long skip(long n) throws IOException { return delegator.skip(n); }
    @Override public String toString() { return delegator.toString(); }
    @Override public int available() throws IOException { return delegator.available(); }
    @Override public void close() throws IOException { delegator.close(); }
    @Override public void mark(int readlimit) { delegator.mark(readlimit); }
    @Override public void reset() throws IOException { delegator.reset(); }
    @Override public boolean markSupported() { return delegator.markSupported(); }
    @Override public boolean isFinished() { return finished; }
    @Override public boolean isReady() { return true; }
    @Override public void setReadListener(ReadListener listener) {
      log.debug("================================================================================");
      log.debug("UNSUPPORTED OPERATION setReadListener");
      log.debug("================================================================================");
    }
  }
  
  public static class XSSFilteredRequest extends HttpServletRequestWrapper {
    public XSSFilteredRequest(HttpServletRequest delegate) { super(delegate); }
    @Override public ServletInputStream getInputStream() throws IOException {
      InputStream istream = null;
      ByteArrayOutputStream bstream = null;
      XSSInputStream xstream = null;
      String str = "";
      try {
        istream = super.getInputStream();
        bstream = new ByteArrayOutputStream();
        passthrough(istream, bstream);
        str = new String(bstream.toByteArray());
        str = cleanXSS(str);
        log.debug("XSS-FILTERED:{}", str);
        xstream = new XSSInputStream(str, UTF8);
      } catch (Exception e) {
        log.debug("ERROR:{}", e);
      } finally {
        safeclose(istream);
        safeclose(bstream);
      }
      return xstream;
    }
    public void test() throws Exception {
      this.getReader();
    }
  }
}
