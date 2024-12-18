/**
 * @File        : JDBCSession.java
 * @Author      : 정재백
 * @Since       : 2024-12-17
 * @Description : JDBC Session 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.ReflectionUtil.cast;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.transaction.PlatformTransactionManager;

import com.ntiple.system.Settings;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration @EnableJdbcHttpSession
public class JDBCSession extends AbstractHttpSessionApplicationInitializer {

  @Autowired private Settings settings;

  @Bean @SpringSessionDataSource
  DataSource dataSourceDbSession() {
    DataSource ret = cast(settings.getAppctx().getBean("datasourceDss"), ret = null);
    return ret;
  }

  // @Bean
  PlatformTransactionManager transactionManagerDbSession(DataSource dataSourceDbSession) {
    return new DataSourceTransactionManager(dataSourceDbSession);
  }

  @Bean
  SessionRepositoryCustomizer<JdbcIndexedSessionRepository> tableNameSessionRepositoryCustomizer() {
    return r -> {
      // String createSessionAttribute = "";
      // // String createSessionAttribute = """
      // //   INSERT INTO SPRING_SESSION_ATTRIBUTES(SESSION_PRIMARY_ID, ATTRIBUTE_NAME, ATTRIBUTE_BYTES)
      // //   VALUES (?, ?, ?)
      // //   ON CONFLICT (SESSION_PRIMARY_ID, ATTRIBUTE_NAME)
      // //   DO NOTHING
      // //   """;
      // String getSession = "";
      // // String getSession = """
      // //   SELECT S.PRIMARY_ID, S.SESSION_ID, S.CREATION_TIME, S.LAST_ACCESS_TIME, S.MAX_INACTIVE_INTERVAL, SA.ATTRIBUTE_NAME, SA.ATTRIBUTE_BYTES   
      // //   FROM SPRING_SESSION S 
      // //   LEFT OUTER JOIN SPRING_SESSION_ATTRIBUTES SA ON S.PRIMARY_ID = SA.SESSION_PRIMARY_ID 
      // //   WHERE S.SESSION_ID = ?
      // //   """;
      // String updateSessionAttribute = "";
      // // String updateSessionAttribute = """
      // //   UPDATE SPRING_SESSION_ATTRIBUTES SET ATTRIBUTE_BYTES = ?   
      // //   WHERE SESSION_PRIMARY_ID = ? 
      // //   AND ATTRIBUTE_NAME = ?
      // //   """;
      // String deleteSessionAttribute = "";
      // // String deleteSessionAttribute = """
      // //   DELETE FROM SPRING_SESSION_ATTRIBUTES   
      // //   WHERE SESSION_PRIMARY_ID = ? 
      // //   AND ATTRIBUTE_NAME = ?
      // //   """;
      // String listSessionsByPrincipalName = "";
      // // String listSessionsByPrincipalName = """
      // //   SELECT S.PRIMARY_ID, S.SESSION_ID, S.CREATION_TIME, S.LAST_ACCESS_TIME, S.MAX_INACTIVE_INTERVAL, SA.ATTRIBUTE_NAME, SA.ATTRIBUTE_BYTES  
      // //   FROM SPRING_SESSION S 
      // //   LEFT OUTER JOIN SPRING_SESSION_ATTRIBUTES SA ON S.PRIMARY_ID = SA.SESSION_PRIMARY_ID
      // //   WHERE S.PRINCIPAL_NAME = ?
      // //   """;
      // r.setCreateSessionAttributeQuery(createSessionAttribute);
      // r.setGetSessionQuery(getSession);
      // r.setUpdateSessionAttributeQuery(updateSessionAttribute);
      // r.setDeleteSessionAttributeQuery(deleteSessionAttribute);
      // r.setListSessionsByPrincipalNameQuery(listSessionsByPrincipalName);
    };
  }
}