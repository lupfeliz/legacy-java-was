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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
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

  @Getter @Setter @ToString
  public static class MapperInfo {
    private String className;
    private Map<String, String> methods = new LinkedHashMap<>();
    private Map<String, String[]> params = new LinkedHashMap<>();
  }

  public static final void applyTypeProcess(SqlSessionFactoryBean fb, String... pkgs) {
    final List<Class<?>> alsLst = new ArrayList<>();
    final List<TypeHandler<?>> hndLst = new ArrayList<>();
    log.debug("APPLY-TYPE-PROCESS..:{}{}", "", pkgs);
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

  public SqlSessionFactory configSqlSession(DataSource source,
    ApplicationContext appctx,
    String nameDatasrc,
    String nameSqlfctr,
    String nameSqltmpl,
    String nameSqltrnx,
    String pthMyaatis,
    String ptnRsrc, String... pkgs) throws Exception {
    // log.debug("================================================================================");
    // log.debug("configSqlSession");
    ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) appctx).getBeanFactory();
    SqlSessionFactoryBean qsFactoryBean = new SqlSessionFactoryBean();
    qsFactoryBean.setDataSource(source);
    qsFactoryBean.setConfigLocation(appctx.getResource(pthMyaatis));
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    // log.debug("CHECK-RESOURCES:{}{}", "", resolver.getResources("com/ntiple/work/**/*.class"));
    Resource[] resources = resolver.getResources(ptnRsrc);
    final List<MapperInfo> mapperList = new ArrayList<>();
    qsFactoryBean.setMapperLocations(resources);
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
      mapperList.add(info);
      // log.debug("INFO:{}", info);
    }
    applyTypeProcess(qsFactoryBean, pkgs);
    SqlSessionFactory qsfc = qsFactoryBean.getObject();
    {
      /** 트랜잭션 매니저 등록 */
      beanFactory.registerSingleton(nameSqltrnx, new DataSourceTransactionManager(source));
      /** SQL 템플릴 생성 */
      SqlSessionTemplate qstp = new SqlSessionTemplate(qsfc);
      // log.debug("INFO:{}", mapperList);
      LOOP1: for (final MapperInfo info : mapperList) {
        try {
          /** SQL맵 생성 */
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
            if (List.class.isAssignableFrom(rtype) && "select".equals(qtype)) {
              info.methods.put(mname, "selectList");
            } else if (Iterable.class.isAssignableFrom(rtype) && "select".equals(qtype)) {
              info.methods.put(mname, "selectIter");
            }
            // log.debug("METHOD:{} / {} / {}",
            //   mname,
            //   info.getMethods().get(mname), 
            //   info.getParams().get(mname));
            continue LOOP2;
          }
          // bean = qstp.getMapper(cls);
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
            case "selectList": { res = qstp.selectList(ns, pmap); } break;
            case "selectIter": { res = qstp.selectCursor(ns, pmap); } break;
            case "select": { res = qstp.selectOne(ns, pmap); } break;
            case "update": { res = qstp.update(ns, pmap); } break;
            case "insert": { res = qstp.insert(ns, pmap); } break;
            case "delete": { res = qstp.delete(ns, pmap); } break;
            default: }
            return res;
          });
          // log.debug("CHECK:{} / {} / {}", cls.isInstance(bean), bean, bean.getClass());
          /** SQL맵 등록 */
          log.debug("REGISTER-BEAN:{} / {}", cls, bean);
          beanFactory.registerResolvableDependency(cls, bean);
        } catch (Exception e) { log.debug("E:", e); }
        continue LOOP1;
      }
      /** SQL 템플릴 등록 */
      beanFactory.registerSingleton(nameSqltmpl, qstp);
    }
    return qsfc;
  }

  @Bean(name = DATASOURCE_MAIN) @Primary
  @ConfigurationProperties(prefix = "spring.datasource-main")
  DataSource datasourceMain() throws Exception {
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
    configSqlSession(ret,
      settings.getAppctx(),
      DATASOURCE_MAIN,
      SQLFACTORY_MAIN,
      SQLTEMPLTE_MAIN,
      SQLTRANSCT_MAIN,
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
    } else {
      log.debug("USING HIKARI POOL");
      ret = DataSourceBuilder.create()
        .type(HikariDataSource.class)
        .build();
    }
    return ret;
  }
}