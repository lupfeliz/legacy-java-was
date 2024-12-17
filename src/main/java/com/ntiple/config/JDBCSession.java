/**
 * @File        : JDBCSession.java
 * @Author      : 정재백
 * @Since       : 2024-12-17
 * @Description : JDBC Session 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration @EnableJdbcHttpSession
public class JDBCSession extends AbstractHttpSessionApplicationInitializer {
  @Bean
  public DataSource dataSourceDbSession() {
    // return new EmbeddedDatabaseBuilder()
    //   .setType(EmbeddedDatabaseType.H2)
    //   .addScript("org/springframework/session/jdbc/schema-h2.sql").build();
    // DataSource ret = new DriverManagerDataSource("jdbc:postgresql://172.17.0.1:5432/myworks", "myuser", "password");
    DataSource ret = new DriverManagerDataSource("jdbc:h2:mem:test", "sa", "");
    Connection con = null;
    PreparedStatement pst = null;
    try {
      // {
      //   con = ret.getConnection();
      //   pst = con.prepareStatement("""
      //     CREATE TABLE IF NOT EXISTS SPRING_SESSION (
      //     PRIMARY_ID CHAR(36) NOT NULL,
      //     SESSION_ID CHAR(36) NOT NULL,
      //     CREATION_TIME BIGINT NOT NULL,
      //     LAST_ACCESS_TIME BIGINT NOT NULL,
      //     MAX_INACTIVE_INTERVAL INT NOT NULL,
      //     EXPIRY_TIME BIGINT NOT NULL,
      //     PRINCIPAL_NAME VARCHAR(100),
      //     CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
      //     ) """);
      //   pst.executeUpdate();
      //   pst.close();
      // }
      // {
      //   con = ret.getConnection();
      //   pst = con.prepareStatement("CREATE UNIQUE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID)");
      //   pst.executeUpdate();
      //   pst.close();
      // }
      // {
      //   con = ret.getConnection();
      //   pst = con.prepareStatement("CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME)");
      //   pst.executeUpdate();
      //   pst.close();
      // }
      // {
      //   con = ret.getConnection();
      //   pst = con.prepareStatement("CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME)");
      //   pst.executeUpdate();
      //   pst.close();
      // }
      // {
      //   con = ret.getConnection();
      //   pst = con.prepareStatement("""
      //     CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
      //       SESSION_PRIMARY_ID CHAR(36) NOT NULL,
      //       ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
      //       ATTRIBUTE_BYTES BYTEA NOT NULL,
      //       CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
      //       CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
      //     )""");
      //   pst.executeUpdate();
      //   pst.close();
      // }
    } catch (Exception e) {
      log.debug("E:{}", e);
    } finally {
      if (pst != null) { try { pst.close(); } catch (Exception e) { log.debug("E:{}", e); } }
      if (con != null) { try { con.close(); } catch (Exception e) { log.debug("E:{}", e); } }
    }
    // Object obj = EmbeddedDatabase.class.getClassLoader().getResource("org/springframework/session/jdbc/schema-postgresql.sql");
    // log.trace("CHECK-SCRIPT:{}", obj);
    return ret;
  }

  @Bean
  public PlatformTransactionManager transactionManagerDbSession(DataSource dataSourceDbSession) {
    return new DataSourceTransactionManager(dataSourceDbSession);
  }

  @Bean
	public SessionRepositoryCustomizer<JdbcIndexedSessionRepository> tableNameSessionRepositoryCustomizer() {
		return sessionRepository -> {
			// String createSessionAttribute = """
      //   INSERT INTO SPRING_SESSION_ATTRIBUTES(SESSION_PRIMARY_ID, ATTRIBUTE_NAME, ATTRIBUTE_BYTES)
      //   VALUES (?, ?, ?)
      //   ON CONFLICT (SESSION_PRIMARY_ID, ATTRIBUTE_NAME)
      //   DO NOTHING
      //   """;
			// String getSession = """
      //   SELECT S.PRIMARY_ID, S.SESSION_ID, S.CREATION_TIME, S.LAST_ACCESS_TIME, S.MAX_INACTIVE_INTERVAL, SA.ATTRIBUTE_NAME, SA.ATTRIBUTE_BYTES   
      //   FROM SPRING_SESSION S 
      //   LEFT OUTER JOIN SPRING_SESSION_ATTRIBUTES SA ON S.PRIMARY_ID = SA.SESSION_PRIMARY_ID 
      //   WHERE S.SESSION_ID = ?
      //   """;
			// String updateSessionAttribute = """
      //   UPDATE SPRING_SESSION_ATTRIBUTES SET ATTRIBUTE_BYTES = ?   
      //   WHERE SESSION_PRIMARY_ID = ? 
      //   AND ATTRIBUTE_NAME = ?
      //   """;
			// String deleteSessionAttribute = """
      //   DELETE FROM SPRING_SESSION_ATTRIBUTES   
      //   WHERE SESSION_PRIMARY_ID = ? 
      //   AND ATTRIBUTE_NAME = ?
      //   """;
			// String listSessionsByPrincipalName = """
      //   SELECT S.PRIMARY_ID, S.SESSION_ID, S.CREATION_TIME, S.LAST_ACCESS_TIME, S.MAX_INACTIVE_INTERVAL, SA.ATTRIBUTE_NAME, SA.ATTRIBUTE_BYTES  
      //   FROM SPRING_SESSION S 
      //   LEFT OUTER JOIN SPRING_SESSION_ATTRIBUTES SA ON S.PRIMARY_ID = SA.SESSION_PRIMARY_ID
      //   WHERE S.PRINCIPAL_NAME = ?
      //   """;
      // sessionRepository.setCreateSessionAttributeQuery(createSessionAttribute);
      // sessionRepository.setGetSessionQuery(getSession);
      // sessionRepository.setUpdateSessionAttributeQuery(updateSessionAttribute);
      // sessionRepository.setDeleteSessionAttributeQuery(deleteSessionAttribute);
      // sessionRepository.setListSessionsByPrincipalNameQuery(listSessionsByPrincipalName);
		};
	}
}