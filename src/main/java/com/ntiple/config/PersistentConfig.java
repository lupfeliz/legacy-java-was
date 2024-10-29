/**
 * @File        : PersistendConfig.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 데이터베이스 스프링부트 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.ConvertUtil.cat;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
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

import com.ntiple.commons.Settings;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration
public class PersistentConfig {

  public static final String DATASOURCE = "data-source";
  public static final String SQLFACTORY = "sql-factory";
  public static final String SQLTRANSCT = "sql-transaction";
  public static final String SQLTEMPLTE = "sql-template";

  @Autowired private ApplicationContext appctx;

  @Autowired Settings settings;

  @PostConstruct
  public void init() {
  }

  @Bean @Qualifier(DATASOURCE)
  @ConfigurationProperties(prefix = "spring.datasource")
  public DataSource datasourceKcdfm() {
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

  @Bean @Qualifier(SQLFACTORY)
  public SqlSessionFactory sqlSessionFactoryKcdfm() throws Exception {
    SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
    fb.setDataSource(datasourceKcdfm());
    fb.setVfs(SpringBootVFS.class);
    fb.setConfigLocation(appctx.getResource("classpath:mybatis-config.xml"));
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resource = resolver.getResources("mapper/**/*.xml");
    fb.setMapperLocations(resource);
    return fb.getObject();
  }

  @Bean @Qualifier(SQLTRANSCT)
  public DataSourceTransactionManager transactionManager(
    @Autowired @Qualifier(DATASOURCE) DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean @Qualifier(SQLTEMPLTE)
  public SqlSessionTemplate sqlSessionTemplateKcdfm(
    @Autowired @Qualifier(SQLFACTORY) SqlSessionFactory fac) {
    return new SqlSessionTemplate(fac);
  }
}