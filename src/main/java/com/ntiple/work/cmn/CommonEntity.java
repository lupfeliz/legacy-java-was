/**
 * @File        : CommonEntity.java
 * @Author      : 정재백
 * @Since       : 2024-04-16 
 * @Description : 공통적으로 사용할 DTO Entity
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.work.cmn;

import static com.ntiple.commons.ConvertUtil.camelCase;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springdoc.core.converters.models.DefaultPageable;
import org.springdoc.core.converters.models.Pageable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonEntity {

  @Target({METHOD, FIELD, TYPE})
  @Retention(RetentionPolicy.RUNTIME) 
  public static @interface SecureOut { }

  @Target({METHOD, FIELD, TYPE})
  @Retention(RetentionPolicy.RUNTIME) 
  public static @interface IgnoreOut { }

  @Schema(title = "로그인 요청 파라메터 (Login)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder
  public static class Login {
    @Schema(title = "사용자ID")
    private String userId;
    @Schema(title = "비밀번호")
    private String passwd;
  }

  @AllArgsConstructor @NoArgsConstructor
  @Getter @Setter @ToString @Builder
  public static class AuthInfo {
    private String ipAddr;
    private String userNm;
    private Boolean isAdmin;
  }

  @Schema(title = "로그인 응답 타입 (AuthResult)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder @SecureOut
  public static class AuthResult {
    @Schema(title = "사용자 ID", hidden = true)
    @SecureOut private String userId;
    @Schema(title = "사용자 이름", hidden = true)
    @SecureOut private String userNm;
    @Schema(title = "응답코드")
    private String rescd;
    @Schema(title = "응답타입")
    private String restyp;
    @Schema(title = "액세스 토큰", hidden = true)
    @SecureOut private String accessToken;
    @Schema(title = "리프레시 토큰", hidden = true)
    @SecureOut private String refreshToken;
  }

  @Schema(title = "검색결과 (SearchEntity)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder
  public static class SearchEntity<T> {
    @Schema(title = "검색타입")
    private String searchType;
    @Schema(title = "검색키워드")
    private String keyword;
    @Schema(title = "검색시작")
    private Integer rowStart;
    @Schema(title = "검색수량")
    private Integer rowCount;
    @Schema(title = "한화면에 표기할 페이지갯수")
    private Integer pagePerScreen;
    @Schema(title = "정렬타입")
    private String orderType;
    @Schema(title = "결과리스트총갯수")
    private int rowTotal;
    @NotBlank @Schema(title = "결과리스트")
    private List<T> list;

    public Pageable getPageable() {
      int page = 0;
      int rowStart = this.rowStart;
      int rowCount = this.rowCount;
      if (rowStart > 0 && rowCount > 0) { page = rowStart / rowCount; }
      if (rowCount < 1) { rowCount = 1; }
      if (page < 1) { page = 0; }
      log.debug("PAGE:{} / COUNT:{} / START:{} / COUNT:{}", page, rowCount, this.rowStart, this.rowCount);
      Pageable ret = new DefaultPageable(page, rowCount, new ArrayList<>());
      return ret;
    }
  }

  @Schema(title = "공통 REST 결과타입 (Result)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder
  public static class Result {
    @NotBlank @Size(min = 1, max = 20)
    @Schema(title = "결과코드")
    private String rescd;
    @Schema(title = "오류코드")
    private String errcd;
    @Schema(title = "오류메시지")
    private String msg;
    @Schema(title = "결과데이터")
    private Object data;
  }

  @Schema(title = "공통 환경변수 결과타입")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder
  public static class InitObj {
    @Schema(title = "서버현재시간 (Unix-Time / 시간동기화 용)")
    private Date current;
    @Schema(title = "지원언어 (KO_KR / EN_US)")
    private String locale;
    @Schema(title = "서버 인코딩(UTF-8)")
    private String encoding;
    @Schema(title = "액세스토큰 만료기간")
    private Long expirecon;
    @Schema(title = "암호동기화용 토큰")
    private String check;
  }

  @Getter @Setter @ToString
  public static class AuthDetail {
    private AuthExtra extraInfo;
  }

  @Getter @Setter @ToString
  public static class AuthExtra {
    private String ipAddr;
    private String userNm;
  }

  /** TB_CODE */
  @Schema(title = "공통코드 (TB_CODE / Code)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder
  public static class Code {
    @Schema(title = "분류코드 (cl_cd)")
    public String clCd;
    @Schema(title = "코드 (cd)")
    public String cd;
    @Schema(title = "일련번호 (sn)")
    public String sn;
    @Schema(title = "코드명 (cd_nm)")
    public String cdNm;
    @Schema(title = "코드설명 (cd_dc)")
    public String cdDc;
    @Schema(title = "깊이 (dp)")
    public Integer dp;
    @Schema(title = "상위코드 (parnts_cd)")
    public String parntsCd;
    @Schema(title = "생성일시 (creat_dt)")
    public String creatDt;
    @Schema(title = "최종수정일시 (last_up_dt)")
    public String lastUpDt;

    /** 조회용 */
    @Schema(hidden = true) public Object clCdLst;
    @Schema(hidden = true) public Object cdLst;
  }

  @Schema(title = "MYBATIS camelcase 매핑 (내부사용)", hidden = true)
  public static class CamelMap<K, V> extends HashMap<String, V> {
    @Override public V put(String k, V v) {
      return super.put(camelCase(k), v);
    }
  }
}