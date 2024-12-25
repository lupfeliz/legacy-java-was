/**
 * @File        : SessionListener.java
 * @Author      : 정재백
 * @Since       : 2024-12-17
 * @Description : SessionListener
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.convert;
import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.commons.HttpUtil.httpWorker;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ntiple.system.Settings;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration
public class SessionListenerConfig {
  private static final Map<String, HttpSession> idmap = new LinkedHashMap<>();

  @Autowired private Settings settings;

  @PostConstruct public void init() {
  }

  @Bean HttpSessionListener httpSessionListener() {
    return new HttpSessionListener() {
      @Override public void sessionCreated(HttpSessionEvent se) {
        log.debug("SESSION-CREATED:{} / {} / {}", this, se, se.getSession());
        HttpSessionListener.super.sessionCreated(se);
      }

      @Override public void sessionDestroyed(HttpSessionEvent se) {
        log.debug("SESSION-DESTROYED:{} / {}", se.getSource(), se.getSession());
        HttpSession s = se.getSession();
        idmap.remove(s.getId());
        HttpSessionListener.super.sessionDestroyed(se);
      }
    };
  }

  @Bean HttpSessionAttributeListener httpSessionAttributeListener() {
    return new HttpSessionAttributeListener() {
      @Override public void attributeAdded(HttpSessionBindingEvent se) {
        log.debug("SESSION-VALUE-ADDED:{} = {}", se.getName(), se.getValue());
        if ("LOGIN-ID".equals(se.getName())) { checkLogin(se.getName(), se.getValue(), se.getSession(), settings); }
        HttpSessionAttributeListener.super.attributeAdded(se);
      }
      @Override public void attributeRemoved(HttpSessionBindingEvent se) {
        log.debug("SESSION-VALUE-REMOVED:{} = {}", se.getName(), se.getValue());
        HttpSessionAttributeListener.super.attributeRemoved(se);
      }
      @Override public void attributeReplaced(HttpSessionBindingEvent se) {
        log.debug("SESSION-VALUE-REPLACED:{}", se.getName(), se.getValue());
        if ("LOGIN-ID".equals(se.getName())) { checkLogin(se.getName(), se.getValue(), se.getSession(), settings); }
        HttpSessionAttributeListener.super.attributeReplaced(se);
      }
    };
  }

  private static void checkLogin(String name, Object value, HttpSession s, Settings st) {
    String loginId = value == null ? null : String.valueOf(value);
    if (loginId != null && idmap.containsKey(loginId)) {
      HttpSession o = idmap.get(loginId);
      if (o != null && !o.getId().equals(s.getId())) {
        log.debug("DUPLICATED-LOGIN-ID:{}", loginId);
        idmap.remove(loginId).invalidate();
      }
    } else if (loginId != null) {
      log.debug("LOGIN:{}", loginId);
      idmap.put(loginId, s);
      httpWorker()
        .url("http://localhost:3000/api/cmn/cmn01001a01")
        .method(p -> p.POST())
        .charset(UTF8)
        .contentType(p -> p.APPLICATION_JSON())
        .contents(convert(new Object[][]{
          { "loginId", loginId },
          { "ctxhash", st.getAppctx().hashCode() }
        }, newMap()))
        .agent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0")
        .work((stat, stream, hdr, ctx) -> {
          return null;
        });
    }
  }
}
