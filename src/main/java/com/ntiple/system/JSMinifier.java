/**
 * @File        : JSMinifier.java
 * @Author      : 정재백
 * @Since       : 2024-10-29
 * @Description : 자바스크립트 minify
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

// import static com.ntiple.commons.Constants.UTF8;
// import static com.ntiple.commons.IOUtils.file;
// import static com.ntiple.commons.IOUtils.istream;
import static com.ntiple.commons.IOUtils.readAsString;
// import static com.ntiple.commons.IOUtils.reader;
import static com.ntiple.commons.IOUtils.safeclose;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
// import java.net.URL;

import javax.annotation.PostConstruct;
// import javax.script.Invocable;
// import javax.script.ScriptEngine;
// import javax.script.ScriptEngineManager;

import org.springframework.stereotype.Service;

// import com.ntiple.Application;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class JSMinifier {

  private static JSMinifier instance;
  // private static final String[] paths = {
  //   "/build/classes/java/main/",
  //   "/build/resources/main/",
  // };

  public static JSMinifier getInstance() {
    if (instance == null) {
      instance = new JSMinifier();
      instance.init();
    }
    return instance;
  }

  // private Invocable ivc;

  @PostConstruct public void init() {
    // if (instance == null) {
    //   instance = this.create();
    // } else {
    //   this.ivc = instance.ivc;
    // }
    if (instance == null) { instance = this; }
  }

  // public JSMinifier create() {
  //   // // File file = null;
  //   // // Reader reader = null;
  //   // // try {
  //   // //   if (ivc == null) {
  //   // //     ScriptEngine se = new ScriptEngineManager().getEngineByName("nashorn");
  //   // //     URL baseuri = Application.class.getClassLoader().getResource("");
  //   // //     if (baseuri != null) {
  //   // //       String base = baseuri.getFile();
  //   // //       if (base.endsWith(paths[0])) {
  //   // //         base = base.substring(0, base.length() - paths[0].length());
  //   // //         file = file(base, paths[1], "/scripts/uglify.min.js");
  //   // //         reader = reader(istream(file), UTF8);
  //   // //         se.eval(reader);
  //   // //         safeclose(reader);
  //   // //         file = file(base, paths[1], "/scripts/do-minify.js");
  //   // //         reader = reader(istream(file), UTF8);
  //   // //         se.eval(reader);
  //   // //         safeclose(reader);
  //   // //       }
  //   // //     }
  //   // //     ivc = (Invocable) se;
  //   // //   }
  //   // // } catch (Exception e) {
  //   // //   log.debug("E:", e);
  //   // // } finally {
  //   // //   safeclose(reader);
  //   // // }
  //   // return this;
  // }

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
      // Object c = ivc.invokeFunction("minifyCode", content);
      // if (c != null) { ret = String.valueOf(c); }
      input = new StringReader(content);
      output = new StringWriter();
      minify(input, output);
      ret = output.toString();
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
      // Object c = ivc.invokeFunction("minifyCode", content);
      // if (c != null) { ret = String.valueOf(c); }
      input = new StringReader(content);
      output = new StringWriter();
      minify(input, output);
      ret = output.toString();
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