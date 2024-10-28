package com.ntiple.commons.utils;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class WebUtil {
  
  // public static HttpServletRequest curRequest() {
  //   return (cast(RequestContextHolder.getRequestAttributes(), ServletRequestAttributes.class))
  //     .getRequest();
  // }
  public static HttpServletRequest curRequest() {
    return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes())
      .getRequest();
  }
}