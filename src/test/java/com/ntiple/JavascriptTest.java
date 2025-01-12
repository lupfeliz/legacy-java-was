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

  // @Test public void test2() throws Exception {
  //   if (!TestUtil.isEnabled("test2", TestLevel.MANUAL)) { return; }
  //   try (Context context = Context.create("js")) {
  //     // 스크립트 파일을 읽어와서 실행시킨다.
  //     context.eval(Source.newBuilder("js",
  //         ClassLoader.getSystemResource("sample_script.js")).build());
  //     // 컨텍스트의 바인딩 객체에서 "accumulator" 함수를 가져온다.
  //     Value accumulatorFunc = context.getBindings("js").getMember("accumulator");
  //     // 함수를 파라미터 1, 2을 넘겨 실행시키고 결과는 int에 매핑시킨다.
  //     int result = accumulatorFunc.execute(1, 2).asInt();
  //     System.out.println("result: " + result);
  //   } catch (IOException e) {
  //     System.err.println(e);
  //   }
  // }
}
