/**
 * @File        : SecurityConfig.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : Spring-Security 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration @EnableWebSecurity
public class SpringSecurityConfig implements SecurityFilterChain {

  @Override public boolean matches(HttpServletRequest request) {
    boolean ret = true;
    return ret;
  }

  @Override public List<Filter> getFilters() {
    List<Filter> ret = new ArrayList<>();
    return ret;
  }
}