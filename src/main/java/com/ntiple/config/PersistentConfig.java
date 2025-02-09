/**
 * @File        : PersistentConfig.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 데이터베이스 스프링부트 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.ConvertUtil.array;
import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.commons.MybatisConfigUtil.getJndiDataSource;
import static com.ntiple.commons.ReflectionUtil.cast;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.ntiple.commons.FunctionUtil.Fn2at;
import com.ntiple.commons.MybatisConfigUtil;
import com.ntiple.commons.ObjectStore;
import com.ntiple.system.Settings;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration @Component @Order(1)
public class PersistentConfig {

  public static final String DATASOURCE_MAIN = "data-source-main";
  public static final String SQLFACTORY_MAIN = "sql-factory-main";
  public static final String SQLTRANSCT_MAIN = "sql-transaction-main";
  public static final String SQLTEMPLTE_MAIN = "sql-template-main";
  public static final String DATASOURCE_DSS = "data-source-dss";

  private static final ObjectStore<Map<String, Object>> props = new ObjectStore<>();
  @Autowired Settings settings;

  @PostConstruct public void init() {
    log.debug("INIT PERSISTENT-CONFIG..");
    // ApplicationContext appctx = settings.getAppctx();
    // new Thread(() -> {
    //   appctx.getBean(DATASOURCE_MAIN);
    //   appctx.getBean(DATASOURCE_DSS);
    // }).start();
  }

  @Bean(name = DATASOURCE_MAIN) @Primary
  @ConfigurationProperties(prefix = "spring.datasource-main")
  DataSource datasourceMain() throws Exception {
    DataSource ret = null;
    String jndi = cast(settings.getProperty("spring.datasource-main.jndi-name"), "");
    if (jndi != null && !"".equals(jndi)) { ret = getJndiDataSource(jndi); }
    if (ret == null) { ret = DataSourceBuilder.create().type(HikariDataSource.class).build(); }
    registerDss.apply(settings.getAppctx(), ret);
    // convert(new Object[][] {
    //   { "@test", "test" },
    //   { "!test", "test" }
    // }, props);
    return ret;
  }

  private static Fn2at<Object, DataSource, Object> registerDss = MybatisConfigUtil.configSqlSession(
    PersistentConfig.class,
    DATASOURCE_MAIN, SQLFACTORY_MAIN, SQLTEMPLTE_MAIN, SQLTRANSCT_MAIN, props.getAsync(() -> newMap()),
    "classpath:mybatis-config.xml", "mapper/**/*.xml",
    array("com.ntiple.work", "com.ntiple.system"));


  @Bean(name = DATASOURCE_DSS)
  @ConfigurationProperties(prefix = "spring.datasource-dss")
  DataSource datasourceDss() {
    DataSource ret = null;
    String jndi = cast(settings.getProperty("spring.datasource-dss.jndi-name"), "");
    if (jndi != null && !"".equals(jndi)) { ret = getJndiDataSource(jndi); }
    if (ret == null) { ret = DataSourceBuilder.create().type(HikariDataSource.class).build(); }
    return ret;
  }
}