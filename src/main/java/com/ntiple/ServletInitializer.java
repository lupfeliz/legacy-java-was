/**
 * @File        : Application.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 스프링부트 WAS 디플로이어
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {
  @Override protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(Application.class);
  }
}