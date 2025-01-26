// package com.ntiple;

// import java.io.File;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.io.Writer;
// import java.util.ArrayList;
// import java.util.List;

// import org.apache.commons.io.FileUtils;
// import org.junit.jupiter.api.Test;

// import com.ntiple.TestUtil.TestLevel;
// import com.vaadin.sass.internal.ScssContext;
// import com.vaadin.sass.internal.ScssStylesheet;
// import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
// import com.vaadin.sass.internal.handler.SCSSErrorHandler;
// import com.vaadin.sass.internal.resolver.FilesystemResolver;
// import com.vaadin.sass.internal.resolver.ScssStylesheetResolver;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// public class ScssCompileTest {
//   public void compileResource(File src, File tgt) {
//     List<ScssStylesheetResolver> resolvers = new ArrayList<>();
//     try {
//       resolvers = new ArrayList<ScssStylesheetResolver>();
//       ScssStylesheetResolver resolver = new FilesystemResolver(src.getParentFile().getAbsolutePath());
//       resolvers.add(resolver);
//       ScssContext.UrlMode urlMode = ScssContext.UrlMode.MIXED;
//       boolean minify = true;
//       boolean ignoreWarnings = true;
//       SCSSErrorHandler errorHandler = new SCSSErrorHandler();
//       errorHandler.setWarningsAreErrors(!ignoreWarnings);
//       ScssStylesheet scss = ScssStylesheet.get(src.getAbsolutePath(), null, new SCSSDocumentHandlerImpl(), errorHandler);
//       if (scss == null) {
//         log.error("The scss file {} could not be found.", src);
//       }
//       scss.setResolvers(resolvers);
//       scss.compile(urlMode);
//       Writer writer = new FileWriter(tgt);
//       scss.write(writer, minify);
//       writer.close();
//     } catch (Exception e) {
//       try {
//         FileUtils.forceDelete(tgt);
//       } catch (IOException e2) { log.debug("E:{}", e2); }
//       log.error("", e);
//     }
//   }

//   @Test public void test1() throws Exception {
//     if (!TestUtil.isEnabled("test1", TestLevel.MANUAL)) { return; }
//     File src = new File("/home/coder/documents/sgg-repo/sgg-sgg-online/src/main/webapp/assets/styles/globals.scss");
//     File tgt = new File("/home/coder/documents/tmp/aaa.css");
//     compileResource(src, tgt);
//   }
// }
