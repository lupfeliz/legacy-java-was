/**
 * @File        : JDBCSession.java
 * @Author      : 정재백
 * @Since       : 2024-12-17
 * @Description : JDBC Session 설정
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import static com.ntiple.commons.ConvertUtil.parseInt;
import static com.ntiple.commons.ConvertUtil.parseStr;
import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.WebUtil.curRequest;
import static com.ntiple.config.PersistentConfig.DATASOURCE_DSS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.sql.DataSource;

import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.lang.NonNull;
import org.springframework.session.DelegatingIndexResolver;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.FlushMode;
import org.springframework.session.IndexResolver;
import org.springframework.session.MapSession;
import org.springframework.session.PrincipalNameIndexResolver;
import org.springframework.session.SaveMode;
import org.springframework.session.Session;
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import com.ntiple.commons.ObjectStore;
import com.ntiple.system.Settings;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("deprecation")
@Slf4j @Configuration @EnableJdbcHttpSession
public class JDBCSessionConfig {
  @Autowired private Settings settings;
  @Autowired private HttpSessionListener slistener;
  @Autowired private HttpSessionAttributeListener alistener;

  private static final ObjectStore<CustomSessionRepository> repo = new ObjectStore<>();

  @Bean @SpringSessionDataSource
  DataSource dataSourceDss() {
    /** JDBC 세션 활성화용 FAKE JDBC-SESSION */
    return new EmbeddedDatabaseBuilder()
      .setType(EmbeddedDatabaseType.H2)
      .addScript("org/springframework/session/jdbc/schema-h2.sql")
      .build();
  }
  @PostConstruct public void init() { }
  @PreDestroy public void destroy() { }

  @Bean @Primary CustomSessionRepository customSessionRepository(@Autowired @Qualifier(DATASOURCE_DSS) DataSource dsr) { return new CustomSessionRepository(dsr); }

  public class CustomSessionRepository implements FindByIndexNameSessionRepository<CustomSessionRepository.CustomSession> {
    private static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
    private final JdbcOperations dbc;
    private final TransactionOperations tdc;
    private final ResultSetExtractor<List<CustomSession>> ext = new SessionResultSetExtractor();
    private final List<Pattern> ptnExclude = new ArrayList<>();
    private String createSessionQuery;
    private String createSessionAttributeQuery;
    private String getSessionQuery;
    private String updateSessionQuery;
    private String updateSessionAttributeQuery;
    private String deleteSessionAttributeQuery;
    private String deleteSessionQuery;
    private String listSessionsByPrincipalNameQuery;
    private String deleteSessionsByExpiryTimeQuery;
    private String findLoginIdQuery;
    private String deleteSessionAttributeAllQuery;
    private String deleteSessionByIdQuery;
    private String updateLoginIdQuery;
    private Integer defaultMaxInactiveInterval;
    private IndexResolver<Session> indexResolver = new DelegatingIndexResolver<>(new PrincipalNameIndexResolver<>());
    private FlushMode flushMode = FlushMode.ON_SAVE;
    private SaveMode saveMode = SaveMode.ON_SET_ATTRIBUTE;
    private ConversionService converter = createDefaultConversionService();
    @Bean ConversionService blobToObjectConverter() { return converter; }
    public CustomSessionRepository(DataSource dss) {
      DataSource dsr = cast(settings.getAppctx().getBean("datasourceDss"), DataSource.class);
      this.dbc = new JdbcTemplate(dsr);
      this.tdc = new TransactionTemplate(new DataSourceTransactionManager(dsr));
      List<String> list = null;
      if (dss == dsr &&
        (list = cast(settings.getProperty("system.dbsession.query.create-session-table"), list)) != null) {
        for (String itm : list) {
          log.debug("EXECUTE-DDL:{}", itm);
          try {
            dbc.update(itm, ps -> { });
          } catch (Exception e) {
            log.debug("E:{}", e);
          }
        }
      }
      String s = null;
      List<String> l = null;
      if ((s = parseStr(settings.getProperty("system.dbsession.query.create-session"))) != null) { this.createSessionQuery = s; }
      if ((s = parseStr(settings.getProperty("system.dbsession.query.create-session-attribute"), null)) != null) { this.createSessionAttributeQuery = s; }
      if ((s = parseStr(settings.getProperty("system.dbsession.query.get-session"))) != null) { this.getSessionQuery = s; }
      if ((s = parseStr(settings.getProperty("system.dbsession.query.update-session"))) != null) { this.updateSessionQuery = s; }
      if ((s = parseStr(settings.getProperty("system.dbsession.query.update-session-attribute"))) != null) { this.updateSessionAttributeQuery = s; }
      if ((s = parseStr(settings.getProperty("system.dbsession.query.delete-session"))) != null) { this.deleteSessionQuery = s; }
      if ((s = parseStr(settings.getProperty("system.dbsession.query.delete-session-attribute"))) != null) { this.deleteSessionAttributeQuery = s; }
      if ((s = parseStr(settings.getProperty("system.dbsession.query.list-sessions-by-principal-name"))) != null) { this.listSessionsByPrincipalNameQuery = s; }
      if ((s = parseStr(settings.getProperty("system.dbsession.query.delete-sessions-by-expiry-time"))) != null) { this.deleteSessionsByExpiryTimeQuery = s; }
      if ((s = parseStr(settings.getProperty("system.dbsession.query.find-login-id"))) != null) { this.findLoginIdQuery = s; }
      if ((s = parseStr(settings.getProperty("system.dbsession.query.delete-session-attribute-all"))) != null) { this.deleteSessionAttributeAllQuery = s; }
      if ((s = parseStr(settings.getProperty("system.dbsession.query.delete-session-by-id"))) != null) { this.deleteSessionByIdQuery = s; }
      if ((s = parseStr(settings.getProperty("system.dbsession.query.update-login-id"))) != null) { this.updateLoginIdQuery = s; }
      if ((l = cast(settings.getProperty("system.dbsession.exclude-pattern"), l)) != null) {
        for (String str : l) {
          try { ptnExclude.add(Pattern.compile(str)); } catch (Exception e) { log.debug("E:{}", e.getMessage()); }
        }
      }
      repo.set(this);
    }
    public void setDefaultMaxInactiveInterval(Integer defaultMaxInactiveInterval) { this.defaultMaxInactiveInterval = defaultMaxInactiveInterval; }
    public void setIndexResolver(IndexResolver<Session> indexResolver) { this.indexResolver = indexResolver; }
    public void setConverter(ConversionService conversionService) { this.converter = conversionService; }
    public void setFlushMode(FlushMode flushMode) { this.flushMode = flushMode; }
    public void setSaveMode(SaveMode saveMode) { this.saveMode = saveMode; }

    @Override public CustomSession createSession() {
      MapSession delegate = new MapSession();
      if (this.defaultMaxInactiveInterval != null) { delegate.setMaxInactiveInterval(Duration.ofSeconds(this.defaultMaxInactiveInterval)); }
      CustomSession session = new CustomSession(delegate, UUID.randomUUID().toString(), true);
      session.flushIfRequired();
      if (slistener != null) { slistener.sessionCreated(new HttpSessionEvent(new CustomSessionWrapper(session))); }
      return session;
    }

    @Override public void save(final CustomSession session) { session.save(); }
    @Override public CustomSession findById(final String id) {
      final HttpServletRequest req = curRequest(HttpServletRequest.class);
      if (req != null) {
        for (Pattern ptn : ptnExclude) {
          if (ptn.matcher(req.getRequestURI()).find()) { return null; }
        }
      }
      final CustomSession session = this.tdc.execute(st -> {
        List<CustomSession> sessions = CustomSessionRepository.this.dbc.query(
          CustomSessionRepository.this.getSessionQuery, ps -> ps.setString(1, id),
          CustomSessionRepository.this.ext);
        if (sessions == null || sessions.isEmpty()) { return null; }
        return sessions.get(0);
      });
      if (session != null) {
        if (session.isExpired()) {
          deleteById(id);
        } else {
          if (req != null) { session.sctx = req.getServletContext(); }
          return session;
        }
      }
      return null;
    }

    @Override public void deleteById(final String id) {
      CustomSession session = findById(id);
      log.debug("SESSION-DESTROY!!! : {}", session);
      if (slistener != null) { slistener.sessionCreated(new HttpSessionEvent(new CustomSessionWrapper(session))); }
      this.tdc.executeWithoutResult(st -> CustomSessionRepository.this.dbc
        .update(CustomSessionRepository.this.deleteSessionQuery, id));
    }

    @Override public Map<String, CustomSession> findByIndexNameAndIndexValue(String indexName, final String indexValue) {
      if (!PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) { return Collections.emptyMap(); }
      List<CustomSession> sessions = this.tdc
        .execute(st -> CustomSessionRepository.this.dbc.query(
          CustomSessionRepository.this.listSessionsByPrincipalNameQuery,
          ps -> ps.setString(1, indexValue), CustomSessionRepository.this.ext));
      Map<String, CustomSession> sessionMap = null;
      if (sessions != null) {
        sessionMap = new HashMap<>(sessions.size());
        for (CustomSession session : sessions) { sessionMap.put(session.getId(), session); }
      } else {
        sessionMap = new HashMap<>();
      }
      return sessionMap;
    }

    public String findLoginId(final String loginId, final String sessionId) {
      return this.dbc.query(this.findLoginIdQuery, ps -> {
        ps.setString(1, loginId);
        ps.setString(2, sessionId);
      }, rs -> rs.next() ? rs.getString(1) : null);
    }

    public void deleteSessionAttributeAll(String dupId) {
      this.dbc.update(this.deleteSessionAttributeAllQuery, ps -> { ps.setString(1, dupId); });
    }

    public void deleteSessionById (String dupId) {
      this.dbc.update(this.deleteSessionByIdQuery, ps -> { ps.setString(1, dupId); });
    }

    public void updateLoginId (final String loginId, final String sessionId) {
      this.dbc.update(this.updateLoginIdQuery, ps -> {
        ps.setString(1, loginId);
        ps.setString(2, sessionId);
      });
    }

    private void insertSessionAttributes(CustomSession session, List<String> names) {
      if (names.size() > 1) {
        try {
          this.dbc.batchUpdate(this.createSessionAttributeQuery,
            new BatchPreparedStatementSetter() {
              @Override public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                String name = names.get(i);
                ps.setString(1, session.primaryKey);
                ps.setString(2, name);
                ps.setString(3, Base64.getEncoder().encodeToString(serialize(session.getAttribute(name))));
              }
              @Override public int getBatchSize() { return names.size(); }
            });
        } catch (DuplicateKeyException e) {
          throw e;
        } catch (DataIntegrityViolationException e) {
          log.debug("E:{}", e);
        }
      } else {
        try {
          this.dbc.update(this.createSessionAttributeQuery, ps -> {
            String name = names.get(0);
            // log.debug("SET-ATTRIBUTE:{} / {}", name, session.getAttribute(name));
            ps.setString(1, session.primaryKey);
            ps.setString(2, name);
            ps.setString(3, Base64.getEncoder().encodeToString(serialize(session.getAttribute(name))));
          });
        } catch (DuplicateKeyException e) {
          throw e;
        } catch (DataIntegrityViolationException e) {
          log.debug("E:{}", e);
        }
      }
    }

    private void updateSessionAttributes(CustomSession session, List<String> attributeNames) {
      if (attributeNames.size() > 1) {
        this.dbc.batchUpdate(this.updateSessionAttributeQuery, new BatchPreparedStatementSetter() {
          @Override public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
            String attributeName = attributeNames.get(i);
            ps.setString(1, Base64.getEncoder().encodeToString(serialize(session.getAttribute(attributeName))));
            ps.setString(2, session.primaryKey);
            ps.setString(3, attributeName);
          }
          @Override public int getBatchSize() { return attributeNames.size(); }
        });
      } else {
        this.dbc.update(this.updateSessionAttributeQuery, ps -> {
          String attributeName = attributeNames.get(0);
          ps.setString(1, Base64.getEncoder().encodeToString(serialize(session.getAttribute(attributeName))));
          ps.setString(2, session.primaryKey);
          ps.setString(3, attributeName);
        });
      }
    }
    private void deleteSessionAttributes(CustomSession session, List<String> attributeNames) {
      if (attributeNames.size() > 1) {
        this.dbc.batchUpdate(this.deleteSessionAttributeQuery, new BatchPreparedStatementSetter() {
          @Override public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
            String attributeName = attributeNames.get(i);
            ps.setString(1, session.primaryKey);
            ps.setString(2, attributeName);
          }
          @Override public int getBatchSize() { return attributeNames.size(); }
        });
      } else {
        this.dbc.update(this.deleteSessionAttributeQuery, ps -> {
          String attributeName = attributeNames.get(0);
          ps.setString(1, session.primaryKey);
          ps.setString(2, attributeName);
        });
      }
    }
    public void cleanUpExpiredSessions() {
      Integer deletedCount = this.tdc
        .execute(st -> CustomSessionRepository.this.dbc.update(
          CustomSessionRepository.this.deleteSessionsByExpiryTimeQuery, System.currentTimeMillis()));
      if (log.isDebugEnabled()) { log.debug("Cleaned up {} expired sessions", deletedCount); }
    }
    private byte[] serialize(Object object) {
      return (byte[]) this.converter.convert(object, TypeDescriptor.valueOf(Object.class),
        TypeDescriptor.valueOf(byte[].class));
    }
    private Object deserialize(byte[] bytes) {
      return this.converter.convert(bytes, TypeDescriptor.valueOf(byte[].class),
        TypeDescriptor.valueOf(Object.class));
    }
    private class SessionResultSetExtractor implements ResultSetExtractor<List<CustomSession>> {
      @Override public List<CustomSession> extractData(@NonNull ResultSet rs) throws SQLException, DataAccessException {
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
            byte[] bytes = Base64.getDecoder().decode(rs.getString("ATTRIBUTE_BYTES"));
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
      private ServletContext sctx;
      CustomSession(MapSession delegate, String primaryKey, boolean isNew) {
        this.delegate = delegate;
        this.primaryKey = primaryKey;
        this.isNew = isNew;
        if (this.isNew || (CustomSessionRepository.this.saveMode == SaveMode.ALWAYS)) {
          getAttributeNames().forEach(nm -> this.delta.put(nm, DeltaValue.UPDATED));
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
      @Override public <T> T getAttribute(String attributeName) {
        Supplier<T> supplier = this.delegate.getAttribute(attributeName);
        if (supplier == null) { return null; }
        T attributeValue = supplier.get();
        if (attributeValue != null
          && CustomSessionRepository.this.saveMode.equals(SaveMode.ON_GET_ATTRIBUTE)) {
          this.delta.merge(attributeName, DeltaValue.UPDATED, 
            (odv, ndv) -> (odv == DeltaValue.ADDED) ? odv : ndv);
        }
        return attributeValue;
      }

      @Override public Set<String> getAttributeNames() { return this.delegate.getAttributeNames(); }
      @Override public void setAttribute(String attributeName, Object attributeValue) {
        boolean attributeExists = (this.delegate.getAttribute(attributeName) != null);
        boolean attributeRemoved = (attributeValue == null);
        if (!attributeExists && attributeRemoved) { return; }
        if (attributeExists) {
          if (attributeRemoved) {
            this.delta.merge(attributeName, DeltaValue.REMOVED,
              (odv, ndv) -> (odv == DeltaValue.ADDED) ? null : ndv);
          } else {
            this.delta.merge(attributeName, DeltaValue.UPDATED,
              (odv, ndv) -> (odv == DeltaValue.ADDED) ? odv : ndv);
          }
        } else {
          this.delta.merge(attributeName, DeltaValue.ADDED,
            (odv, ndv) -> (odv == DeltaValue.ADDED) ? odv : DeltaValue.UPDATED);
        }
        this.delegate.setAttribute(attributeName, value(attributeValue));
        if (PRINCIPAL_NAME_INDEX_NAME.equals(attributeName) || SPRING_SECURITY_CONTEXT.equals(attributeName)) {
          this.changed = true;
        }
        flushIfRequired();
      }

      @Override public void removeAttribute(String attributeName) { setAttribute(attributeName, null); }
      @Override public Instant getCreationTime() { return this.delegate.getCreationTime(); }
      @Override public void setLastAccessedTime(Instant lastAccessedTime) {
        this.delegate.setLastAccessedTime(lastAccessedTime);
        this.changed = true;
        flushIfRequired();
      }
      @Override public Instant getLastAccessedTime() { return this.delegate.getLastAccessedTime(); }
      @Override public void setMaxInactiveInterval(Duration interval) {
        this.delegate.setMaxInactiveInterval(interval);
        this.changed = true;
        flushIfRequired();
      }
      @Override public Duration getMaxInactiveInterval() { return this.delegate.getMaxInactiveInterval(); }
      @Override public boolean isExpired() { return this.delegate.isExpired(); }
      private void flushIfRequired() { if (CustomSessionRepository.this.flushMode == FlushMode.IMMEDIATE) { save(); } }
      private void save() {
        if (this.isNew) {
          CustomSessionRepository.this.tdc.executeWithoutResult(st -> {
            Map<String, String> indexes = CustomSessionRepository.this.indexResolver
              .resolveIndexesFor(CustomSession.this);
            CustomSessionRepository.this.dbc
              .update(CustomSessionRepository.this.createSessionQuery, ps -> {
                ps.setString(1, CustomSession.this.primaryKey);
                ps.setString(2, getId());
                ps.setLong(3, getCreationTime().toEpochMilli());
                ps.setLong(4, getLastAccessedTime().toEpochMilli());
                ps.setInt(5, (int) getMaxInactiveInterval().getSeconds());
                ps.setLong(6, getExpiryTime().toEpochMilli());
                ps.setString(7, indexes.get(PRINCIPAL_NAME_INDEX_NAME));
              });
            Set<String> names = getAttributeNames();
            if (!names.isEmpty()) {
              insertSessionAttributes(CustomSession.this, new ArrayList<>(names));
              if (alistener != null) {
                for (String name : names) {
                  alistener.attributeAdded(new HttpSessionBindingEvent(
                    new CustomSessionWrapper(CustomSession.this), name,
                    CustomSession.this.getAttribute(name)));
                }
              }
            }
          });
        } else {
          CustomSessionRepository.this.tdc.executeWithoutResult(st -> {
            // log.debug("UPDATE-SESSION:{}", getId());
            if (CustomSession.this.changed) {
              Map<String, String> indexes = CustomSessionRepository.this.indexResolver
                .resolveIndexesFor(CustomSession.this);
              CustomSessionRepository.this.dbc
                .update(CustomSessionRepository.this.updateSessionQuery, ps -> {
                  ps.setString(1, getId());
                  ps.setLong(2, getLastAccessedTime().toEpochMilli());
                  ps.setInt(3, (int) getMaxInactiveInterval().getSeconds());
                  ps.setLong(4, getExpiryTime().toEpochMilli());
                  ps.setString(5, indexes.get(PRINCIPAL_NAME_INDEX_NAME));
                  ps.setString(6, CustomSession.this.primaryKey);
                });
            }
            List<String> names = null;
            names = CustomSession.this.delta.entrySet().stream()
              .filter(entry -> entry.getValue() == DeltaValue.ADDED).map(Map.Entry::getKey)
              .collect(Collectors.toList());
            if (!names.isEmpty()) {
              insertSessionAttributes(CustomSession.this, names);
              if (alistener != null) {
                for (String name : names) {
                  alistener.attributeAdded(new HttpSessionBindingEvent(
                    new CustomSessionWrapper(CustomSession.this), name,
                    CustomSession.this.getAttribute(name)));
                }
              }
            }
            names = CustomSession.this.delta.entrySet().stream()
              .filter(entry -> entry.getValue() == DeltaValue.UPDATED).map(Map.Entry::getKey)
              .collect(Collectors.toList());
            if (!names.isEmpty()) {
              updateSessionAttributes(CustomSession.this, names);
              if (alistener != null) {
                for (String name : names) {
                  alistener.attributeReplaced(new HttpSessionBindingEvent(
                    new CustomSessionWrapper(CustomSession.this), name,
                    CustomSession.this.getAttribute(name)));
                }
              }
            }
            names = CustomSession.this.delta.entrySet().stream()
              .filter(entry -> entry.getValue() == DeltaValue.REMOVED).map(Map.Entry::getKey)
              .collect(Collectors.toList());
            if (!names.isEmpty()) {
              deleteSessionAttributes(CustomSession.this, names);
              if (alistener != null) {
                for (String name : names) {
                  alistener.attributeRemoved(new HttpSessionBindingEvent(
                    new CustomSessionWrapper(CustomSession.this), name,
                    CustomSession.this.getAttribute(name)));
                }
              }
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
      @Override public T get() { return this.value == null ? (this.value = supplier.get()) : this.value; }
    };
    return (supplier != null) ? lazySupplier : null;
  }

  public static class CustomSessionWrapper implements HttpSession {
    private CustomSessionRepository.CustomSession s;
    public CustomSessionWrapper(CustomSessionRepository.CustomSession s) { this.s = s; }
    public String findLoginId(String loginId) { return repo.get().findLoginId(loginId, this.s.getId()); }
    public void deleteSessionAttributeAll(String dupId) { repo.get().deleteSessionAttributeAll(dupId); }
    public void deleteSessionById(String dupId) { repo.get().deleteSessionById(dupId); }
    public void updateLoginId(String loginId) { repo.get().updateLoginId(loginId, this.s.getId()); }
    @Override public long getCreationTime() { return s.getCreationTime().toEpochMilli(); }
    @Override public String getId() { return s.getId(); }
    @Override public long getLastAccessedTime() { return s.getLastAccessedTime().toEpochMilli(); }
    @Override public ServletContext getServletContext() { return s.sctx; }
    @Override public void setMaxInactiveInterval(int interval) { s.setMaxInactiveInterval(Duration.ofMillis(interval)); }
    @Override public int getMaxInactiveInterval() { return parseInt(s.getMaxInactiveInterval().toMillis()); }
    @Deprecated @Override public HttpSessionContext getSessionContext() { return null; }
    @Override public Object getAttribute(String name) { return s.getAttribute(name); }
    @Override public Object getValue(String name) { return getAttribute(name); }
    @Override public Enumeration<String> getAttributeNames() { return new IteratorEnumeration<>(s.getAttributeNames().iterator()); }
    @Override public String[] getValueNames() { return cast(s.getAttributeNames().toArray(), String[].class); }
    @Override public void setAttribute(String name, Object value) { s.setAttribute(name, value); }
    @Override public void putValue(String name, Object value) { setAttribute(name, value); }
    @Override public void removeAttribute(String name) { s.removeAttribute(name); }
    @Override public void removeValue(String name) { removeAttribute(name); }
    @Override public void invalidate() { repo.get().deleteById(this.getId()); }
    @Override public boolean isNew() { return s.isNew(); }
  }
}