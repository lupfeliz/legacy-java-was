/**
 * @File        : HttpUtil.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2023-09-04 최초 작성
 * @Description : http 통신모듈
 * @Site        : https://gitlab.ntiple.com/developers
 **/
package com.ntiple.commons.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.servlet.ReadListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtil {

  // public static final String PTN_SCHEM_HTTP = "^http[s]{0,1}[:][/][/]";

  // public int defaultConnectionTimeout = 1000;

  // public static final Pattern PTN_PARAM = Pattern.compile("^([^=]+)[=](.*)$");

  // @PostConstruct
  // public void init() {
  //   log.trace("INIT:{}", HttpUtil.class);
  // }

  // @PreDestroy
  // public void destroy() {
  //   log.trace("DESTROY:{}", HttpUtil.class);
  // }

  // /**
  //  * URL 중 scheme 부분을 제외한 URI 부분의 double slash 를 삭제한다.
  //  * 예 :
  //  * http://localhost:8080/api///main =>
  //  * http://localhost:8080/api/main
  //  * ※ http* 이외의 scheme 은 무시
  //  */
  // public static String cleanURL(String url) {
  //   final String SCHEME_HTTP = cat(S_HTTP, "://");
  //   final String SCHEME_HTTPS = cat(S_HTTPS, "://");
  //   if (url == null) { return url; }
  //   if (url.startsWith(SCHEME_HTTP)) {
  //     url = url.substring(SCHEME_HTTP.length());
  //     url = url.replaceAll("[/]+", "/");
  //     url = SCHEME_HTTP + url;
  //   } else if (url.startsWith(SCHEME_HTTPS)) {
  //     url = url.substring(SCHEME_HTTPS.length());
  //     url = url.replaceAll("[/]+", "/");
  //     url = SCHEME_HTTPS + url;
  //   } else {
  //     url = url.replaceAll("[/]+", "/");
  //   }
  //   return url;
  // }
  
  // public static HttpClient httpClient() throws Exception {
  //   final SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (X509CertChain, authType) -> true).build();
  //   HttpClient client = HttpClientBuilder.create().setSSLContext(sslContext)
  //     .setRedirectStrategy(new LaxRedirectStrategy())
  //     .setDefaultCookieStore(new BasicCookieStore())
  //     .setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
  //       @Override public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
  //         HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
  //         while (it.hasNext()) {
  //           HeaderElement he = it.nextElement();
  //           String param = he.getName();
  //           String value = he.getValue();
  //           if (value != null && param.equalsIgnoreCase(TIMEOUT)) {
  //             return Long.parseLong(value) * 1000;
  //           }
  //         }
  //         return 5 * 1000;
  //       }
  //     })
  //   .setConnectionManager(new PoolingHttpClientConnectionManager(
  //     RegistryBuilder.<ConnectionSocketFactory>create()
  //       .register(S_HTTP, PlainConnectionSocketFactory.INSTANCE)
  //       .register(S_HTTPS, new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE)).build()
  //   )).build();
  //   return client;
  // }

  // public static HttpHost httpHost(String addr) throws Exception {
  //   HttpHost ret = null;
  //   try {
  //     URL url = new URL(addr);
  //     String host = url.getHost();
  //     String prot = url.getProtocol();
  //     Integer port = url.getPort();
  //     if (port == null || port == -1) {
  //       switch (prot) {
  //       case S_HTTPS: port = 443; break;
  //       default: port = 80; break;
  //       }
  //     }
  //     ret = new HttpHost(host, port, prot);
  //   } catch (MalformedURLException e) {
  //     log.debug("ERROR:{}", e);
  //   }
  //   return ret;
  // }

  // public static UrlEncodedFormEntity nameValueEntity(String[][] arg, String enc) throws Exception {
  //   UrlEncodedFormEntity entity = null;
  //   List<NameValuePair> list = new LinkedList<>();
  //   for (String[] kv : arg) {
  //     if (kv.length >= 2 && kv[0] != null && kv[1] != null) {
  //       list.add(new BasicNameValuePair(kv[0], kv[1]));
  //     }
  //   }
  //   entity = new UrlEncodedFormEntity(list, enc);
  //   return entity;
  // }

  // public static UrlEncodedFormEntity nameValueEntity(Map<String, Object> map, String enc) throws Exception {
  //   UrlEncodedFormEntity entity = null;
  //   List<NameValuePair> list = new LinkedList<>();
  //   for (String key : map.keySet()) {
  //     list.add(new BasicNameValuePair(key, String.valueOf(map.get(key))));
  //   }
  //   entity = new UrlEncodedFormEntity(list, enc);
  //   return entity;
  // }

  // public static HttpEntity stringEntity(Object param, String type, String enc) throws Exception {
  //   StringEntity entity = new StringEntity(String.valueOf(param), enc);
  //   entity.setContentType(cat(type, ";charset=", enc));
  //   return entity;
  // }

  // public static String urlParamString(String[][] arg, String enc) throws Exception {
  //   StringBuilder ret = new StringBuilder();
  //   for (String[] kv : arg) {
  //     if (kv.length >= 2 && kv[0] != null && kv[1] != null) {
  //       if (ret.length() == 0) {
  //         ret.append("?");
  //       } else {
  //         ret.append("&");
  //       }
  //       ret.append(URLEncoder.encode(kv[0], enc))
  //         .append("=").append(URLEncoder.encode(kv[1], enc));
  //     }
  //   }
  //   return String.valueOf(ret);
  // }

  // public static void copyHeaders(HttpServletRequest requestf, HttpRequestBase requestt) throws Exception {
  //   Enumeration<String> names = cast(requestf.getHeaderNames(), names = null);
  //   while (names.hasMoreElements()) {
  //     String name = names.nextElement();
  //     String value = requestf.getHeader(name);
  //     requestt.addHeader(name, value);
  //   }
  // }

  // public static Header[] headers(HttpServletRequest request) throws Exception {
  //   List<Header> ret = new LinkedList<>();
  //   Enumeration<String> names = cast(request.getHeaderNames(), names = null);
  //   while (names.hasMoreElements()) {
  //     String name = names.nextElement();
  //     String value = request.getHeader(name);
  //     ret.add(new BasicHeader(name, value));
  //   }
  //   log.debug("HEADERS:{}", ret);
  //   return ret.toArray(new Header[ret.size()]);
  // }

  // public static Header[] headers(String[][] arg) throws Exception {
  //   List<Header> ret = new LinkedList<>();
  //   for (String[] item : arg) {
  //     if (item != null && item.length >= 2) {
  //       ret.add(new BasicHeader(item[0], item[1]));
  //     }
  //   }
  //   log.debug("HEADERS:{}", ret);
  //   return ret.toArray(new Header[ret.size()]);
  // }

  // public static Object httpContentReader(HttpResponse response, String enc) throws Exception {
  //   HttpEntity entity = response.getEntity();
  //   return reader(entity.getContent(), enc);
  // }

  // public static String httpContentStr(HttpResponse response) throws Exception {
  //   return httpContentStr(response, UTF8);
  // }

  // public static String httpContentStr(HttpResponse response, String enc) throws Exception {
  //   StringBuilder ret = new StringBuilder();
  //   Object reader = null;
  //   try {
  //     reader = httpContentReader(response, enc);
  //     for (String rl; (rl = readLine(reader)) != null;) {
  //       ret.append(rl).append("\n");
  //     }
  //   } finally {
  //     safeclose(reader);
  //   }
  //   return String.valueOf(ret);
  // }

  // public static String httpContentStr(HttpServletRequest request) throws Exception {
  //   return httpContentStr(request, UTF8);
  // }

  // public static String httpContentStr(HttpServletRequest request, String enc) throws Exception {
  //   StringBuilder ret = new StringBuilder();
  //   Object reader = null;
  //   try {
  //     reader = reader(istream(request), enc);
  //     for (String rl; (rl = readLine(reader)) != null;) {
  //       ret.append(rl).append("\n");
  //     }
  //   } finally {
  //     safeclose(reader);
  //   }
  //   return ret.substring(0, ret.length() - 1);
  // }

  // public static Map<String, Object> param(HttpServletRequest request) {
  //   Map<String, Object> ret = new LinkedHashMap<String, Object>();
  //   Enumeration<String> keys = cast(request.getAttributeNames(), keys = null);
  //   while (keys.hasMoreElements()) {
  //     String key = keys.nextElement();
  //     Object val = request.getAttribute(key);
  //     ret.put(key, val);
  //   }
  //   return ret;
  // }

  // public static Map<String, Object> param(HttpSession session) {
  //   Map<String, Object> ret = new LinkedHashMap<String, Object>();
  //   Enumeration<String> keys = cast(session.getAttributeNames(), keys = null);
  //   while (keys.hasMoreElements()) {
  //     String key = keys.nextElement();
  //     Object val = session.getAttribute(key);
  //     ret.put(key, val);
  //   }
  //   return ret;
  // }

  // public static Map<String, Object> param(ServletContext context) {
  //   Map<String, Object> ret = new LinkedHashMap<String, Object>();
  //   Enumeration<String> keys = cast(context.getAttributeNames(), keys = null);
  //   while (keys.hasMoreElements()) {
  //     String key = keys.nextElement();
  //     Object val = context.getAttribute(key);
  //     ret.put(key, val);
  //   }
  //   return ret;
  // }

  // public static Map<String, Object> param(String str, String enc) {
  //   Map<String, Object> ret = new LinkedHashMap<String, Object>();
  //   str = str.trim();
  //   str = str.replaceAll("^[?]", "");
  //   String[] split = str.split("[&]");
  //   Matcher mat = null;
  //   for (String kvstr : split) {
  //     mat = PTN_PARAM.matcher(kvstr);
  //     if (mat.find() && mat.groupCount() == 2) {
  //       String key = mat.group(1);
  //       String val = mat.group(2);
  //       try {
  //         val = URLDecoder.decode(val, enc);
  //       } catch (Exception ignore) { log.trace("E:{}", ignore); }
  //       ret.put(key, val);
  //     }
  //   }
  //   return ret;
  // }

  // public static String getAuthToken(HttpServletRequest req) {
  //   String hval = req.getHeader(AUTHORIZATION);
  //   log.trace("AUTH-HEADER:{}", hval);
  //   if (hval != null && hval.startsWith(BEARER) && hval.length() > BEARER.length() + 2) {
  //     return hval.substring(BEARER.length() + 1);
  //   }
  //   return null;
  // }

  // public static String getAuthToken() {
  //   return getAuthToken(curRequest());
  // }

  // public static HttpServletResponse curResponse() { return curResponse(curRequest()); }
  // public static HttpServletResponse curResponse(HttpServletRequest request) {
  //   if (request != null) {
  //     try {
  //       Object obj = request.getAttribute(HttpServletResponse.class.getName());
  //       if (obj != null) {
  //         if (obj instanceof HttpServletResponse) {
  //           return cast(obj, HttpServletResponse.class);
  //         }
  //       }
  //     } catch (Exception ignore) { log.trace("E:{}", ignore); }
  //   }
  //   return null;
  // }

  // public static String remoteAddr() { return remoteAddr(curRequest()); }
  // public static String remoteAddr(HttpServletRequest req) {
  //   String ret = null;
  //   ret = req.getHeader(X_FORWARDED_FOR);
  //   if (ret == null) { ret = req.getHeader("Proxy-Client-IP"); }
  //   if (ret == null) { ret = req.getHeader("WL-Proxy-Client-IP"); }
  //   if (ret == null) { ret = req.getHeader("HTTP_CLIENT_IP"); }
  //   if (ret == null) { ret = req.getHeader("HTTP_X_FORWARDED_FOR"); }
  //   if (ret == null) { ret = req.getRemoteAddr(); }
  //   return ret;
  // }

  // public static String referer() { return referer(curRequest()); }
  // public static String referer(HttpServletRequest req) {
  //   String ret = null;
  //   ret = req.getHeader(REFERER);
  //   return ret;
  // }

  // public static String getUri(String urlStr, List<String> hostNames) {
  //   String ret = "";
  //   if (urlStr == null || "".equals(urlStr)) { return ret; }
  //   urlStr = urlStr.trim().replaceAll(PTN_SCHEM_HTTP, "").trim();
  //   if (hostNames != null) {
  //     LOOP:
  //     for (String hostName : hostNames) {
  //       if (urlStr.startsWith(hostName)) {
  //         ret = urlStr.substring(hostName.length());
  //         break LOOP;
  //       }
  //     }
  //   }
  //   return ret;
  // }
  
  
  // public static Map<String, String> queryToMap(String query){
  //   Map<String, String> result = new HashMap<String, String>();
    
  //   for(String param : query.split("&")) {
  //     String pair[] = param.split("=");
      
  //     if(pair.length > 1) {
  //       result.put(pair[0], pair[1]);
  //     } else {
  //       result.put(pair[0], "");
  //     }
  //   }
    
  //   return result;
  // }
  
  // public static Map<String, String> queryStringToMap(String query){
  //   Map<String, String> result = new HashMap<String, String>();
    
  //   for(String param : query.split("&")) {
  //     String pair[] = param.split("=");
      
  //     if(pair.length > 1) {
  //       String key = param.substring(0, param.indexOf("="));
  //       String val = param.substring(param.indexOf("=")+1, param.length());
        
  //       result.put(key, val);
  //     } else {
  //       result.put(pair[0], "");
  //     }
  //   }
  //   return result;
  // }
  
  // public static class XSSInputStream extends ServletInputStream {
  //   private InputStream delegator;
  //   private boolean finished = false;
  //   public XSSInputStream(InputStream delegator) { this.delegator = delegator; }
  //   public XSSInputStream(String str, String enc) {
  //     try {
  //       delegator = new ByteArrayInputStream(str.getBytes(enc));
  //     } catch (Exception e) {
  //       log.debug("ERROR:{}", e);
  //     }
  //   }
  //   @Override public int read() throws IOException {
  //     int ret = this.delegator.read();
  //     if (!finished && ret == -1) { this.finished = true; }
  //     return ret;
  //   }
  //   @Override public int read(byte[] b) throws IOException {
  //     int ret = delegator.read(b);
  //     if (!finished && ret == -1) { this.finished = true; }
  //     return ret;
  //   }
  //   @Override public int read(byte[] b, int off, int len) throws IOException {
  //     int ret = delegator.read(b, off, len);
  //     if (!finished && ret == -1) { this.finished = true; }
  //     return ret;
  //   }
  //   @Override public int hashCode() { return delegator.hashCode(); }
  //   @Override public boolean equals(Object obj) { return delegator.equals(obj); }
  //   @Override public long skip(long n) throws IOException { return delegator.skip(n); }
  //   @Override public String toString() { return delegator.toString(); }
  //   @Override public int available() throws IOException { return delegator.available(); }
  //   @Override public void close() throws IOException { delegator.close(); }
  //   @Override public void mark(int readlimit) { delegator.mark(readlimit); }
  //   @Override public void reset() throws IOException { delegator.reset(); }
  //   @Override public boolean markSupported() { return delegator.markSupported(); }
  //   @Override public boolean isFinished() { return finished; }
  //   @Override public boolean isReady() { return true; }
  //   @Override public void setReadListener(ReadListener listener) {
  //     log.debug("================================================================================");
  //     log.debug("UNSUPPORTED OPERATION setReadListener");
  //     log.debug("================================================================================");
  //   }
  // }
  
  
  // public static class XSSFilteredRequest extends HttpServletRequestWrapper {
  //   public XSSFilteredRequest(HttpServletRequest delegate) { super(delegate); }
  //   @Override public ServletInputStream getInputStream() throws IOException {
  //     InputStream istream = null;
  //     ByteArrayOutputStream bstream = null;
  //     XSSInputStream xstream = null;
  //     String str = "";
  //     try {
  //       istream = super.getInputStream();
  //       bstream = new ByteArrayOutputStream();
  //       passthrough(istream, bstream);
  //       str = new String(bstream.toByteArray());
  //       str = cleanXSS(str);
  //       log.debug("XSS-FILTERED:{}", str);
  //       xstream = new XSSInputStream(str, UTF8);
  //     } catch (Exception e) {
  //       log.debug("ERROR:{}", e);
  //     } finally {
  //       safeclose(istream);
  //       safeclose(bstream);
  //     }
  //     return xstream;
  //   }
  //   public void test() throws Exception {
  //     this.getReader();
  //   }
  //   public static String cleanXSS(String value) {
  //     String ret = value;
  //     log.trace("VALUE:{}", value);
  //     try {
  //       ret = ret.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
  //       ret = ret.replaceAll("'", "&#39;");
  //       ret = ret.replaceAll("eval\\((.*)\\)", "");
  //       ret = ret.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
  //       ret = ret.replaceAll("<script", "&lt;script");
  //       ret = ret.replaceAll("</script", "&lt;/script");
  //       ret = ret.replaceAll("<([^>]+)on[a-zA-Z]+[=]", "<$1");
  //     } catch (Exception e) {
  //       log.debug("E:{}", e);
  //     }
  //     return ret;
  //   }
    
  // }
}