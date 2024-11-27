/**
 * @File        : Application.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 스프링부트 어플리케이션 런처
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    String profile = System.getProperty("spring.profiles.active");
    if (profile == null || "".equals(profile)) {
      System.setProperty("spring.profiles.active", "local");
    }
    System.setProperty("file.encoding", "UTF-8");
    SpringApplication.run(Application.class, args);
  }
}