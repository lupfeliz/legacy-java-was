package com.ntiple;

import static com.ntiple.commons.StringUtil.cat;
import static com.ntiple.commons.StringUtil.strreplace;

import java.io.File;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.jupiter.api.Test;

import com.ntiple.TestUtil.TestLevel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JarClassTest {

  @Test public void testJar() throws Exception {
    if (!TestUtil.isEnabled("testJar", TestLevel.MANUAL)) { return; }
    String path ="";
    path = "C:/woorifg/workspace/wra-wra-batch/build/libs/sgg-sgg-batch.jar!/WEB-INF/classes!/com/wooribank/sgg/work";
    {
      int st = -1;
      String ext = ".jar";
      String prefix = "WEB-INF/classes";
      String suffix = ".class";
      if ((st = path.indexOf(cat(ext, "!"))) != -1) {
        String jpath= cat(path.substring(0, st), ext);
        String ipath = path.substring(st + ext.length() + 1)
          .replaceAll("^/", "")
          .replaceAll("WEB-INF[/]classes[!][/]", "/");
        JarFile jfile = new JarFile(new File(jpath));
        log.debug("PATH:{} / {} / {}", jfile, st, ipath);
        Enumeration<JarEntry> enums = jfile.entries();
        for (JarEntry entry = null; enums.hasMoreElements();) {
          entry = enums.nextElement();
          String ename = entry.getName();
          if(
            ename.startsWith(prefix) &&
            ename.endsWith(suffix)) {
            ename = ename.substring(prefix.length());
            if (ename.startsWith(ipath)) {
              ename = ename.replaceAll("^/", "");
              ename = ename.substring(0, ename.length() - suffix.length());
              ename = strreplace(ename, "/", ".");
              log.debug("ENTRY:{}", ename);
            }
          }
        }
      }
    }
  }
}
