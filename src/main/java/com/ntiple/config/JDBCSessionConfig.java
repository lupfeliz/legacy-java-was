/**
 * @File        : JDBCSession.java
 * @Author      : 정재백
 * @Since       : 2024-12-17
 * @Description : JDBC Session 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.ReflectionUtil.cast;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import com.ntiple.system.Settings;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration @EnableJdbcHttpSession(cleanupCron = "0 0 * * * *")
public class JDBCSessionConfig {

  @Autowired private Settings settings;

  @PostConstruct public void init() {
  }

  @Bean @SpringSessionDataSource
  DataSource dataSourceDbSession() {
    DataSource ret = cast(settings.getAppctx().getBean("datasourceDss"), ret = null);
    return ret;
  }

  @Bean
  SessionRepositoryCustomizer<JdbcIndexedSessionRepository> tableNameSessionRepositoryCustomizer() {
    return r -> {
      String createSession = "INSERT INTO SPRING_SESSION (PRIMARY_ID, SESSION_ID, CREATION_TIME, LAST_ACCESS_TIME, MAX_INACTIVE_INTERVAL, EXPIRY_TIME, PRINCIPAL_NAME) VALUES (?, ?, ?, ?, ?, ?, ?)";
      String createSessionAttribute = "INSERT INTO SPRING_SESSION_ATTRIBUTES(SESSION_PRIMARY_ID, ATTRIBUTE_NAME, ATTRIBUTE_BYTES) VALUES (?, ?, ?)";
      String getSession = "SELECT S.PRIMARY_ID, S.SESSION_ID, S.CREATION_TIME, S.LAST_ACCESS_TIME, S.MAX_INACTIVE_INTERVAL, SA.ATTRIBUTE_NAME, SA.ATTRIBUTE_BYTES FROM SPRING_SESSION S LEFT JOIN SPRING_SESSION_ATTRIBUTES SA ON S.PRIMARY_ID = SA.SESSION_PRIMARY_ID WHERE S.SESSION_ID = ?";
      String updateSession = "UPDATE SPRING_SESSION SET SESSION_ID = ?, LAST_ACCESS_TIME = ?, MAX_INACTIVE_INTERVAL = ?, EXPIRY_TIME = ?, PRINCIPAL_NAME = ? WHERE PRIMARY_ID = ?";
      String updateSessionAttribute = "UPDATE SPRING_SESSION_ATTRIBUTES SET ATTRIBUTE_BYTES = ? WHERE SESSION_PRIMARY_ID = ? AND ATTRIBUTE_NAME = ?";
      String deleteSession = "DELETE FROM SPRING_SESSION WHERE SESSION_ID = ? AND MAX_INACTIVE_INTERVAL >= 0";
      String deleteSessionAttribute = "DELETE FROM SPRING_SESSION_ATTRIBUTES WHERE SESSION_PRIMARY_ID = ? AND ATTRIBUTE_NAME = ?";
      String listSessionsByPrincipalName = "SELECT S.PRIMARY_ID, S.SESSION_ID, S.CREATION_TIME, S.LAST_ACCESS_TIME, S.MAX_INACTIVE_INTERVAL, SA.ATTRIBUTE_NAME, SA.ATTRIBUTE_BYTES FROM SPRING_SESSION S LEFT OUTER JOIN SPRING_SESSION_ATTRIBUTES SA ON S.PRIMARY_ID = SA.SESSION_PRIMARY_ID WHERE S.PRINCIPAL_NAME = ?";
      String deleteSessionsByExpiryTime = "DELETE FROM SPRING_SESSION WHERE EXPIRY_TIME < ?";
      r.setCreateSessionQuery(createSession);
      r.setCreateSessionAttributeQuery(createSessionAttribute);
      r.setGetSessionQuery(getSession);
      r.setUpdateSessionQuery(updateSession);
      r.setUpdateSessionAttributeQuery(updateSessionAttribute);
      r.setDeleteSessionQuery(deleteSession);
      r.setDeleteSessionAttributeQuery(deleteSessionAttribute);
      r.setListSessionsByPrincipalNameQuery(listSessionsByPrincipalName);
      r.setDeleteSessionsByExpiryTimeQuery(deleteSessionsByExpiryTime);
    };
  }
}