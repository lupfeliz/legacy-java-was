/**
 * @File        : HttpUtil.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2023-09-04 최초 작성
 * @Description : http 통신모듈
 * @Site        : https://gitlab.ntiple.com/developers
 **/
package com.ntiple.commons.utils;

import static com.ntiple.commons.Constants.S_HTTP;
import static com.ntiple.commons.Constants.S_HTTPS;
import static com.ntiple.commons.Constants.TIMEOUT;
import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.cast;
import static com.ntiple.commons.ConvertUtil.cat;
import static com.ntiple.commons.IOUtils.reader;
import static com.ntiple.commons.IOUtils.safeclose;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtil {

  public int defaultConnectionTimeout = 1000;

  public static final Pattern PTN_PARAM = Pattern.compile("^([^=]+)[=](.*)$");

  public static HttpClient httpClient() throws Exception {
    final SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (X509CertChain, authType) -> true).build();
    HttpClient client = HttpClientBuilder.create().setSSLContext(sslContext)
      .setRedirectStrategy(new LaxRedirectStrategy())
      .setDefaultCookieStore(new BasicCookieStore())
      .setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
        @Override public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
          HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
          while (it.hasNext()) {
            HeaderElement he = it.nextElement();
            String param = he.getName();
            String value = he.getValue();
            if (value != null && param.equalsIgnoreCase(TIMEOUT)) {
              return Long.parseLong(value) * 1000;
            }
          }
          return 5 * 1000;
        }
      })
    .setConnectionManager(new PoolingHttpClientConnectionManager(
      RegistryBuilder.<ConnectionSocketFactory>create()
        .register(S_HTTP, PlainConnectionSocketFactory.INSTANCE)
        .register(S_HTTPS, new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE)).build()
    )).build();
    return client;
  }

  public static HttpHost httpHost(String addr) throws Exception {
    HttpHost ret = null;
    try {
      URL url = new URL(addr);
      String host = url.getHost();
      String prot = url.getProtocol();
      Integer port = url.getPort();
      if (port == null || port == -1) {
        switch (prot) {
        case S_HTTPS: port = 443; break;
        default: port = 80; break;
        }
      }
      ret = new HttpHost(host, port, prot);
    } catch (MalformedURLException e) {
      log.debug("ERROR:{}", e);
    }
    return ret;
  }

  public static UrlEncodedFormEntity nameValueEntity(String[][] arg, String enc) throws Exception {
    UrlEncodedFormEntity entity = null;
    List<NameValuePair> list = new LinkedList<>();
    for (String[] kv : arg) {
      if (kv.length >= 2 && kv[0] != null && kv[1] != null) {
        list.add(new BasicNameValuePair(kv[0], kv[1]));
      }
    }
    entity = new UrlEncodedFormEntity(list, enc);
    return entity;
  }

  public static UrlEncodedFormEntity nameValueEntity(Map<String, Object> map, String enc) throws Exception {
    UrlEncodedFormEntity entity = null;
    List<NameValuePair> list = new LinkedList<>();
    for (String key : map.keySet()) {
      list.add(new BasicNameValuePair(key, String.valueOf(map.get(key))));
    }
    entity = new UrlEncodedFormEntity(list, enc);
    return entity;
  }

  public static HttpEntity stringEntity(Object param, String type, String enc) throws Exception {
    StringEntity entity = new StringEntity(String.valueOf(param), enc);
    entity.setContentType(cat(type, ";charset=", enc));
    return entity;
  }

  public static String urlParamString(String[][] arg, String enc) throws Exception {
    StringBuilder ret = new StringBuilder();
    for (String[] kv : arg) {
      if (kv.length >= 2 && kv[0] != null && kv[1] != null) {
        if (ret.length() == 0) {
          ret.append("?");
        } else {
          ret.append("&");
        }
        ret.append(URLEncoder.encode(kv[0], enc))
          .append("=").append(URLEncoder.encode(kv[1], enc));
      }
    }
    return String.valueOf(ret);
  }

  public static void copyHeaders(HttpServletRequest requestf, HttpRequestBase requestt) throws Exception {
    Enumeration<String> names = cast(requestf.getHeaderNames(), names = null);
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      String value = requestf.getHeader(name);
      requestt.addHeader(name, value);
    }
  }

  public static Header[] headers(HttpServletRequest request) throws Exception {
    List<Header> ret = new LinkedList<>();
    Enumeration<String> names = cast(request.getHeaderNames(), names = null);
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      String value = request.getHeader(name);
      ret.add(new BasicHeader(name, value));
    }
    log.debug("HEADERS:{}", ret);
    return ret.toArray(new Header[ret.size()]);
  }

  public static Header[] headers(String[][] arg) throws Exception {
    List<Header> ret = new LinkedList<>();
    for (String[] item : arg) {
      if (item != null && item.length >= 2) {
        ret.add(new BasicHeader(item[0], item[1]));
      }
    }
    log.debug("HEADERS:{}", ret);
    return ret.toArray(new Header[ret.size()]);
  }

  public static BufferedReader httpContentReader(HttpResponse response, String enc) throws Exception {
    HttpEntity entity = response.getEntity();
    return reader(entity.getContent(), enc);
  }

  public static String httpContentStr(HttpResponse response) throws Exception {
    return httpContentStr(response, UTF8);
  }

  public static String httpContentStr(HttpResponse response, String enc) throws Exception {
    StringBuilder ret = new StringBuilder();
    BufferedReader reader = null;
    try {
      reader = httpContentReader(response, enc);
      for (String rl; (rl = reader.readLine()) != null;) {
        ret.append(rl).append("\n");
      }
    } finally {
      safeclose(reader);
    }
    return String.valueOf(ret);
  }

  public static String httpContentStr(HttpServletRequest request) throws Exception {
    return httpContentStr(request, UTF8);
  }

  public static String httpContentStr(HttpServletRequest request, String enc) throws Exception {
    StringBuilder ret = new StringBuilder();
    BufferedReader reader = null;
    InputStream istream = null;
    try {
      istream = request.getInputStream();
      reader = reader(istream, enc);
      for (String rl; (rl = reader.readLine()) != null;) {
        ret.append(rl).append("\n");
      }
    } finally {
      safeclose(reader);
      safeclose(istream);
    }
    return ret.substring(0, ret.length() - 1);
  }

  public static Map<String, Object> param(HttpServletRequest request) {
    Map<String, Object> ret = new LinkedHashMap<String, Object>();
    Enumeration<String> keys = cast(request.getAttributeNames(), keys = null);
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      Object val = request.getAttribute(key);
      ret.put(key, val);
    }
    return ret;
  }

  public static Map<String, Object> param(HttpSession session) {
    Map<String, Object> ret = new LinkedHashMap<String, Object>();
    Enumeration<String> keys = cast(session.getAttributeNames(), keys = null);
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      Object val = session.getAttribute(key);
      ret.put(key, val);
    }
    return ret;
  }

  public static Map<String, Object> param(ServletContext context) {
    Map<String, Object> ret = new LinkedHashMap<String, Object>();
    Enumeration<String> keys = cast(context.getAttributeNames(), keys = null);
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      Object val = context.getAttribute(key);
      ret.put(key, val);
    }
    return ret;
  }

  public static Map<String, Object> param(String str, String enc) {
    Map<String, Object> ret = new LinkedHashMap<String, Object>();
    str = str.trim();
    str = str.replaceAll("^[?]", "");
    String[] split = str.split("[&]");
    Matcher mat = null;
    for (String kvstr : split) {
      mat = PTN_PARAM.matcher(kvstr);
      if (mat.find() && mat.groupCount() == 2) {
        String key = mat.group(1);
        String val = mat.group(2);
        try {
          val = URLDecoder.decode(val, enc);
        } catch (Exception ignore) { log.trace("E:{}", ignore); }
        ret.put(key, val);
      }
    }
    return ret;
  }

}