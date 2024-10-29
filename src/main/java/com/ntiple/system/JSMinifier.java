package com.ntiple.system;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.IOUtils.file;
import static com.ntiple.commons.IOUtils.istream;
import static com.ntiple.commons.IOUtils.reader;
import static com.ntiple.commons.IOUtils.safeclose;

import java.io.File;
import java.io.Reader;
import java.net.URL;

import javax.annotation.PostConstruct;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.stereotype.Service;

import com.ntiple.Application;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class JSMinifier {

  private static JSMinifier instance;
  private static final String[] paths = {
    "/build/classes/java/main/",
    "/build/resources/main/",
  };

  public static JSMinifier getInstance() {
    if (instance == null) {
      instance = new JSMinifier();
      instance.init();
    }
    return instance;
  }

  private Invocable ivc;

  @PostConstruct public void init() {
    if (instance == null) {
      instance = this.create();
    } else {
      this.ivc = instance.ivc;
    }
  }

  public JSMinifier create() {
    File file = null;
    Reader reader = null;
    try {
      if (ivc == null) {
        ScriptEngine se = new ScriptEngineManager().getEngineByName("nashorn");
        URL baseuri = Application.class.getClassLoader().getResource("");
        if (baseuri != null) {
          String base = baseuri.getFile();
          if (base.endsWith(paths[0])) {
            base = base.substring(0, base.length() - paths[0].length());
            file = file(base, paths[1], "/scripts/uglify.min.js");
            reader = reader(istream(file), UTF8);
            se.eval(reader);
            safeclose(reader);
            file = file(base, paths[1], "/scripts/do-minify.js");
            reader = reader(istream(file), UTF8);
            se.eval(reader);
            safeclose(reader);
          }
        }
        ivc = (Invocable) se;
      }
    } catch (Exception e) {
      log.debug("E:", e);
    } finally {
      safeclose(reader);
    }
    return this;
  }

  public String minify(String content) {
    String ret = content;
    try {
      Object c = ivc.invokeFunction("minifyCode", content);
      if (c != null) { ret = String.valueOf(c); }
    } catch (Exception e) {
      log.debug("E:", e);
      ret = content;
    }
    return ret;
  }
}
