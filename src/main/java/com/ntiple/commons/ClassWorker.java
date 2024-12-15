/**
 * @File        : ClassWorker.java
 * @Author      : 정재백
 * @Since       : 2024-12-05
 * @Description : class 탐색
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.IOUtil.safeclose;
import static com.ntiple.commons.StringUtil.cat;
import static com.ntiple.commons.StringUtil.strreplace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import com.ntiple.commons.FunctionUtil.Fn1av;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClassWorker {

  private static final Pattern PTN_WIN32FILEURL = Pattern.compile("^\\/[A-Z][:]\\/");

  private static final String getResourcePath(ClassLoader loader, String path) {
    String ret = "";
    if (path == "") {
      LOOP: for (int inx = 0; inx < 2; inx++) {
        try {
          switch (inx) { case 0: { path = "."; } break; case 1: { path = "/"; } break; }
          ret = strreplace(String.valueOf(loader.getResource(path).getFile()), "\\", "/").replaceAll("^file:/", "/");
          break LOOP;
        } catch (Exception e) { log.trace("", e); }
      }
    } else {
      ret = strreplace(String.valueOf(loader.getResource(path).getFile()), "\\", "/").replaceAll("^file:/", "/");
    }
    if (PTN_WIN32FILEURL.matcher(ret).find()) { ret = ret.substring(1); }
    return ret;
  }

  static class ClassFileFilter implements FileFilter {
    private List<String> files;
    private String prefix;
    public ClassFileFilter(List<String> files, String prefix) {
      this.files = files;
      this.prefix = prefix;
    }

    @Override public boolean accept(File file) {
      if (file != null && file.exists()) {
        if (file.isDirectory()) {
          file.listFiles(this);
        } else {
          String name = strreplace(file.getAbsolutePath(), "\\", "/");
          LOOP: for (int inx = 0; inx < 2; inx++) {
            String fprefix = prefix;
            if (inx == 0 && fprefix.endsWith("/test/")) {
              fprefix = cat(fprefix.substring(0, fprefix.length() - "/test/".length()), "/main/");
            }
            log.trace("FILE:{} / {} / {}", file, fprefix, inx);
            if (name.startsWith(fprefix) && name.endsWith(".class")) {
              name = strreplace(name.substring(fprefix.length()).replaceAll("[.]class$", ""), "/", ".");
              log.debug("CLASS:{}", name);
              files.add(name);
              break LOOP;
            }
          }
        }
      }
      return false;
    }
  }

  public static List<String> findClasses(String bpath, String fpath) {
    List<String> ret = new ArrayList<>();
    String jarext = ".jar";
    try {
      File file = new File(fpath);
      if (file.exists()) {
        log.debug("FILE:{} / {}", bpath, fpath);
        file.listFiles(new ClassFileFilter(ret, bpath));
      } else if (
        fpath.indexOf(cat((jarext = ".jar"), "!/")) != -1 ||
        fpath.indexOf(cat((jarext = ".war"), "!/")) != -1) {
        int st = -1;
        String ext = jarext;
        String prefix = "WEB-INF/classes";
        String suffix = ".class";
        if ((st = fpath.indexOf(cat(ext, "!"))) != -1) {
          String jpath= cat(fpath.substring(0, st), ext);
          String ipath = fpath.substring(st + ext.length() + 1)
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
                ret.add(ename);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.debug("CANNOT FIND CLASSES IN {}", fpath);
    }
    return ret;
  }
  
  public static final void work(ClassLoader loader, Fn1av<Class<?>> callback, String... pkg) {
    BufferedReader reader = null;
    try {
      String bpath = getResourcePath(loader, "");
      for (int inx = 0; inx < pkg.length; inx++) {
        List<String> list = findClasses(bpath, getResourcePath(loader, strreplace(pkg[inx], ".", "/")));
        log.trace("FIND-CLASSES:{}", list);
        for (String path : list) {
          log.trace("CLASS:{}", path);
          try {
            Class<?> cls = Class.forName(path);
            callback.apply(cls);
          } catch (Exception e) { log.trace("", e); }
        }
      }
    } catch (Exception e) {
      log.debug("CANNOT ACCESS PACKAGE:{}{} / {}", "", pkg, e.getMessage());
    } finally {
      safeclose(reader);
    }
  }
}
