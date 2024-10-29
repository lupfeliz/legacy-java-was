/**
 * @File        : DbDateHandler.java
 * @Author      : 정재백
 * @Since       : 2024-04-16 
 * @Description : Mybatis 날자 형태 핸들러
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbDateHandler extends BaseTypeHandler<Date> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int inx, Date prm, JdbcType typ) throws SQLException {
    Timestamp ts = null;
    if (prm != null) {
      ts = new Timestamp(prm.getTime() - Settings.instance.getSystemTimeDiff());
    }
    ps.setObject(inx, ts);
  }

  @Override
  public Date getNullableResult(ResultSet rs, String cnm) throws SQLException {
    Date ret = null;
    Timestamp ts = rs.getTimestamp(cnm);
    log.trace("TIMESTAMP1[{}]:{}", cnm, ts);
    if (ts != null) {
      ret = new Date(ts.getTime() + Settings.instance.getSystemTimeDiff());
    }
    return ret;
  }

  @Override
  public Date getNullableResult(ResultSet rs, int cinx) throws SQLException {
    Date ret = null;
    Timestamp ts = rs.getTimestamp(cinx);
    log.trace("TIMESTAMP2[{}]:{}", cinx, ts);
    if (ts != null) {
      ret = new Date(ts.getTime() + Settings.instance.getSystemTimeDiff());
    }
    return ret;
  }

  @Override
  public Date getNullableResult(CallableStatement cs, int cinx) throws SQLException {
    Date ret = null;
    Timestamp ts = cs.getTimestamp(cinx);
    log.trace("TIMESTAMP3[{}]:{}", cinx, ts);
    if (ts != null) {
      ret = new Date(ts.getTime() + Settings.instance.getSystemTimeDiff());
    }
    return ret;
  }
}
