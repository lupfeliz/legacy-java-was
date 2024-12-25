/**
 * @File        : JDBCSession.java
 * @Author      : 정재백
 * @Since       : 2024-12-17
 * @Description : JDBC Session 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.ReflectionUtil.cast;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.session.DelegatingIndexResolver;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.FlushMode;
import org.springframework.session.IndexResolver;
import org.springframework.session.MapSession;
import org.springframework.session.PrincipalNameIndexResolver;
import org.springframework.session.SaveMode;
import org.springframework.session.Session;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import com.ntiple.system.Settings;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Configuration
public class JDBCSessionConfig {

  @Autowired private Settings settings;

  @PostConstruct public void init() {
  }

  @Bean @Primary
  CustomSessionRepository customSessionRepository() {
    DataSource ds = cast(settings.getAppctx().getBean("datasourceDss"), ds = null);
    CustomSessionRepository ret = new CustomSessionRepository(
      new JdbcTemplate(ds),
      new TransactionTemplate(new DataSourceTransactionManager(ds)));
    String createSession = "INSERT INTO SPRING_SESSION (PRIMARY_ID, SESSION_ID, CREATION_TIME, LAST_ACCESS_TIME, MAX_INACTIVE_INTERVAL, EXPIRY_TIME, PRINCIPAL_NAME) VALUES (?, ?, ?, ?, ?, ?, ?)";
    String createSessionAttribute = "INSERT INTO SPRING_SESSION_ATTRIBUTES(SESSION_PRIMARY_ID, ATTRIBUTE_NAME, ATTRIBUTE_BYTES) VALUES (?, ?, ?)";
    String getSession = "SELECT S.PRIMARY_ID, S.SESSION_ID, S.CREATION_TIME, S.LAST_ACCESS_TIME, S.MAX_INACTIVE_INTERVAL, SA.ATTRIBUTE_NAME, SA.ATTRIBUTE_BYTES FROM SPRING_SESSION S LEFT JOIN SPRING_SESSION_ATTRIBUTES SA ON S.PRIMARY_ID = SA.SESSION_PRIMARY_ID WHERE S.SESSION_ID = ?";
    String updateSession = "UPDATE SPRING_SESSION SET SESSION_ID = ?, LAST_ACCESS_TIME = ?, MAX_INACTIVE_INTERVAL = ?, EXPIRY_TIME = ?, PRINCIPAL_NAME = ? WHERE PRIMARY_ID = ?";
    String updateSessionAttribute = "UPDATE SPRING_SESSION_ATTRIBUTES SET ATTRIBUTE_BYTES = ? WHERE SESSION_PRIMARY_ID = ? AND ATTRIBUTE_NAME = ?";
    String deleteSession = "DELETE FROM SPRING_SESSION WHERE SESSION_ID = ? AND MAX_INACTIVE_INTERVAL >= 0";
    String deleteSessionAttribute = "DELETE FROM SPRING_SESSION_ATTRIBUTES WHERE SESSION_PRIMARY_ID = ? AND ATTRIBUTE_NAME = ?";
    String listSessionsByPrincipalName = "SELECT S.PRIMARY_ID, S.SESSION_ID, S.CREATION_TIME, S.LAST_ACCESS_TIME, S.MAX_INACTIVE_INTERVAL, SA.ATTRIBUTE_NAME, SA.ATTRIBUTE_BYTES FROM SPRING_SESSION S LEFT OUTER JOIN SPRING_SESSION_ATTRIBUTES SA ON S.PRIMARY_ID = SA.SESSION_PRIMARY_ID WHERE S.PRINCIPAL_NAME = ?";
    String deleteSessionsByExpiryTime = "DELETE FROM SPRING_SESSION WHERE EXPIRY_TIME < ?";
    ret.setCreateSessionQuery(createSession);
    ret.setCreateSessionAttributeQuery(createSessionAttribute);
    ret.setGetSessionQuery(getSession);
    ret.setUpdateSessionQuery(updateSession);
    ret.setUpdateSessionAttributeQuery(updateSessionAttribute);
    ret.setDeleteSessionQuery(deleteSession);
    ret.setDeleteSessionAttributeQuery(deleteSessionAttribute);
    ret.setListSessionsByPrincipalNameQuery(listSessionsByPrincipalName);
    ret.setDeleteSessionsByExpiryTimeQuery(deleteSessionsByExpiryTime);
    return ret;
  }

  public class CustomSessionRepository implements FindByIndexNameSessionRepository<CustomSessionRepository.CustomSession> {
    public static final String DEFAULT_TABLE_NAME = "SPRING_SESSION";
    private static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
    private static final String CREATE_SESSION_QUERY = "";
    private static final String CREATE_SESSION_ATTRIBUTE_QUERY = "";
    private static final String GET_SESSION_QUERY = "";
    private static final String UPDATE_SESSION_QUERY = "";
    private static final String UPDATE_SESSION_ATTRIBUTE_QUERY = "";
    private static final String DELETE_SESSION_ATTRIBUTE_QUERY = "";
    private static final String DELETE_SESSION_QUERY = "";
    private static final String LIST_SESSIONS_BY_PRINCIPAL_NAME_QUERY = "";
    private static final String DELETE_SESSIONS_BY_EXPIRY_TIME_QUERY = "";
    private final JdbcOperations jdbcOperations;
    private final TransactionOperations transactionOperations;
    private final ResultSetExtractor<List<CustomSession>> extractor = new SessionResultSetExtractor();
    private String tableName = DEFAULT_TABLE_NAME;
    private String createSessionQuery;
    private String createSessionAttributeQuery;
    private String getSessionQuery;
    private String updateSessionQuery;
    private String updateSessionAttributeQuery;
    private String deleteSessionAttributeQuery;
    private String deleteSessionQuery;
    private String listSessionsByPrincipalNameQuery;
    private String deleteSessionsByExpiryTimeQuery;
    private Integer defaultMaxInactiveInterval;
    private IndexResolver<Session> indexResolver = new DelegatingIndexResolver<>(new PrincipalNameIndexResolver<>());
    private ConversionService conversionService = createDefaultConversionService();
    private LobHandler lobHandler = new DefaultLobHandler();
    private FlushMode flushMode = FlushMode.ON_SAVE;
    private SaveMode saveMode = SaveMode.ON_SET_ATTRIBUTE;
    public CustomSessionRepository(JdbcOperations jdbcOperations, TransactionOperations transactionOperations) {
      this.jdbcOperations = jdbcOperations;
      this.transactionOperations = transactionOperations;
      prepareQueries();
    }
    public void setTableName(String tableName) {
      this.tableName = tableName.trim();
      prepareQueries();
    }
    public void setCreateSessionQuery(String createSessionQuery) { this.createSessionQuery = getQuery(createSessionQuery); }
    public void setCreateSessionAttributeQuery(String createSessionAttributeQuery) { this.createSessionAttributeQuery = getQuery(createSessionAttributeQuery); }
    public void setGetSessionQuery(String getSessionQuery) { this.getSessionQuery = getQuery(getSessionQuery); }
    public void setUpdateSessionQuery(String updateSessionQuery) { this.updateSessionQuery = getQuery(updateSessionQuery); }
    public void setUpdateSessionAttributeQuery(String updateSessionAttributeQuery) { this.updateSessionAttributeQuery = getQuery(updateSessionAttributeQuery); }
    public void setDeleteSessionAttributeQuery(String deleteSessionAttributeQuery) { this.deleteSessionAttributeQuery = getQuery(deleteSessionAttributeQuery); }
    public void setDeleteSessionQuery(String deleteSessionQuery) { this.deleteSessionQuery = getQuery(deleteSessionQuery); }
    public void setListSessionsByPrincipalNameQuery(String listSessionsByPrincipalNameQuery) { this.listSessionsByPrincipalNameQuery = getQuery(listSessionsByPrincipalNameQuery); }
    public void setDeleteSessionsByExpiryTimeQuery(String deleteSessionsByExpiryTimeQuery) { this.deleteSessionsByExpiryTimeQuery = getQuery(deleteSessionsByExpiryTimeQuery); }
    public void setDefaultMaxInactiveInterval(Integer defaultMaxInactiveInterval) { this.defaultMaxInactiveInterval = defaultMaxInactiveInterval; }
    public void setIndexResolver(IndexResolver<Session> indexResolver) { this.indexResolver = indexResolver; }
    public void setLobHandler(LobHandler lobHandler) { this.lobHandler = lobHandler; }
    public void setConversionService(ConversionService conversionService) { this.conversionService = conversionService; }
    public void setFlushMode(FlushMode flushMode) { this.flushMode = flushMode; }
    public void setSaveMode(SaveMode saveMode) { this.saveMode = saveMode; }

    @Override public CustomSession createSession() {
      MapSession delegate = new MapSession();
      if (this.defaultMaxInactiveInterval != null) { delegate.setMaxInactiveInterval(Duration.ofSeconds(this.defaultMaxInactiveInterval)); }
      CustomSession session = new CustomSession(delegate, UUID.randomUUID().toString(), true);
      session.flushIfRequired();
      log.debug("SESSION-CREATED!!! : {}", session.getId());
      return session;
    }

    @Override public void save(final CustomSession session) { session.save(); }
    @Override public CustomSession findById(final String id) {
      final CustomSession session = this.transactionOperations.execute((status) -> {
        List<CustomSession> sessions = CustomSessionRepository.this.jdbcOperations.query(
          CustomSessionRepository.this.getSessionQuery, (ps) -> ps.setString(1, id),
          CustomSessionRepository.this.extractor);
        if (sessions.isEmpty()) { return null; }
        return sessions.get(0);
      });
      if (session != null) {
        if (session.isExpired()) {
          deleteById(id);
        } else {
          return session;
        }
      }
      return null;
    }

    @Override public void deleteById(final String id) {
      log.debug("SESSION-DESTROY!!! : {}", id);
      this.transactionOperations.executeWithoutResult((status) -> CustomSessionRepository.this.jdbcOperations
        .update(CustomSessionRepository.this.deleteSessionQuery, id));
    }

    @Override public Map<String, CustomSession> findByIndexNameAndIndexValue(String indexName, final String indexValue) {
      if (!PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) { return Collections.emptyMap(); }
      List<CustomSession> sessions = this.transactionOperations
        .execute((status) -> CustomSessionRepository.this.jdbcOperations.query(
          CustomSessionRepository.this.listSessionsByPrincipalNameQuery,
          (ps) -> ps.setString(1, indexValue), CustomSessionRepository.this.extractor));
      Map<String, CustomSession> sessionMap = new HashMap<>(sessions.size());
      for (CustomSession session : sessions) { sessionMap.put(session.getId(), session); }
      return sessionMap;
    }

    private void insertSessionAttributes(CustomSession session, List<String> attributeNames) {
      try (LobCreator lobCreator = this.lobHandler.getLobCreator()) {
        if (attributeNames.size() > 1) {
          try {
            this.jdbcOperations.batchUpdate(this.createSessionAttributeQuery,
              new BatchPreparedStatementSetter() {
                @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
                  String attributeName = attributeNames.get(i);
                  ps.setString(1, session.primaryKey);
                  ps.setString(2, attributeName);
                  lobCreator.setBlobAsBytes(ps, 3, serialize(session.getAttribute(attributeName)));
                }
                @Override public int getBatchSize() { return attributeNames.size(); }
              });
          } catch (DuplicateKeyException e) {
            throw e;
          } catch (DataIntegrityViolationException e) {
            log.debug("E:{}", e);
          }
        } else {
          try {
            this.jdbcOperations.update(this.createSessionAttributeQuery, (ps) -> {
              String attributeName = attributeNames.get(0);
              ps.setString(1, session.primaryKey);
              ps.setString(2, attributeName);
              lobCreator.setBlobAsBytes(ps, 3, serialize(session.getAttribute(attributeName)));
            });
          } catch (DuplicateKeyException e) {
            throw e;
          } catch (DataIntegrityViolationException e) {
            log.debug("E:{}", e);
          }
        }
      }
    }

    private void updateSessionAttributes(CustomSession session, List<String> attributeNames) {
      try (LobCreator lobCreator = this.lobHandler.getLobCreator()) {
        if (attributeNames.size() > 1) {
          this.jdbcOperations.batchUpdate(this.updateSessionAttributeQuery, new BatchPreparedStatementSetter() {
            @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
              String attributeName = attributeNames.get(i);
              lobCreator.setBlobAsBytes(ps, 1, serialize(session.getAttribute(attributeName)));
              ps.setString(2, session.primaryKey);
              ps.setString(3, attributeName);
            }
            @Override public int getBatchSize() { return attributeNames.size(); }
          });
        } else {
          this.jdbcOperations.update(this.updateSessionAttributeQuery, (ps) -> {
            String attributeName = attributeNames.get(0);
            lobCreator.setBlobAsBytes(ps, 1, serialize(session.getAttribute(attributeName)));
            ps.setString(2, session.primaryKey);
            ps.setString(3, attributeName);
          });
        }
      }
    }
    private void deleteSessionAttributes(CustomSession session, List<String> attributeNames) {
      if (attributeNames.size() > 1) {
        this.jdbcOperations.batchUpdate(this.deleteSessionAttributeQuery, new BatchPreparedStatementSetter() {
          @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
            String attributeName = attributeNames.get(i);
            ps.setString(1, session.primaryKey);
            ps.setString(2, attributeName);
          }
          @Override public int getBatchSize() { return attributeNames.size(); }
        });
      } else {
        this.jdbcOperations.update(this.deleteSessionAttributeQuery, (ps) -> {
          String attributeName = attributeNames.get(0);
          ps.setString(1, session.primaryKey);
          ps.setString(2, attributeName);
        });
      }
    }
    public void cleanUpExpiredSessions() {
      Integer deletedCount = this.transactionOperations
        .execute((status) -> CustomSessionRepository.this.jdbcOperations.update(
          CustomSessionRepository.this.deleteSessionsByExpiryTimeQuery, System.currentTimeMillis()));
      if (log.isDebugEnabled()) { log.debug("Cleaned up {} expired sessions", deletedCount); }
    }
    private String getQuery(String base) { return StringUtils.replace(base, "%TABLE_NAME%", this.tableName); }
    private void prepareQueries() {
      this.createSessionQuery = getQuery(CREATE_SESSION_QUERY);
      this.createSessionAttributeQuery = getQuery(CREATE_SESSION_ATTRIBUTE_QUERY);
      this.getSessionQuery = getQuery(GET_SESSION_QUERY);
      this.updateSessionQuery = getQuery(UPDATE_SESSION_QUERY);
      this.updateSessionAttributeQuery = getQuery(UPDATE_SESSION_ATTRIBUTE_QUERY);
      this.deleteSessionAttributeQuery = getQuery(DELETE_SESSION_ATTRIBUTE_QUERY);
      this.deleteSessionQuery = getQuery(DELETE_SESSION_QUERY);
      this.listSessionsByPrincipalNameQuery = getQuery(LIST_SESSIONS_BY_PRINCIPAL_NAME_QUERY);
      this.deleteSessionsByExpiryTimeQuery = getQuery(DELETE_SESSIONS_BY_EXPIRY_TIME_QUERY);
    }
    private LobHandler getLobHandler() { return this.lobHandler; }
    private byte[] serialize(Object object) {
      return (byte[]) this.conversionService.convert(object, TypeDescriptor.valueOf(Object.class),
        TypeDescriptor.valueOf(byte[].class));
    }
    private Object deserialize(byte[] bytes) {
      return this.conversionService.convert(bytes, TypeDescriptor.valueOf(byte[].class),
        TypeDescriptor.valueOf(Object.class));
    }
    private class SessionResultSetExtractor implements ResultSetExtractor<List<CustomSession>> {
      @Override public List<CustomSession> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<CustomSession> sessions = new ArrayList<>();
        while (rs.next()) {
          String id = rs.getString("SESSION_ID");
          CustomSession session;
          if (sessions.size() > 0 && getLast(sessions).getId().equals(id)) {
            session = getLast(sessions);
          } else {
            MapSession delegate = new MapSession(id);
            String primaryKey = rs.getString("PRIMARY_ID");
            delegate.setCreationTime(Instant.ofEpochMilli(rs.getLong("CREATION_TIME")));
            delegate.setLastAccessedTime(Instant.ofEpochMilli(rs.getLong("LAST_ACCESS_TIME")));
            delegate.setMaxInactiveInterval(Duration.ofSeconds(rs.getInt("MAX_INACTIVE_INTERVAL")));
            session = new CustomSession(delegate, primaryKey, false);
          }
          String attributeName = rs.getString("ATTRIBUTE_NAME");
          if (attributeName != null) {
            byte[] bytes = getLobHandler().getBlobAsBytes(rs, "ATTRIBUTE_BYTES");
            session.delegate.setAttribute(attributeName, lazily(() -> deserialize(bytes)));
          }
          sessions.add(session);
        }
        return sessions;
      }
      private CustomSession getLast(List<CustomSession> sessions) { return sessions.get(sessions.size() - 1); }
    }
    class CustomSession implements Session {
      private final Session delegate;
      private final String primaryKey;
      private boolean isNew;
      private boolean changed;
      private Map<String, DeltaValue> delta = new HashMap<>();
      CustomSession(MapSession delegate, String primaryKey, boolean isNew) {
        this.delegate = delegate;
        this.primaryKey = primaryKey;
        this.isNew = isNew;
        if (this.isNew || (CustomSessionRepository.this.saveMode == SaveMode.ALWAYS)) {
          getAttributeNames().forEach((attributeName) -> this.delta.put(attributeName, DeltaValue.UPDATED));
        }
      }
      boolean isNew() { return this.isNew; }
      boolean isChanged() { return this.changed; }
      Map<String, DeltaValue> getDelta() { return this.delta; }
      void clearChangeFlags() {
        this.isNew = false;
        this.changed = false;
        this.delta.clear();
      }
      Instant getExpiryTime() {
        if (getMaxInactiveInterval().isNegative()) { return Instant.ofEpochMilli(Long.MAX_VALUE); }
        return getLastAccessedTime().plus(getMaxInactiveInterval());
      }
      @Override public String getId() { return this.delegate.getId(); }
      @Override public String changeSessionId() { this.changed = true; return this.delegate.changeSessionId(); }
      @Override
      public <T> T getAttribute(String attributeName) {
        Supplier<T> supplier = this.delegate.getAttribute(attributeName);
        if (supplier == null) { return null; }
        T attributeValue = supplier.get();
        if (attributeValue != null
          && CustomSessionRepository.this.saveMode.equals(SaveMode.ON_GET_ATTRIBUTE)) {
          this.delta.merge(attributeName, DeltaValue.UPDATED, (oldDeltaValue,
            deltaValue) -> (oldDeltaValue == DeltaValue.ADDED) ? oldDeltaValue : deltaValue);
        }
        return attributeValue;
      }

      @Override public Set<String> getAttributeNames() { return this.delegate.getAttributeNames(); }
      @Override
      public void setAttribute(String attributeName, Object attributeValue) {
        boolean attributeExists = (this.delegate.getAttribute(attributeName) != null);
        boolean attributeRemoved = (attributeValue == null);
        if (!attributeExists && attributeRemoved) { return; }
        if (attributeExists) {
          if (attributeRemoved) {
            this.delta.merge(attributeName, DeltaValue.REMOVED,
              (oldDeltaValue, deltaValue) -> (oldDeltaValue == DeltaValue.ADDED) ? null : deltaValue);
          } else {
            this.delta.merge(attributeName, DeltaValue.UPDATED, (oldDeltaValue,
              deltaValue) -> (oldDeltaValue == DeltaValue.ADDED) ? oldDeltaValue : deltaValue);
          }
        } else {
          this.delta.merge(attributeName, DeltaValue.ADDED, (oldDeltaValue,
            deltaValue) -> (oldDeltaValue == DeltaValue.ADDED) ? oldDeltaValue : DeltaValue.UPDATED);
        }
        this.delegate.setAttribute(attributeName, value(attributeValue));
        if (PRINCIPAL_NAME_INDEX_NAME.equals(attributeName) || SPRING_SECURITY_CONTEXT.equals(attributeName)) {
          this.changed = true;
        }
        flushIfRequired();
      }

      @Override public void removeAttribute(String attributeName) { setAttribute(attributeName, null); }
      @Override public Instant getCreationTime() { return this.delegate.getCreationTime(); }
      @Override
      public void setLastAccessedTime(Instant lastAccessedTime) {
        this.delegate.setLastAccessedTime(lastAccessedTime);
        this.changed = true;
        flushIfRequired();
      }
      @Override public Instant getLastAccessedTime() { return this.delegate.getLastAccessedTime(); }
      @Override
      public void setMaxInactiveInterval(Duration interval) {
        this.delegate.setMaxInactiveInterval(interval);
        this.changed = true;
        flushIfRequired();
      }
      @Override public Duration getMaxInactiveInterval() { return this.delegate.getMaxInactiveInterval(); }
      @Override public boolean isExpired() { return this.delegate.isExpired(); }
      private void flushIfRequired() { if (CustomSessionRepository.this.flushMode == FlushMode.IMMEDIATE) { save(); } }
      private void save() {
        if (this.isNew) {
          CustomSessionRepository.this.transactionOperations.executeWithoutResult((status) -> {
            Map<String, String> indexes = CustomSessionRepository.this.indexResolver
              .resolveIndexesFor(CustomSession.this);
            CustomSessionRepository.this.jdbcOperations
              .update(CustomSessionRepository.this.createSessionQuery, (ps) -> {
                ps.setString(1, CustomSession.this.primaryKey);
                ps.setString(2, getId());
                ps.setLong(3, getCreationTime().toEpochMilli());
                ps.setLong(4, getLastAccessedTime().toEpochMilli());
                ps.setInt(5, (int) getMaxInactiveInterval().getSeconds());
                ps.setLong(6, getExpiryTime().toEpochMilli());
                ps.setString(7, indexes.get(PRINCIPAL_NAME_INDEX_NAME));
              });
            Set<String> attributeNames = getAttributeNames();
            if (!attributeNames.isEmpty()) { insertSessionAttributes(CustomSession.this, new ArrayList<>(attributeNames)); }
          });
        } else {
          CustomSessionRepository.this.transactionOperations.executeWithoutResult((status) -> {
            if (CustomSession.this.changed) {
              Map<String, String> indexes = CustomSessionRepository.this.indexResolver
                .resolveIndexesFor(CustomSession.this);
              CustomSessionRepository.this.jdbcOperations
                .update(CustomSessionRepository.this.updateSessionQuery, (ps) -> {
                  ps.setString(1, getId());
                  ps.setLong(2, getLastAccessedTime().toEpochMilli());
                  ps.setInt(3, (int) getMaxInactiveInterval().getSeconds());
                  ps.setLong(4, getExpiryTime().toEpochMilli());
                  ps.setString(5, indexes.get(PRINCIPAL_NAME_INDEX_NAME));
                  ps.setString(6, CustomSession.this.primaryKey);
                });
            }
            List<String> addedAttributeNames = CustomSession.this.delta.entrySet().stream()
              .filter((entry) -> entry.getValue() == DeltaValue.ADDED).map(Map.Entry::getKey)
              .collect(Collectors.toList());
            if (!addedAttributeNames.isEmpty()) {
              insertSessionAttributes(CustomSession.this, addedAttributeNames);
            }
            List<String> updatedAttributeNames = CustomSession.this.delta.entrySet().stream()
              .filter((entry) -> entry.getValue() == DeltaValue.UPDATED).map(Map.Entry::getKey)
              .collect(Collectors.toList());
            if (!updatedAttributeNames.isEmpty()) {
              updateSessionAttributes(CustomSession.this, updatedAttributeNames);
            }
            List<String> removedAttributeNames = CustomSession.this.delta.entrySet().stream()
              .filter((entry) -> entry.getValue() == DeltaValue.REMOVED).map(Map.Entry::getKey)
              .collect(Collectors.toList());
            if (!removedAttributeNames.isEmpty()) {
              deleteSessionAttributes(CustomSession.this, removedAttributeNames);
            }
          });
        }
        clearChangeFlags();
      }
    }
  }
  private enum DeltaValue { ADDED, UPDATED, REMOVED }
  private static GenericConversionService createDefaultConversionService() {
    GenericConversionService converter = new GenericConversionService();
    converter.addConverter(Object.class, byte[].class, new SerializingConverter());
    converter.addConverter(byte[].class, Object.class, new DeserializingConverter());
    return converter;
  }
  private static <T> Supplier<T> value(T value) { return (value != null) ? () -> value : null; }
  private static <T> Supplier<T> lazily(Supplier<T> supplier) {
    Supplier<T> lazySupplier = new Supplier<T>() {
      private T value;
      @Override public T get() {
        if (this.value == null) { this.value = supplier.get(); }
        return this.value;
      }
    };
    return (supplier != null) ? lazySupplier : null;
  }
}