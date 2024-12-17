/**
 * @File        : SessionListener.java
 * @Author      : 정재백
 * @Since       : 2024-12-17
 * @Description : SessionListener
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j @WebListener
public class SessionListener implements HttpSessionListener, HttpSessionAttributeListener  {

  @Override
  public void sessionCreated(HttpSessionEvent se) {
    log.debug("SESSION-CREATED:{} / {} / {}", this, se, se.getSession());
    HttpSessionListener.super.sessionCreated(se);
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
    log.debug("SESSION-DESTROYED:{} / {}", se.getSource(), se.getSession());
    HttpSessionListener.super.sessionDestroyed(se);
  }

  @Override
  public void attributeAdded(HttpSessionBindingEvent se) {
    HttpSessionAttributeListener.super.attributeAdded(se);
  }

  @Override
  public void attributeRemoved(HttpSessionBindingEvent se) {
    HttpSessionAttributeListener.super.attributeRemoved(se);
  }

  @Override
  public void attributeReplaced(HttpSessionBindingEvent se) {
    HttpSessionAttributeListener.super.attributeReplaced(se);
  }
}
