/**
 * @File        : WebResourceConfig.java
 * @Author      : 정재백
 * @Since       : 2024-11-07
 * @Description : 리소스 캐시설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

// @EnableWebMvc
@RequiredArgsConstructor
@Configuration
public class WebResourceConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
    CacheControl cacheControl = CacheControl
      // .noCache();
      .maxAge(30, TimeUnit.SECONDS);

    registry
      .addResourceHandler("/assets/**")
      // .addResourceLocations("classpath:/static/")
      .addResourceLocations("/assets/")
      // .setCachePeriod(30000);
      .setCacheControl(cacheControl);
  }
}
