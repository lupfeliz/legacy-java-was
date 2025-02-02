/**
 * @File        : PersistentConfig.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 데이터베이스 스프링부트 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.ConvertUtil.array;
import static com.ntiple.commons.ConvertUtil.convert;
import static com.ntiple.commons.ConvertUtil.list;
import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.ReflectionUtil.findConstructor;
import static com.ntiple.commons.StringUtil.cat;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.Alias;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import com.ntiple.Application;
import com.ntiple.commons.ClassWorker;
import com.ntiple.commons.XMLWorker;
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
    /** Bean 등록방법 */
    Settings bean = Settings.getInstance();
    ConfigurableListableBeanFactory bf = ((ConfigurableApplicationContext) settings.getAppctx()).getBeanFactory();
    bf.registerSingleton(bean.getClass().getCanonicalName(), bean);
  }

  private static DataSource getJndiDataSource(String jndiName) {
    DataSource ret = null;
    JndiDataSourceLookup lookup = new JndiDataSourceLookup();
    if (ret == null) {
      try {
        ret = lookup.getDataSource(cat("java:/comp/env/jdbc/", jndiName));
      } catch (Exception e) {
        log.debug("E: java:/comp/env/jdbc/{} NOT FOUND", jndiName, e.getMessage());
      }
    }
    if (ret == null) {
      try {
        ret = lookup.getDataSource(cat("java:/jdbc/", jndiName));
      } catch (Exception e) {
        log.debug("E: java:/jdbc/{} NOT FOUND", jndiName, e.getMessage());
      }
    }
    return ret;
  }

  @Documented @Inherited @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
  public @interface MapperMain { }

  public static final void applyTypeProcess(SqlSessionFactoryBean fb, String... pkgs) {
    final List<Class<?>> alsLst = new ArrayList<>();
    final List<TypeHandler<?>> hndLst = new ArrayList<>();
    log.debug("APPLY-TYPE-PROCESS..");
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

  @Bean(name = DATASOURCE_MAIN) @Primary
  @ConfigurationProperties(prefix = "spring.datasource-main")
  DataSource datasourceMain() {
    DataSource ret = null;
    String jndi = cast(settings.getProperty("spring.datasource-main.jndi-name"), "");
    if (jndi != null && !"".equals(jndi)) {
      log.debug("USING JNDI:{}", jndi);
      ret = getJndiDataSource(jndi);
    } else {
      log.debug("USING HIKARI POOL");
      ret = DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .build();
    }
    return ret;
  }

  @Bean(name = SQLFACTORY_MAIN)
  SqlSessionFactory sqlSessionFactoryMain() throws Exception {
    log.debug("================================================================================");
    log.debug("sqlSessionFactoryMain");
    SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
    fb.setDataSource(datasourceMain());
    fb.setConfigLocation(settings.getAppctx().getResource("classpath:mybatis-config.xml"));
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    log.debug("CHECK-RESOURCES:{}{}", "", resolver.getResources("com/ntiple/work/**/*.class"));
    Resource[] resources = resolver.getResources("mapper/**/*.xml");
    fb.setMapperLocations(resources);
    XMLWorker.parse(c -> { },
      resources[0].getInputStream(), (uri, lname, qname, attr, ctx) -> {
        switch (qname) {
        case "mapper": {
        } break;
        case "select": {
        } break;
        case "update": {
        } break;
        case "insert": {
        } break;
        default: };
        for (int ainx = 0; ainx < attr.getLength(); ainx++) {
          String anam = attr.getQName(ainx);
          String aval = attr.getValue(ainx);
          switch (anam) {
          case "namespace": {
            log.debug("NAMESPACE:{}", aval);
          } break;
          case "id": {
            log.debug("ID:{} / {}", qname, aval);
          } break;
          default: }
        }
        log.debug("XML:{} / {}", qname, attr);
      }, (uri, lname, qname, ctx) -> {
      }, (ch, st, len, ctx) -> {
      });
    applyTypeProcess(fb, "com.ntiple.work", "com.ntiple.system");
    return fb.getObject();
  }

  @Bean(name = SQLTRANSCT_MAIN)
  DataSourceTransactionManager transactionManagerMain(@Autowired @Qualifier(DATASOURCE_MAIN) DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean(name = SQLTEMPLTE_MAIN)
  SqlSessionTemplate sqlSessionTemplateMain(@Autowired @Qualifier(SQLFACTORY_MAIN) SqlSessionFactory fac) {
    SqlSessionTemplate ret = new SqlSessionTemplate(fac);
    try {
      log.debug("--------------------------------------------------------------------------------");
      Object res = ret.selectList("com.ntiple.work.smp01.Smp01001Repository.findSample", convert(new Object[][] {
        { "prm", convert(new Object[][] {
            { "test", "TEST" }
          }, newMap()) }
      }, newMap()));
      log.debug("CHECK:{}", res);
    } catch (Exception e) {
      log.debug("E:{}", e.getMessage());
    }
    try {
      Object bean = null;
      Class<?> cls = com.ntiple.work.smp01.Smp01001Repository.class;
      bean = Proxy.newProxyInstance(cls.getClassLoader(), array(cls), (prx, mtd, arg) -> {
        String mname = mtd.getName();
        log.debug("CHECK:{} / {} / {}", prx, mname, arg);
        switch (mname) {
        case "toString": { return this.toString(); }
        case "findSample": { return list(newMap(), newMap()); }
        default: }
        // return m.invoke(p, a);
        return null;
      });

      // log.debug("BEAN:{}", bean.getClass());
      ConfigurableListableBeanFactory bf = ((ConfigurableApplicationContext) settings.getAppctx()).getBeanFactory();
      // bean = new com.ntiple.work.smp01.Smp01001Repository() {
      //   @Override public List<SampleArticle> findSample(Object prm) throws Exception { return new ArrayList<>(); }
      //   @Override public Integer countSample(Object prm) throws Exception { return 0; }
      //   @Override public Integer addSample(Object prm) throws Exception { return 0; }
      // };
      log.debug("CHECK:{} / {} / {}", cls.isInstance(bean), bean, bean.getClass());
      bf.registerResolvableDependency(cls, bean);
      // bf.registerSingleton(bean.getClass().getCanonicalName(), bean);
      // bf.registerSingleton("com.ntiple.work.smp01.Smp01001Repository", bean);
    } catch (Exception e) {
      log.debug("E:", e);
    }
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
    } else {
      log.debug("USING HIKARI POOL");
      ret = DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .build();
    }
    return ret;
  }
}