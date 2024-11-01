/**
 * @File        : ScssFilter.java
 * @Author      : 정재백
 * @Since       : 2024-04-16 
 * @Description : 공통적으로 사용할 웹필터
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.cast;
import static com.ntiple.commons.ConvertUtil.cat;
import static com.ntiple.commons.IOUtils.file;
import static com.ntiple.commons.IOUtils.istream;
import static com.ntiple.commons.IOUtils.mkdirs;
import static com.ntiple.commons.IOUtils.ostream;
import static com.ntiple.commons.IOUtils.readAsString;
import static com.ntiple.commons.IOUtils.safeclose;

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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Component
public class AssetsModifyFilter implements Filter {

  @Autowired private Settings settings;

  private static final Pattern PTN_SCSS = Pattern.compile(
    cat("(" ,
    "^(.*[.]scss)([?].+){0,1}$|",
    "^.*(/assets/scripts/[^.]+[.]js)([?].+){0,1}$",
    ")")
    );
  private static final long CACHE_INTERVAL = 1000 * 60 * 60;

  @Override public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest req = cast(sreq, HttpServletRequest.class);
    HttpServletResponse res = cast(sres, HttpServletResponse.class);
    boolean processed = false;
    String uri = req.getRequestURI();
    String cpath = req.getContextPath();
    if (cpath.length () > 0 && uri.startsWith(cpath) && !uri.equals(cpath)) { uri = cat(uri.substring(req.getContextPath().length())); }
    // log.debug("URI:{} / {}", uri, cpath);
    Matcher mat = PTN_SCSS.matcher(uri);
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
          // File cache = file(Application.class.getClassLoader().getResource("").getFile(), "log", uri);
          File cbase = rfile.getParentFile();
          if (!"".equals(settings.getScssCacheDir())) { cbase = file(settings.getScssCacheDir()); }
          File cache = file(cbase, cat(rfile.getName(), ".cache"));
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
            ostream.write(content.getBytes());
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
          res.setContentLength(content.getBytes().length);
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
    if (!processed) {
      chain.doFilter(sreq, sres);
    }
  }
  @Override public void destroy() { }
  @Override public void init(FilterConfig arg0) throws ServletException { }
}
