/**
 * @File        : PersistentConfig.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 데이터베이스 스프링부트 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.IOUtil.safeclose;
import static com.ntiple.commons.StringUtil.cat;
import static com.ntiple.commons.StringUtil.strreplace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.Alias;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import com.ntiple.Application;
import com.ntiple.system.Settings;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration
public class PersistentConfig {

  public static final String DATASOURCE_MAIN = "data-source-main";
  public static final String SQLFACTORY_MAIN = "sql-factory-main";
  public static final String SQLTRANSCT_MAIN = "sql-transaction-main";
  public static final String SQLTEMPLTE_MAIN = "sql-template-main";

  @Autowired private ApplicationContext appctx;

  @Autowired Settings settings;

  @PostConstruct
  public void init() {
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
          if (name.startsWith(prefix) && name.endsWith(".class")) {
            name = strreplace(name.substring(prefix.length()).replaceAll("[.]class$", ""), "/", ".");
            log.trace("CLASS:{}", name);
            files.add(name);
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
        log.trace("FILE:{} / {}", bpath, fpath);
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

  private static final void applyTypeAlias(SqlSessionFactoryBean fb, String pkg) {
    BufferedReader reader = null;
    try {
      String ipkg = strreplace(pkg, ".", "/");
      ClassLoader loader = Application.class.getClassLoader();
      String bpath = getResourcePath(loader, "");
      String fpath = getResourcePath(loader, ipkg);
      List<String> list = findClasses(bpath, fpath);
      log.debug("FIND-CLASSES:{}", list);
      if (list != null && list.size() > 0) {
        List<Class<?>> clslst = new ArrayList<>();
        for (String path : list) {
          log.trace("CLASS:{}", path);
          try {
            Class<?> cls = Class.forName(path);
            Annotation[] ans = cls.getAnnotations();
            for (Annotation an : ans) {
              if (an.annotationType() == Alias.class) {
                log.debug("FOUND TYPE ALIAS:{} / {}", cls, an);
                clslst.add(cls);
              }
            }
          } catch (Exception e) { log.trace("", e); }
        }
        if (clslst.size() > 0) {
          Class<?>[] classes = new Class[clslst.size()];
          fb.setTypeAliases(clslst.toArray(classes));
        }
      }
    } catch (Exception e) {
      log.debug("CANNOT ACCESS PACKAGE:{}", pkg);
      log.debug("E:", e);
    } finally {
      safeclose(reader);
    }
  }

  @Bean @Qualifier(DATASOURCE_MAIN)
  @ConfigurationProperties(prefix = "spring.datasource-main")
  DataSource datasourceMain() {
    DataSource ret = null;
    String jndiName = settings.getJndiName();
    if (!"".equals(jndiName)) {
      log.debug("USING JNDI:{}", jndiName);
      JndiDataSourceLookup lookup = new JndiDataSourceLookup();
      ret = lookup.getDataSource(cat("java:/comp/env/jdbc/", jndiName));
    } else {
      log.debug("USING HIKARI POOL");
      ret = DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .build();
    }
    return ret;
  }

  @Bean @Qualifier(SQLFACTORY_MAIN)
  SqlSessionFactory sqlSessionFactoryMain() throws Exception {
    SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
    fb.setDataSource(datasourceMain());
    fb.setVfs(SpringBootVFS.class);
    fb.setConfigLocation(appctx.getResource("classpath:mybatis-config.xml"));
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resource = resolver.getResources("mapper/**/*.xml");
    fb.setMapperLocations(resource);
    applyTypeAlias(fb, "com.ntiple.work");
    return fb.getObject();
  }

  @Bean @Qualifier(SQLTRANSCT_MAIN)
  DataSourceTransactionManager transactionManagerMain(
    @Autowired @Qualifier(DATASOURCE_MAIN) DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean @Qualifier(SQLTEMPLTE_MAIN)
  SqlSessionTemplate sqlSessionTemplateMain(
    @Autowired @Qualifier(SQLFACTORY_MAIN) SqlSessionFactory fac) {
    return new SqlSessionTemplate(fac);
  }
}