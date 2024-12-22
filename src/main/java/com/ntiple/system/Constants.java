/**
 * @File        : Constants.java
 * @Author      : 정재백
 * @Since       : 2024-10-29
 * @Description : 시스템 전역 상수
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import static com.ntiple.commons.ConvertUtil.convert;
import static com.ntiple.commons.ConvertUtil.newMap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

public class Constants {
  public static final String STATIC_ACCESS = "static-access";
  public static final String PROF_LOCAL = "local";
  public static final String PROF_DEV = "dev";
  public static final String PROF_TEST = "test";
  public static final String PROF_PROD = "prod";
  public static final String PROF_MY = "my";
  public static final String PTH_SWAGGER = "/swagger/swagger-ui";
  public static final String PTH_V3_APIDOCS = "/swagger/v3/api-docs";
  public static final String RESCD_FAIL = "9999";
  public static final String RESCD_OK = "0000";
  public static final String RESCD = "rescd";

  public static final String USER_AGENT = "User-Agent";
  public static final String AUTH = "auth";
  public static final String PRFX_ROLE = "ROLE_";
  public static final String USER = "USER";
  public static final String ADMIN = "ADMIN";
  public static final String TEMP = "TEMP";
  public static final String PASSWORD_EMPTY = "password-emmpty";
  public static final String NOT_ALLOWED = "not-allowed";
  public static final String SUPER_ADMIN_GRP_NM = "통합관리자";
  public static final String SUPER_ADMIN_GRP = "1";
  public static final String AUTH_READ_ONLY = "1";
  public static final String AUTH_READ_WRITE = "2";

  public static final HttpStatus SC_OK = HttpStatus.OK;
  public static final HttpStatus SC_BAD_REQUEST = HttpStatus.BAD_REQUEST;
  public static final HttpStatus SC_UNAUTHORIZED = HttpStatus.UNAUTHORIZED;
  public static final HttpStatus SC_FORBIDDEN = HttpStatus.FORBIDDEN;
  public static final HttpStatus SC_NOT_FOUND = HttpStatus.NOT_FOUND;
  public static final HttpStatus SC_METHOD_NOT_ALLOWED = HttpStatus.METHOD_NOT_ALLOWED;
  public static final HttpStatus SC_INTERNAL_SERVER_ERROR = HttpStatus.INTERNAL_SERVER_ERROR;
  public static final HttpStatus SC_BAD_GATEWAY = HttpStatus.BAD_GATEWAY;
  public static final HttpStatus SC_SERVICE_UNAVAILABLE = HttpStatus.SERVICE_UNAVAILABLE;
  public static final HttpStatus SC_GATEWAY_TIMEOUT = HttpStatus.GATEWAY_TIMEOUT;

  public static final String CONTENT_DISPOSITION = "Content-disposition";

  /** 시스템 속성 */
  public static final String SYSPROP = "SYSPROP";

  public static final String CMMN = "cmmn";

  public static final String COMMA = ",";
  public static final String I = "/";
  public static final String NL = "\n";
  public static final String CRNL = "\r\n";

  public static final String C_GET = "GET";
  public static final String C_POST = "POST";
  public static final String C_PUT = "PUT";
  public static final String C_DELETE = "DELETE";

  public static final String SELECT = "SELECT";
  public static final String INSERT = "INSERT";
  public static final String UPDATE = "UPDATE";
  public static final String DELETE = "DELETE";
  public static final String CREATE = "CREATE";
  public static final String DROP = "DROP";
  public static final String ATTR_KEY_USER_ID = "@%ATTR_USERID%";
  public static final String ATTR_KEY_AUTH = "@%ATTR_AUTH%";
  public static final String ATTR_KEY_GRANT = "@%ATTR_GRANT%";
  public static final String ATTR_KEY_IS_ADMIN = "@%ATTR_IS_ADMIN%";
  public static final String ATTR_KEY_IS_USER = "@%ATTR_IS_USER%";
  public static final String ATTR_KEY_IS_GUEST = "@%ATTR_IS_GUEST%";
  public static final String ATTR_KEY_EXTRAINFO = "@%ATTR_EXTRAINFO%";

  public static final String ATTR_KEY_LAYOUT_META_DEFINE = "@%ATTR_KEY_LAYOUT_META_DEFINE%";
  public static final String ATTR_KEY_LAYOUT_ASSETS_DEFINE = "@%ATTR_KEY_LAYOUT_ASSETS_DEFINE%";
  public static final String ATTR_KEY_LAYOUT_HEADER = "@%ATTR_KEY_LAYOUT_HEADER%";
  public static final String ATTR_KEY_LAYOUT_FOOTER = "@%ATTR_KEY_LAYOUT_FOOTER%";
  public static final String ATTR_KEY_LAYOUT_ASIDE = "@%ATTR_KEY_LAYOUT_ASIDE%";
  public static final String ATTR_KEY_LAYOUT_DCONTAINER = "@%ATTR_KEY_LAYOUT_DCONTAINER%";
  public static final String ATTR_KEY_LAYOUT_SCRIPTS = "@%ATTR_KEY_LAYOUT_SCRIPTS%";
  public static final String ATTR_KEY_LAYOUT_BODY = "@%ATTR_KEY_LAYOUT_BODY%";

  public static final String TYPE_XLSX = "xlsx";
  public static final String TYPE_XLS = "xls";

  public static class TMPCLS { }
  public static Object TMPOBJ = new TMPCLS();

  public static final Map<String, Object> EMPTY_MAP = new LinkedHashMap<>();
  public static final Map<String, Object> OK_RESULT_MAP = convert(new Object[] { RESCD, RESCD_OK }, newMap());
  public static final Map<String, Object> FAIL_RESULT_MAP = convert(new Object[] { RESCD, RESCD_FAIL }, newMap());
}
