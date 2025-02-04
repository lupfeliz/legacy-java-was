/**
 * @File        : PersistentConfig.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 데이터베이스 스프링부트 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.commons.MybatisConfigUtil.getJndiDataSource;
import static com.ntiple.commons.ReflectionUtil.cast;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.ntiple.commons.MybatisConfigUtil;
import com.ntiple.commons.SimpleLogger;
import com.ntiple.system.Settings;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration
public class PersistentConfig {

  public static final String DATASOURCE_MAIN = "data-source-main";
  public static final String SQLFACTORY_MAIN = "sql-factory-main";
  public static final String SQLTRANSCT_MAIN = "sql-transaction-main";
  public static final String SQLTEMPLTE_MAIN = "sql-template-main";
  public static final String DATASOURCE_DSS = "data-source-dss";

  @Autowired Settings settings;

  @PostConstruct public void init() {
    log.debug("INIT PERSISTENT-CONFIG..");
    SimpleLogger.setSrcLogger(log);
  }

  @Getter @Setter @ToString
  public static class MapperInfo {
    private String className;
    private Map<String, String> methods = new LinkedHashMap<>();
    private Map<String, String[]> params = new LinkedHashMap<>();
  }

  @Bean(name = DATASOURCE_MAIN) @Primary
  @ConfigurationProperties(prefix = "spring.datasource-main")
  DataSource datasourceMain() throws Exception {
    DataSource ret = null;
    String jndi = cast(settings.getProperty("spring.datasource-main.jndi-name"), "");
    if (jndi != null && !"".equals(jndi)) {
      log.debug("USING JNDI:{}", jndi);
      ret = getJndiDataSource(jndi);
    }
    if (ret == null) {
      log.debug("USING HIKARI POOL");
      ret = DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .build();
    }
    MybatisConfigUtil.configSqlSession(ret, this,
      settings.getAppctx(),
      DATASOURCE_MAIN,
      SQLFACTORY_MAIN,
      SQLTEMPLTE_MAIN,
      SQLTRANSCT_MAIN,
      newMap(),
      "classpath:mybatis-config.xml",
      "mapper/**/*.xml", "com.ntiple.work", "com.ntiple.system");
    return ret;
  }

  @Bean(name = DATASOURCE_DSS)
  @ConfigurationProperties(prefix = "spring.datasource-dss")
  DataSource datasourceDss() {
    DataSource ret = null;
    String jndi = cast(settings.getProperty("spring.datasource-dss.jndi-name"), "");
    if (jndi != null && !"".equals(jndi)) {
      log.debug("USING JNDI:{}", jndi);
      ret = getJndiDataSource(jndi);
    }
    if (ret == null) {
      log.debug("USING HIKARI POOL");
      ret = DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .build();
    }
    return ret;
  }
}