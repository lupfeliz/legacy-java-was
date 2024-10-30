/**
 * @File        : ScssWorker.java
 * @Author      : 정재백
 * @Since       : 2024-04-16 
 * @Description : Scss 파일 변환기
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class ScssWorker {
  private static ScssWorker instance;

  public static ScssWorker getInstance() {
    if (instance == null) {
      instance = new ScssWorker();
      instance.init();
    }
    return instance;
  }
  
  @PostConstruct public void init() {
    if (instance == null) {
      instance = this;
    }
  }
}
