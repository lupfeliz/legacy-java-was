/**
 * @File        : SessionListener.java
 * @Author      : 정재백
 * @Since       : 2024-12-17
 * @Description : SessionListener
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.ReflectionUtil.cast;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ntiple.commons.ObjectStore;
import com.ntiple.config.JDBCSessionConfig.CustomSessionWrapper;
import com.ntiple.system.Settings;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration
public class SessionListenerConfig {
  public static final String LOGIN_ID_KEY = "$$LOGIN_ID_KEY$$";
  private static final ObjectStore<SessionListenerConfig> instance = new ObjectStore<>();

  @Autowired private Settings settings;

  @Autowired @Qualifier(PersistentConfig.DATASOURCE_DSS)
  private DataSource dsr;

  @PostConstruct public void init() {
    instance.set(this);
  }

  @Bean HttpSessionListener httpSessionListener() {
    return new HttpSessionListener() {
      @Override public void sessionCreated(HttpSessionEvent se) {
        log.debug("SESSION-CREATED:{} / {} / {}", this, se, se.getSession());
        HttpSessionListener.super.sessionCreated(se);
      }

      @Override public void sessionDestroyed(HttpSessionEvent se) {
        log.debug("SESSION-DESTROYED:{} / {}", se.getSource(), se.getSession());
        HttpSessionListener.super.sessionDestroyed(se);
      }
    };
  }

  @Bean HttpSessionAttributeListener httpSessionAttributeListener() {
    return new HttpSessionAttributeListener() {
      @Override public void attributeAdded(HttpSessionBindingEvent se) {
        log.debug("SESSION-VALUE-ADDED:{} = {}", se.getName(), se.getValue());
        if (LOGIN_ID_KEY.equals(se.getName())) { checkLogin(se.getName(), se.getValue(), se.getSession(), settings); }
        HttpSessionAttributeListener.super.attributeAdded(se);
      }
      @Override public void attributeRemoved(HttpSessionBindingEvent se) {
        log.debug("SESSION-VALUE-REMOVED:{} = {}", se.getName(), se.getValue());
        HttpSessionAttributeListener.super.attributeRemoved(se);
      }
      @Override public void attributeReplaced(HttpSessionBindingEvent se) {
        log.debug("SESSION-VALUE-REPLACED:{}", se.getName(), se.getValue());
        if (LOGIN_ID_KEY.equals(se.getName())) { checkLogin(se.getName(), se.getValue(), se.getSession(), settings); }
        HttpSessionAttributeListener.super.attributeReplaced(se);
      }
    };
  }

  private static void checkLogin(String name, Object value, HttpSession s, Settings st) {
    String loginId = null;
    if (value == null) { return; }
    if (value instanceof Map) {
      Map<String, Object> map = cast(value, map = null);
      loginId = cast(map.get("loginId"), "");
    } else if (value instanceof String) {
      loginId = cast(value, "");
    }

    CustomSessionWrapper cs = null;
    try {
      if (s instanceof CustomSessionWrapper) {
        cs = cast(s, cs);
        String dupId = cs.findLoginId(loginId);
        if (dupId != null) {
          /** invalidate another session */
          cs.deleteSessionAttributeAll(dupId);
          cs.deleteSessionById(dupId);
        }
        /** update this session */
        cs.updateLoginId(loginId);
      }
    } catch (Exception e) {
      log.debug("E:{}", e);
    }
  }
}
