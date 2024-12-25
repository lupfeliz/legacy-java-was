/**
 * @File        : SessionListener.java
 * @Author      : 정재백
 * @Since       : 2024-12-17
 * @Description : SessionListener
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration
public class SessionListenerConfig {

  @PostConstruct public void init() {
  }

  @Bean
  HttpSessionListener httpSessionListener() {
    return new HttpSessionListener() {
      @Override
      public void sessionCreated(HttpSessionEvent se) {
        log.trace("--------------------------------------------------------------------------------");
        log.trace("SESSION-CREATED:{} / {} / {}", this, se, se.getSession());
        HttpSessionListener.super.sessionCreated(se);
      }

      @Override
      public void sessionDestroyed(HttpSessionEvent se) {
        log.trace("--------------------------------------------------------------------------------");
        log.trace("SESSION-DESTROYED:{} / {}", se.getSource(), se.getSession());
        HttpSessionListener.super.sessionDestroyed(se);
      }
    };
  }

  @Bean
  HttpSessionAttributeListener httpSessionAttributeListener() {
    return new HttpSessionAttributeListener() {
      @Override
      public void attributeAdded(HttpSessionBindingEvent se) {
        log.trace("--------------------------------------------------------------------------------");
        log.trace("SESSION-VALUE-ADDED:");
        HttpSessionAttributeListener.super.attributeAdded(se);
      }
      @Override
      public void attributeRemoved(HttpSessionBindingEvent se) {
        log.trace("--------------------------------------------------------------------------------");
        log.trace("SESSION-VALUE-REMOVED:");
        HttpSessionAttributeListener.super.attributeRemoved(se);
      }
      @Override
      public void attributeReplaced(HttpSessionBindingEvent se) {
        log.trace("--------------------------------------------------------------------------------");
        log.trace("SESSION-VALUE-REPLACED:");
        HttpSessionAttributeListener.super.attributeReplaced(se);
      }
    };
  }
}
