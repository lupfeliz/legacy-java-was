/**
 * @File        : ScssWorker.java
 * @Author      : 정재백
 * @Since       : 2024-04-16 
 * @Description : Scss 파일 변환기
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import static com.ntiple.commons.IOUtils.safeclose;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

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

  private static boolean minify(Reader input, Writer output) throws Exception {
    net.logicsquad.minifier.css.CSSMinifier min = null;
    try {
      min = new net.logicsquad.minifier.css.CSSMinifier(input);
      min.minify(output);
      return true;
    } catch (Exception e) {
      log.debug("E:", e);
    }
    return false;
  }

  public String work(String content) {
    String ret = content;
    StringReader input = null;
    StringWriter output = null;
    try {
      if (Settings.getInstance().isJsMinify()) {
        CompileSuccess cs = sc.compileScssString(content);
        input = new StringReader(cs.getCss());
        output = new StringWriter();
        minify(input, output);
        ret = output.toString();
      } else {
        ret = content;
      }
    } catch (Exception e) {
      log.debug("E:", e);
      ret = content;
    } finally {
      safeclose(output);
      safeclose(input);
    }
    return ret;
  }

  public String work(File file) {
    String ret = "";
    StringReader input = null;
    StringWriter output = null;
    try {
      CompileSuccess cs = sc.compileFile(file);
      String content = cs.getCss();
      if (Settings.getInstance().isJsMinify()) {
        input = new StringReader(content);
        output = new StringWriter();
        minify(input, output);
        ret = output.toString();
      } else {
        ret = content;
      }
    } catch (Exception e) {
      log.debug("E:", e);
    }
    return ret;
  }
}
