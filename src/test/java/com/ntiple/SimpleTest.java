/**
 * @File        : SimpleTest.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 간단한 테스트 케이스들
 * @Site        : https://devlog.ntiple.com
 * 
 * sh gradlew cleanTest test -Dproject.build.test=MANUAL -Dspring.profiles.active=local -i --no-watch-fs --tests "com.ntiple.SimpleTest.testSimple"
 **/
package com.ntiple;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.IOUtils.file;
import static com.ntiple.commons.IOUtils.istream;
import static com.ntiple.commons.IOUtils.readAsString;
import static com.ntiple.commons.IOUtils.reader;
import static com.ntiple.commons.IOUtils.safeclose;
import static com.ntiple.commons.IOUtils.writer;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

// import com.moodysalem.phantomjs.wrapper.PhantomJS;
// import com.moodysalem.phantomjs.wrapper.beans.PhantomJSOptions;
import com.ntiple.TestUtil.TestLevel;

// import de.larsgrefer.sass.embedded.CompileSuccess;
// import de.larsgrefer.sass.embedded.SassCompiler;
// import de.larsgrefer.sass.embedded.SassCompilerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleTest {

  @Test public void testSimple() throws Exception {
    log.info("OK");
    assertTrue(true);
  }

  @Test public void testPattern() throws Exception {
    if (!TestUtil.isEnabled("testPattern", TestLevel.MANUAL)) { return; }
    {
      Pattern PTN_ST_SCRIPT = Pattern.compile("^[<]script[^>]*>");
      Pattern PTN_ED_SCRIPT = Pattern.compile("[<]/script[^>]*>$");
      String str = "<script> TEST </script>";
      str = str.trim();
      str = PTN_ST_SCRIPT.matcher(str).replaceAll("");
      str = PTN_ED_SCRIPT.matcher(str).replaceAll("");
      log.debug("TEST:{}", str);
    }
    {
      Pattern PTN_NL = Pattern.compile("[\\r\\n][\\\\]", Pattern.MULTILINE);
      // Pattern PTN_NL = Pattern.compile("\\\\", Pattern.MULTILINE);
      String str = "function $component_input(app){const CInput={template:`<input\r" +
        "\\ class=\"form-control\"\r" +
        "\\:vrules=\"\r" +
        "\\ data-test=\"OK\"\r" +
        "\\/>`,name:\"c-input\",props:{},data:function(){return{};},mounted:async function(){}};app.component(\"c-input\",CInput);};\r";
      // String str = readAsString(file("/home/coder/documents/tmp/c-input.js"));
      log.debug("STR:{}", str);
      log.debug("--------------------------------------------------------------------------------");
      log.debug("STR:{}", PTN_NL.matcher(str).replaceAll(""));
    }
    assertTrue(true);
  }

  @Test public void testByNashornScript() throws Exception {
    if (!TestUtil.isEnabled("testByNashornScript", TestLevel.MANUAL)) { return; }
    // final String LANG_VER = "TypeScript.LanguageVersion.EcmaScript5";
    // ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    // engine.eval("print('OK');");
    // engine.eval("var Sass = require('./sass.sync-0.11.1.min.js');");
    // engine.eval(
    //   "const Sass = require('./sass.sync-0.11.1.min.js');\r" +
    //   "Sass.compile(`div { border: 1px solid #f00; > div { background: #f00; } }`, function(r) { console.log(r.text); });"
    // );
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

  // @Test
  // public void testSassCompile() throws Exception {
  //   if (!TestUtil.isEnabled("testSassCompile", TestLevel.MANUAL)) { return; }
  //   try (SassCompiler sassCompiler = SassCompilerFactory.bundled()) {
  //     String content = "div { border: 1px solid #ccc; > div { background: #f00; } }";
  //     // CompileSuccess res = sassCompiler.compileCssString(content);
  //     // CompileSuccess res = sassCompiler.compileSassString(content);
  //     CompileSuccess res = sassCompiler.compileScssString(content);
  //     // log.debug("RESULT:\n{}", res.getCompileResponse());
  //     log.debug("RESULT:\n{}", res.getCss());
  //   } catch (Exception e) {
  //     log.debug("E:", e);
  //   }
  //   // try (SassCompiler sassCompiler = SassCompilerFactory.bundled()) {
  //   //   sassCompiler.registerImporter(new WebjarsImporter().autoCanonicalize());
  //   //   URL resource = getClass().getResource("/custom-bootstrap.scss");
  //   //   CompileSuccess compileSuccess = sassCompiler.compile(resource);
  //   //   // custom Bootstrap css
  //   //   String css = compileSuccess.getCss();
  //   // } catch (Exception e) {
  //   //   log.debug("E:", e);
  //   // }
  // }

  @Test
  public void testMinifyJS() throws Exception {
    if (!TestUtil.isEnabled("testMinifyJS", TestLevel.MANUAL)) { return; }
    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    ScriptContext context = new SimpleScriptContext();
    context.getBindings(ScriptContext.GLOBAL_SCOPE);
    context.setBindings(engine.createBindings(), ScriptContext.GLOBAL_SCOPE);
    Bindings bindings = context.getBindings(ScriptContext.GLOBAL_SCOPE); 
    // Bindings bindings = new SimpleBindings();
    bindings.put("console", System.console());
    engine.setContext(context);
    BufferedReader reader = null;
    Writer writer = null;
    File scrFile = null;
    File srcFile = null;
    try {
      Invocable invocable = (Invocable) engine;
      scrFile = TestUtil.getResource(Application.class, "/scripts/uglify.min.js");
      reader = reader(istream(scrFile), UTF8);
      engine.eval(reader);
      safeclose(reader);
      scrFile = TestUtil.getResource(Application.class, "/scripts/do-minify.js");
      reader = reader(istream(scrFile), UTF8);
      engine.eval(reader);
      safeclose(reader);
      srcFile = file("/home/coder/documents/tmp/test.js");
      String content = readAsString(srcFile);
      content = content.replaceAll("`", "｀");
      Object obj = invocable.invokeFunction("minifyCode", content);
      if (obj != null) {
        content = String.valueOf(obj).replaceAll("｀", "`");
      }
      log.debug("RESULT:{}", content);
    } finally {
      safeclose(reader);
      safeclose(writer);
    }
  }

  // @Test
  // public void testYUICompressor() throws Exception {
  //   if (!TestUtil.isEnabled("testYUICompressor", TestLevel.MANUAL)) { return; }
  //   BufferedReader reader = null;
  //   StringWriter out = new StringWriter();
  //   try {
  //     reader = reader(file("/home/coder/documents/tmp/minify-7196260321170045552.script"), UTF8);
  //     JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new ErrorReporter() {
  //       @Override public void error(String arg0, String arg1, int arg2, String arg3, int arg4) {
  //         log.debug("ERROR:");
  //       }
  //       @Override public EvaluatorException runtimeError(String arg0, String arg1, int arg2, String arg3, int arg4) {
  //         log.debug("ERROR:");
  //         return null;
  //       }
  //       @Override public void warning(String arg0, String arg1, int arg2, String arg3, int arg4) { }
  //     });
  //     int linebreakpos = -1;
  //     boolean munge = false;
  //     boolean preserveAllSemiColons = false;
  //     boolean disableOptimizations = false;
  //     boolean verbose = true;
  //     compressor.compress(out, linebreakpos, munge, verbose,
  //       preserveAllSemiColons, disableOptimizations);
  //     log.debug("OUT:{}", out.toString());
  //   } catch (Exception e) {
  //     log.debug("E:", e);
  //   } finally {
  //   }
  // }

  // @Test
  // public void testClosureCompiler() throws Exception {
  //   com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();
  //   CompilerOptions options = new CompilerOptions();
  //   WarningLevel warningLevel = WarningLevel.QUIET;
  //   options.setCodingConvention(CodingConventions.getDefault());
  //   options.setOutputCharset(UTF8);
  //   // options.setOutputCharset(Charset.forName(UTF8));
  //   warningLevel.setOptionsForWarningLevel(options);
  //   // DEFAULT_COMPILATION_LEVEL.setOptionsForCompilationLevel(options);
  //   File file = file("/home/coder/documents/tmp/minify-7196260321170045552.script");
  //   compiler.disableThreads();
  //   final List<SourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
  //   // List<SourceFile> defaultExterns = CommandLineRunner.getBuiltinExterns(null);
  //   List<SourceFile> jsSourceFiles = new ArrayList<>();
  //   jsSourceFiles.add(SourceFile.fromFile(file.getAbsolutePath(), Charset.forName("UTF-8")));
  //   Result result = compiler.compile(defaultExterns, jsSourceFiles, options);
  //   log.debug("RESULT:{}", result);
  // }

  // @Test
  // public void testBabel() throws Exception {
  //   if (!TestUtil.isEnabled("testBabel", TestLevel.MANUAL)) { return; }
  //   ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
  //   ScriptContext context = new SimpleScriptContext();
  //   context.getBindings(ScriptContext.GLOBAL_SCOPE);
  //   context.setBindings(engine.createBindings(), ScriptContext.GLOBAL_SCOPE);
  //   Bindings bindings = context.getBindings(ScriptContext.GLOBAL_SCOPE); 
  //   // Bindings bindings = new SimpleBindings();
  //   bindings.put("console", System.console());
  //   engine.setContext(context);
  //   BufferedReader reader = null;
  //   Writer writer = null;
  //   File scrFile = null;
  //   File srcFile = null;
  //   try {
  //     Invocable invocable = (Invocable) engine;
  //     scrFile = TestUtil.getResource(Application.class, "/scripts/babel-core-6.1.19.min.js");
  //     reader = reader(istream(scrFile), UTF8);
  //     engine.eval(reader);
  //     safeclose(reader);
  //     // scrFile = TestUtil.getResource(Application.class, "/scripts/do-minify.js");
  //     // reader = reader(istream(scrFile), UTF8);
  //     // engine.eval(reader);
  //     // safeclose(reader);
  //     // srcFile = file("/home/coder/documents/tmp/test.js");
  //     // String content = readAsString(srcFile);
  //     // content = content.replaceAll("`", "｀");
  //     // Object obj = invocable.invokeFunction("minifyCode", content);
  //     // if (obj != null) {
  //     //   content = String.valueOf(obj).replaceAll("｀", "`");
  //     // }
  //     // log.debug("RESULT:{}", content);
  //   } finally {
  //     safeclose(reader);
  //     safeclose(writer);
  //   }
  // }

  // @Test
  // public void testPhantomJS() throws Exception {
  //   if (!TestUtil.isEnabled("testPhantomJS", TestLevel.MANUAL)) { return; }
  //   try {
  //     String str = "console.log('OK');";
  //     InputStream script = new ByteArrayInputStream(str.getBytes());
  //     PhantomJSOptions option = PhantomJSOptions.DEFAULT.withHelp(true);
  //     PhantomJS.exec(script, option);
  //   } catch (Exception e) {
  //     log.debug("E:", e);
  //   }
  // }

  @Test
  public void testCast() throws Exception {
    if (!TestUtil.isEnabled("testCast", TestLevel.MANUAL)) { return; }
    JSONObject prm = null;
    prm = testCastImpl(prm);
  }

  public static <T> T testCastImpl(T t) throws Exception {
    T ret = t;
    log.debug("CHECK:{} / {}", t, t instanceof JSONObject);
    return ret;
  }

  @Test
  public void testMinifier() throws Exception {
    if (!TestUtil.isEnabled("testMinifier", TestLevel.MANUAL)) { return; }
    net.logicsquad.minifier.js.JSMinifier min = null;
    Reader input = null;
    Writer output = null;
    try {
      input = reader(file("/home/coder/documents/tmp/minify-7196260321170045552.js"), UTF8);
      output = writer(file("/home/coder/documents/tmp/test.js"), UTF8);
      min = new net.logicsquad.minifier.js.JSMinifier(input);
      min.minify(output);
    } finally {
      safeclose(input);
      safeclose(output);
    }
  }
}