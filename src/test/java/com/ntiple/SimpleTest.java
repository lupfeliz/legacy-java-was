/**
 * @File        : SimpleTest.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 간단한 테스트 케이스들
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleTest {

  @Test public void testSimple() throws Exception {
    log.debug("OK");
    assertTrue(true);
  }

  @Test public void testPattern() throws Exception {
    Pattern PTN_ST_SCRIPT = Pattern.compile("^[<]script[^>]*>");
    Pattern PTN_ED_SCRIPT = Pattern.compile("[<]/script[^>]*>$");
    String str = "<script> TEST </script>";
    str = str.trim();
    str = PTN_ST_SCRIPT.matcher(str).replaceAll("");
    str = PTN_ED_SCRIPT.matcher(str).replaceAll("");
    log.debug("TEST:{}", str);
    assertTrue(true);
  }

  @Test public void testByNashornScript() throws Exception {
    final String LANG_VER = "TypeScript.LanguageVersion.EcmaScript5";
    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    engine.eval("print('Hello World!');");
    // if (launchForBuild) { return; }
    // ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
    // ScriptContext context = new SimpleScriptContext();
    // Bindings binding = engine.createBindings();
    // engine.setContext(context);
    // List<Integer> scopes = context.getScopes();
    // context.setBindings(binding, scopes.get(0));
    // Reader reader = null;
    // //ScriptContext context = null;
    // try {
    //   reader = new InputStreamReader(Application.class.getClassLoader().getResource("META-INF/" + TYPESCRIPT_COMPILER).openConnection().getInputStream());
    //   engine.eval(reader, context);
    //   reader.close();
    //   reader = new InputStreamReader(Application.class.getClassLoader().getResource("META-INF/" + COMPILER_WRAPPER).openConnection().getInputStream());
    //   engine.eval(reader, context);
    //   File targetScript = new File("src/test/resources/test.ts");
    //   binding.put("input", targetScript.getAbsolutePath());
    //   binding.put("contextName", "");
    //   binding.put("ver", LANG_VER);
    //   String script = "var result;"+
    //   "result = compilerWrapper.compile(input, ver, contextName);"+
    //   "";
    //   engine.eval(script, context);
    //   assertTrue(true);
    // } finally {
    //   if (reader != null) try { reader.close(); } catch (Exception ignore) { }
    // }
  }
}
