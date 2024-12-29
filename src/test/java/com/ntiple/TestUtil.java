/**
 * @File        : TestUtil.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 테스트 레벨링 모듈
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple;

import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.StringUtil.cat;

import java.io.File;
import java.net.URL;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.ntiple.config.PersistentConfig;
import com.ntiple.system.Settings;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestUtil {
  public static enum TestLevel {
    NONE(0),
    SIMPLE(1),
    DBCON(2),
    API(3),
    FULL(4),
    MANUAL(99);
    
    private final int value;
    TestLevel(int value) { this.value = value; }
    public int value() { return value; }
  }

  public static boolean isEnabled(String testName, TestLevel lvl) {
    boolean ret = false;
    if (lvl == null) { lvl = TestLevel.NONE; }
    String enabled = "";
    try {
      enabled = System.getProperty("project.build.test");
      if (enabled == null || "".equals(enabled)) { enabled = TestLevel.SIMPLE.name(); }
      TestLevel target = TestLevel.valueOf(enabled);
      if (target.value() >= lvl.value()) { ret = true; }
      log.info("LEVEL CHECK:{} / {}[{}] / {}[{}], {}, {}", testName,
        target, target.name(), lvl, lvl.name(), enabled, ret);
    } catch (Exception ignore) { }
    return ret;
  }

  public static File getResource(Class<?> cls, String uri) throws Exception {
    File ret = null;
    try {
      String[] paths = {
        "/build/classes/java/test/",
        "/build/classes/java/main/",
        "/build/resources/test/",
        "/build/resources/main/",
        "/build/webapps/",
        "/src/main/webapp/"
      };
      URL url = cls.getClassLoader().getResource("");
      if (url == null) { return ret; }

      String buildpath = url.getFile();
      buildpath = buildpath.replaceAll("\\\\", "/");
      if (buildpath.endsWith(paths[0])) {
        buildpath = cat(buildpath.substring(0, buildpath.length() - paths[0].length()));
      } else if (buildpath.endsWith(paths[1])) {
        buildpath = cat(buildpath.substring(0, buildpath.length() - paths[1].length()));
      }
      for (String p : paths) {
        File file = new File(cat(buildpath, "/", p, "/", uri).replaceAll("[/]+", "/"));
        log.debug("FILE:{}", file.getAbsolutePath());
        if (file.exists()) {
          ret = file;
          break;
        }
      }
      log.debug("BUILDPATH:{} / {}", buildpath, ret);
      // ret = new File(path);
    } catch (Exception e) {
      log.debug("E:", e);
    }
    return ret;
  }

  public static SqlSessionTemplate initDb() {
    SqlSessionTemplate ret = null;
    try {
      ClassLoader loader = null;
      Settings settings = Settings.getInstance();
      log.debug("TEST:{}", settings.getProperty("spring.datasource-dss.dbsession-query.create-session"));
      String driver = cast(settings.getProperty("spring.datasource-dss.driver-class-name"), "");
      String jdburl = cast(settings.getProperty("spring.datasource-dss.jdbc-url"), "");
      String jdbusr = cast(settings.getProperty("spring.datasource-dss.username"), "");
      String jdbpsw = cast(settings.getProperty("spring.datasource-dss.password"), "");
      HikariDataSource source = new HikariDataSource();
      source.setDriverClassName(driver);
      source.setJdbcUrl(jdburl);
      source.setUsername(jdbusr);
      source.setPassword(jdbpsw);
      loader = Application.class.getClassLoader();
      SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
      fb.setDataSource(source);
      fb.setConfigLocation(new FileUrlResource(loader.getResource("mybatis-config.xml")));
      PersistentConfig.applyTypeProcess(fb, "com.ntiple.work", "com.ntiple.system");
      ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource[] resource = resolver.getResources("mapper/**/*.xml");
      fb.setMapperLocations(resource);
      ret = new SqlSessionTemplate(fb.getObject());
      log.info("RESOURCES:{}{}", "", resource);
      log.info("SQLTMP:{}{}", "", ret);
    } catch (Exception e) {
      log.debug("E:", e);
    }
    return ret;
  }
}
