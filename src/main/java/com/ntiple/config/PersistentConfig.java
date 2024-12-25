/**
 * @File        : PersistentConfig.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 데이터베이스 스프링부트 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.ReflectionUtil.findConstructor;
import static com.ntiple.commons.StringUtil.cat;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.Alias;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import com.ntiple.Application;
import com.ntiple.commons.ClassWorker;
import com.ntiple.system.Settings;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration
public class PersistentConfig {

  public static final String DATASOURCE_MAIN = "data-source-main";
  public static final String SQLFACTORY_MAIN = "sql-factory-main";
  public static final String SQLTRANSCT_MAIN = "sql-transaction-main";
  public static final String SQLTEMPLTE_MAIN = "sql-template-main";

  public static final String DATASOURCE_DSS = "data-source-dss";

  @Autowired Settings settings;

  @PostConstruct
  public void init() {
  }

  private static final void applyTypeProcess(SqlSessionFactoryBean fb, String... pkgs) {
    final List<Class<?>> alsLst = new ArrayList<>();
    final List<TypeHandler<?>> hndLst = new ArrayList<>();
    ClassWorker.workClasses(Application.class.getClassLoader(), cls -> {
      Annotation[] ans = cls.getAnnotations();
      for (Annotation an : ans) {
        Class<? extends Annotation> type = an.annotationType();
        if (type == Alias.class) {
          log.debug("FOUND TYPE ALIAS:{} / {}", cls, an);
          alsLst.add(cls);
        }
      }
      if (TypeHandler.class.isAssignableFrom(cls)) {
        log.debug("FOUND TYPE HANDLER:{}", cls);
        hndLst.add(cast(findConstructor(cls).newInstance(), TypeHandler.class));
      }
    }, pkgs);
    if (alsLst.size() > 0) {
      Class<?>[] array = new Class[alsLst.size()];
      fb.setTypeAliases(alsLst.toArray(array));
    }
    if (hndLst.size() > 0) {
      TypeHandler<?>[] array = new TypeHandler[hndLst.size()];
      fb.setTypeHandlers(hndLst.toArray(array));
    }
  }

  @Bean @Primary @Qualifier(DATASOURCE_MAIN)
  @ConfigurationProperties(prefix = "spring.datasource-main")
  DataSource datasourceMain() {
    DataSource ret = null;
    String jndi = cast(settings.getProperty("spring.datasource-main.jndi-name"), "");
    if (jndi != null && !"".equals(jndi)) {
      log.debug("USING JNDI:{}", jndi);
      JndiDataSourceLookup lookup = new JndiDataSourceLookup();
      ret = lookup.getDataSource(cat("java:/comp/env/jdbc/", jndi));
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
    fb.setConfigLocation(settings.getAppctx().getResource("classpath:mybatis-config.xml"));
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resource = resolver.getResources("mapper/**/*.xml");
    fb.setMapperLocations(resource);
    applyTypeProcess(fb, "com.ntiple.work", "com.ntiple.system");
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

  @Bean @Qualifier(DATASOURCE_DSS)
  @ConfigurationProperties(prefix = "spring.datasource-dss")
  DataSource datasourceDss() {
    DataSource ret = null;
    String drv = cast(settings.getProperty("spring.datasource-dss.driver-class-name"), "");
    if (drv == null || "".equals(drv) || "org.h2.Driver".equals(drv)) {
      ret = new EmbeddedDatabaseBuilder()
        .setType(org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2)
        .addScript("org/springframework/session/jdbc/schema-h2.sql").build();
    } else if (drv == null || "".equals(drv) || "org.postgresql.Driver".equals(drv)) {
      ret = DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .build();
    } else {
      ret = DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .build();
    }
    return ret;
  }
}