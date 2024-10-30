/**
 * @File        : ScssWorker.java
 * @Author      : 정재백
 * @Since       : 2024-04-16 
 * @Description : Scss 파일 변환기
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import java.io.File;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import de.larsgrefer.sass.embedded.CompileSuccess;
import de.larsgrefer.sass.embedded.SassCompiler;
import de.larsgrefer.sass.embedded.SassCompilerFactory;
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

  private SassCompiler sc = null;
  
  @PostConstruct public void init() {
    if (instance == null) {
      try {
        instance = this;
        this.sc = SassCompilerFactory.bundled();
      } catch (Exception e) {
        log.debug("E:", e);
        instance = null;
      }
    } else {
      this.sc = instance.sc;
    }
  }

  public String work(String content) {
    String ret = content;
    try {
      CompileSuccess cs = sc.compileScssString(content);
      ret = cs.getCss();
    } catch (Exception e) {
      log.debug("E:", e);
      ret = content;
    }
    return ret;
  }

  public String work(File file) {
    String ret = "";
    try {
      CompileSuccess cs = sc.compileFile(file);
      ret = cs.getCss();
    } catch (Exception e) {
      log.debug("E:", e);
    }
    return ret;
  }
}
