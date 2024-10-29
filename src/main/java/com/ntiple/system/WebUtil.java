/**
 * @File        : WebUtil.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2023-09-04 최초 작성
 * @Description : 웹유틸
 * @Site        : https://gitlab.ntiple.com/developers
 **/
package com.ntiple.system;

import static com.ntiple.commons.Constants.REFERER;
import static com.ntiple.commons.Constants.S_HTTP;
import static com.ntiple.commons.Constants.S_HTTPS;
import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.Constants.X_FORWARDED_FOR;
import static com.ntiple.commons.ConvertUtil.cast;
import static com.ntiple.commons.ConvertUtil.cat;
import static com.ntiple.commons.IOUtils.passthrough;
import static com.ntiple.commons.IOUtils.safeclose;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebUtil {

  public static final String PTN_SCHEM_HTTP = "^http[s]{0,1}[:][/][/]";

  /**
   * URL 중 scheme 부분을 제외한 URI 부분의 double slash 를 삭제한다.
   * 예 :
   * http://localhost:8080/api///main =>
   * http://localhost:8080/api/main
   * ※ http* 이외의 scheme 은 무시
   */
  public static String cleanURL(String url) {
    final String SCHEME_HTTP = cat(S_HTTP, "://");
    final String SCHEME_HTTPS = cat(S_HTTPS, "://");
    if (url == null) { return url; }
    if (url.startsWith(SCHEME_HTTP)) {
      url = url.substring(SCHEME_HTTP.length());
      url = url.replaceAll("[/]+", "/");
      url = SCHEME_HTTP + url;
    } else if (url.startsWith(SCHEME_HTTPS)) {
      url = url.substring(SCHEME_HTTPS.length());
      url = url.replaceAll("[/]+", "/");
      url = SCHEME_HTTPS + url;
    } else {
      url = url.replaceAll("[/]+", "/");
    }
    return url;
  }
  
  public static HttpServletRequest curRequest() {
    return (cast(RequestContextHolder.getRequestAttributes(), ServletRequestAttributes.class))
      .getRequest();
  }

  public static HttpServletResponse curResponse() { return curResponse(curRequest()); }
  public static HttpServletResponse curResponse(HttpServletRequest request) {
    if (request != null) {
      try {
        Object obj = request.getAttribute(HttpServletResponse.class.getName());
        if (obj != null) {
          if (obj instanceof HttpServletResponse) {
            return cast(obj, HttpServletResponse.class);
          }
        }
      } catch (Exception ignore) { log.trace("E:{}", ignore); }
    }
    return null;
  }

  public static String remoteAddr() { return remoteAddr(curRequest()); }
  public static String remoteAddr(HttpServletRequest req) {
    String ret = null;
    ret = req.getHeader(X_FORWARDED_FOR);
    if (ret == null) { ret = req.getHeader("Proxy-Client-IP"); }
    if (ret == null) { ret = req.getHeader("WL-Proxy-Client-IP"); }
    if (ret == null) { ret = req.getHeader("HTTP_CLIENT_IP"); }
    if (ret == null) { ret = req.getHeader("HTTP_X_FORWARDED_FOR"); }
    if (ret == null) { ret = req.getRemoteAddr(); }
    return ret;
  }

  public static String referer() { return referer(curRequest()); }
  public static String referer(HttpServletRequest req) {
    String ret = null;
    ret = req.getHeader(REFERER);
    return ret;
  }

  public static String getUri(String urlStr, List<String> hostNames) {
    String ret = "";
    if (urlStr == null || "".equals(urlStr)) { return ret; }
    urlStr = urlStr.trim().replaceAll(PTN_SCHEM_HTTP, "").trim();
    if (hostNames != null) {
      LOOP:
      for (String hostName : hostNames) {
        if (urlStr.startsWith(hostName)) {
          ret = urlStr.substring(hostName.length());
          break LOOP;
        }
      }
    }
    return ret;
  }
  
  
  public static Map<String, String> queryToMap(String query) {
    Map<String, String> ret = new LinkedHashMap<String, String>();
    for (String param : query.split("&")) {
      String pair[] = param.split("=");
      if (pair.length > 1) {
        ret.put(pair[0], pair[1]);
      } else {
        ret.put(pair[0], "");
      }
    }
    return ret;
  }
  
  public static Map<String, String> queryStringToMap(String query) {
    Map<String, String> ret = new LinkedHashMap<String, String>();
    for (String param : query.split("&")) {
      String pair[] = param.split("=");
      if (pair.length > 1) {
        String key = param.substring(0, param.indexOf("="));
        String val = param.substring(param.indexOf("=") + 1, param.length());
        ret.put(key, val);
      } else {
        ret.put(pair[0], "");
      }
    }
    return ret;
  }
  
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
    public static String cleanXSS(String value) {
      String ret = value;
      log.trace("VALUE:{}", value);
      try {
        ret = ret.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
        ret = ret.replaceAll("'", "&#39;");
        ret = ret.replaceAll("eval\\((.*)\\)", "");
        ret = ret.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
        ret = ret.replaceAll("<script", "&lt;script");
        ret = ret.replaceAll("</script", "&lt;/script");
        ret = ret.replaceAll("<([^>]+)on[a-zA-Z]+[=]", "<$1");
      } catch (Exception e) {
        log.debug("E:{}", e);
      }
      return ret;
    }
  }
}