/**
 * @File        : JSMinifier.java
 * @Author      : 정재백
 * @Since       : 2024-10-29
 * @Description : 자바스크립트 minify
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import static com.ntiple.commons.IOUtil.readAsString;
import static com.ntiple.commons.IOUtil.safeclose;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class JSMinifier {

  private static JSMinifier instance;

  private static final Pattern PTN_NL = Pattern.compile("[\\r\\n][\\\\]", Pattern.MULTILINE);

  public static JSMinifier getInstance() {
    if (instance == null) {
      instance = new JSMinifier();
      instance.init();
    }
    return instance;
  }

  @PostConstruct public void init() {
    if (instance == null) { instance = this; }
  }

  private static boolean minify(Reader input, Writer output) throws Exception {
    net.logicsquad.minifier.js.JSMinifier min = null;
    try {
      min = new net.logicsquad.minifier.js.JSMinifier(input);
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
        input = new StringReader(content);
        output = new StringWriter();
        minify(input, output);
        ret = output.toString();
        ret = PTN_NL.matcher(ret).replaceAll("");
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
    String content = "";
    StringReader input = null;
    StringWriter output = null;
    try {
      content = readAsString(file);
      if (Settings.getInstance().isJsMinify()) {
        input = new StringReader(content);
        output = new StringWriter();
        minify(input, output);
        ret = output.toString();
        ret = PTN_NL.matcher(ret).replaceAll("");
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
}