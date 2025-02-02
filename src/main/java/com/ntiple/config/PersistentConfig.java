/**
 * @File        : PersistentConfig.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 데이터베이스 스프링부트 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.ConvertUtil.array;
import static com.ntiple.commons.IOUtil.safeclose;
import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.ReflectionUtil.findConstructor;
import static com.ntiple.commons.StringUtil.cat;
import static com.ntiple.commons.XMLWorker.parseXML;
import static com.ntiple.commons.XMLWorker.xmlAttr;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.ibatis.annotations.Param;
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
import org.springframework.context.ApplicationContext;
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
import com.ntiple.system.Settings;
import com.zaxxer.hikari.HikariDataSource;

import lombok.Builder;
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
    // this.doConfig();
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

  @Getter @Setter @ToString
  public static class MapperInfo {
    private String className;
    private Map<String, String> methods = new LinkedHashMap<>();
    private Map<String, String[]> params = new LinkedHashMap<>();
  }

  @Getter @Setter @ToString @Builder
  public static class MapperContext {
    Resource[] resources;
    List<MapperInfo> mapperList;
  }

  private static Map<String, MapperContext> mapperContext = new LinkedHashMap<>();

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

  // public void doConfig() {
  //   Class<?>clsArray[] = new Class[] {
  //     com.ntiple.work.cmn01.Cmn01001Repository.class,
  //     com.ntiple.work.sys01.Sys01001Repository.class,
  //     com.ntiple.work.smp01.Smp01001Repository.class
  //   };
  //   for (Class<?> cls : clsArray) {
  //     try {
  //         Object bean = Proxy.newProxyInstance(cls.getClassLoader(), array(cls), (prx, mtd, arg) -> {
  // log.debug("EXECUTE...:{}", mtd.getName());
  //           String mname = mtd.getName();
  //           // log.debug("EXECUTE:{} / {}", cls, mname);
  //           switch (mname) {
  //           case "toString": { return this.toString(); }
  //           case "equals": { return this.equals(arg[0]); }
  //           default: }
  //           return null;
  //         });
  //         ConfigurableListableBeanFactory bf = settings.getBeanFactory();
  // log.debug("REGISTER:{} / {}", cls, bean);
  //         bf.registerResolvableDependency(cls, bean);
  //     } catch (Exception e) {
  //       log.debug("E:", e);
  //     }
  //   }
  // }

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
    // log.debug("CHECK-RESOURCES:{}{}", "", resolver.getResources("com/ntiple/work/**/*.class"));
    Resource[] resources = resolver.getResources("mapper/**/*.xml");
    final MapperContext config = MapperContext.builder()
      .resources(resources)
      .mapperList(new ArrayList<>())
      .build();
    mapperContext.put(SQLFACTORY_MAIN, config);
    fb.setMapperLocations(resources);
    for (Resource resource : resources) {
      InputStream istream = null;
      final MapperInfo info = new MapperInfo();
      try {
        parseXML(c -> { },
          istream = resource.getInputStream(), (uri, lname, qname, depth, attr, ctx) -> {
          switch(cat(depth, qname)) {
          case "1mapper": {
            String clsName = xmlAttr(attr, "namespace");
            info.setClassName(clsName);
            // log.debug("MAPPER FOUND:{}", info.getClassName());
          } break;
          case "2select": case "2update": case "2insert": case "2delete": {
            String key = xmlAttr(attr, "id");
            info.getMethods().put(key, qname);
            // log.debug("MAPPER-METHOD FOUND:{} / {}", qname, key);
          } break;
          default: }
        }, (uri, lname, qname, depth, ctx) -> {
        }, (ch, st, len, depth, ctx) -> {
        });
      } finally { safeclose(istream); }
      config.mapperList.add(info);
      // log.debug("INFO:{}", info);
    }
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
    MapperContext config = mapperContext.get(SQLFACTORY_MAIN);
    ApplicationContext appctx = settings.getAppctx();
    ConfigurableListableBeanFactory bf = settings.getBeanFactory();
    log.debug("INFO:{}", config.getMapperList());
    LOOP1: for (final MapperInfo info : config.getMapperList()) {
      try {
        Object bean = null;
        Class<?> cls = Class.forName(info.getClassName());
        // log.debug("MAPPER-CLASS:{}", cls, bean);
        LOOP2: for (Method method : cls.getMethods()) {
          String mname = method.getName();
          String qtype = info.getMethods().get(mname);
          if (qtype == null) { continue LOOP2; }
          Class<?> rtype = method.getReturnType();
          Annotation[][] anns = method.getParameterAnnotations();
          String[] params = new String[anns.length];
          for (int ainx = 0; ainx < anns.length; ainx++) {
            for (Annotation a : anns[ainx]) {
              if (a instanceof Param) { params[ainx] = ((Param) a).value(); }
            }
          }
          info.getParams().put(mname, params);
          if (List.class.isAssignableFrom(rtype) && "select".equals(qtype)) { info.methods.put(mname, "selectList"); }
          // String[] params = info.params.get(mname);
          log.debug("METHOD:{} / {} / {}",
            mname,
            info.getMethods().get(mname), 
            info.getParams().get(mname));
          continue LOOP2;
        }
        bean = Proxy.newProxyInstance(cls.getClassLoader(), array(cls), (prx, mtd, arg) -> {
          String mname = mtd.getName();
          // log.debug("EXECUTE:{} / {}", cls, mname);
          switch (mname) {
          case "toString": { return this.toString(); }
          case "equals": { return this.equals(arg[0]); }
          default: }
          String ns = cat(info.className, ".", mname);
          Map<String, Object> pmap = new LinkedHashMap<>();
          String qtype = info.getMethods().get(mname);
          String[] pnames = info.getParams().get(mname);
          if (qtype == null) { return null; }
          for (int inx = 0; pnames != null && inx < pnames.length && inx < arg.length; inx++) { pmap.put(pnames[inx], arg[inx]); }
          Object res = null;
          switch (qtype) {
          case "selectList": { res = ret.selectList(ns, pmap); } break;
          case "select": { res = ret.selectOne(ns, pmap); } break;
          case "update": { res = ret.update(ns, pmap); } break;
          case "insert": { res = ret.insert(ns, pmap); } break;
          case "delete": { res = ret.delete(ns, pmap); } break;
          default: }
          return res;
        });
        // log.debug("CHECK:{} / {} / {}", cls.isInstance(bean), bean, bean.getClass());
        // bf.destroyBean(appctx.getBean(cls));
        log.debug("REGISTER-BEAN:{} / {}", cls, bean);
        bf.registerResolvableDependency(cls, bean);
      } catch (Exception e) { log.debug("E:", e); }
      continue LOOP1;
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