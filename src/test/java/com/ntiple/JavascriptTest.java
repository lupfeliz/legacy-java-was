package com.ntiple;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.jupiter.api.Test;

import com.ntiple.TestUtil.TestLevel;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class JavascriptTest {

  @Test public void test1() throws Exception {
    if (!TestUtil.isEnabled("test1", TestLevel.MANUAL)) { return; }
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("graal.js");
    /** "Graal.js" 출력 */
    System.out.println("engine name: " + engine.getFactory().getEngineName());
    try {
      /** 2 출력 */
      // engine.eval("print( Math.min(2, 3) )");
      engine.eval("const a = () => print( Math.min(2, 3) ); a();");
    } catch (ScriptException e) {
      System.err.println(e);
    }
  }
}
