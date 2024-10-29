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

  /** Primary키를 표기하기 위한 어노테이션 (특별한 기능은 없음) */
  @Target({METHOD, FIELD})
  @Retention(RetentionPolicy.SOURCE)
  public static @interface PrimaryKey { }

  @Target({METHOD, FIELD, TYPE})
  @Retention(RetentionPolicy.RUNTIME) 
  public static @interface SecureOut { }

  @Target({METHOD, FIELD, TYPE})
  @Retention(RetentionPolicy.RUNTIME) 
  public static @interface IgnoreOut { }

  /** 관리자 전용을 표기하기 위한 어노테이션 (특별한 기능은 없음) */
  @Target({METHOD, FIELD})
  @Retention(RetentionPolicy.SOURCE)
  public static @interface AdminOnly { }

  /** 파일 호환 인터페이스 */
  public static interface FileInterface {
    String getOrginlFileNm();
    void setOrginlFileNm(String v);

    String getStreFileNm();
    void setStreFileNm(String v);

    String getStreFlpth();
    void setStreFlpth(String v);

    String getEventn();
    void setEventn(String v);

    Integer getSize();
    void setSize(Integer v);

    String getCn();
    void setCn(String v);
  }

  public static interface DateUpdatable {
    Date getRgsde();
    void setRgsde(Date v);
    Date getLastUpdde();
    void setLastUpdde(Date v);
  }

  public static interface UpdusrId {
    String getRegisterId();
    void setRegisterId(String v);
    String getLastUpdusrId();
    void setLastUpdusrId(String v);
  }

  public static interface Search {
    String getKeyword();
    void setKeyword(String v);

    Integer getRowStart();
    void setRowStart(Integer v);

    Integer getRowCount();
    void setRowCount(Integer v);
  }

  @Schema(title = "검색질의 파라메터 (Search)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder
  public static class SearchResult<T> implements Search {
    @Schema(title = "검색키워드")
    public String keyword;
    @Schema(title = "검색타입")
    public String type;
    @Schema(title = "검색시작")
    public Integer rowStart;
    @Schema(title = "검색수량")
    public Integer rowCount;
    @Schema(title = "한화면에 표기할 페이지갯수")
    public Integer pagePerScreen;
    @Schema(title = "결과리스트총갯수")
    public int rowTotal;
    @Schema(title = "결과리스트")
    public List<T> list;
  }

  @Schema(title = "공통 REST 결과타입 (Result)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder
  public static class Result {
    @Schema(title = "결과코드")
    public String rescd;
    @Schema(title = "오류코드")
    public String errcd;
    @Schema(title = "오류메시지")
    public String msg;
    @Schema(title = "결과데이터")
    public Object data;
  }

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

  @Schema(title = "공통 코드 분류 (TB_CODE_CL / CodeCl)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder
  public static class CodeCl {
    @Schema(title = "분류코드 (cl_cd)")
    public String clCd;
    @Schema(title = "분류명 (cl_nm)")
    public String clNm;
    @Schema(title = "분류설명 (cl_dc)")
    public String clDc;
    @Schema(title = "생성일시 (creat_dt)")
    public String creatDt;
    @Schema(title = "최종수정일시 (last_up_dt)")
    public String lastUpDt;
  }

  @Schema(title = "공통 파일 타입 (TB_CMMN_FILE / CmmnFile)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder @SecureOut
  public static class CmmnFile implements FileInterface, DateUpdatable, UpdusrId {
    @Schema(title = "파일일련번호 (file_sn)")
    public Integer fileSn;
    @Schema(title = "원본파일명 (orginl_file_nm)")
    public String orginlFileNm;
    @Schema(title = "저장파일명 (stre_file_nm)")
    @SecureOut public String streFileNm;
    @Schema(title = "저장파일경로 (stre_flpth)")
    @SecureOut public String streFlpth;
    @Schema(title = "확장자명 (eventn)")
    public String eventn;
    @Schema(title = "사이즈 (size)")
    public Integer size;
    @Schema(title = "내용 (cn)")
    @SecureOut public String cn;
    @Schema(title = "필드명 (fieldNm)")
    @SecureOut public String fieldNm;
    @Schema(title = "마스킹여부 (masking_at)")
    @SecureOut public String maskingAt;
    @Schema(title = "옵션1 (option1)")
    @SecureOut public String option1;
    @Schema(title = "옵션2 (option2)")
    @SecureOut public String option2;
    @Schema(title = "옵션3 (option3)")
    @SecureOut public String option3;
    @Schema(title = "연관테이블 (relate_table)")
    @SecureOut public String relateTable;
    @Schema(title = "연관테이블기본키 (relate_table_pk)")
    @SecureOut public String relateTablePk;
    @Schema(title = "등록자ID (register_id)")
    @SecureOut public String registerId;
    @Schema(title = "등록일 (rgsde)")
    @SecureOut public Date rgsde;
    @Schema(title = "최종수정자ID (last_updusr_id)")
    @SecureOut public String lastUpdusrId;
    @Schema(title = "최종수정일 (last_updde)")
    @SecureOut public Date lastUpdde;
    @Schema(title = "파일일련번호목록(조회용)", hidden = true) public Object fileSnSc;
  }

  @Schema(title = "시스템정보")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder @SecureOut
  public static class SystemInfo {
    public String iaddress;
    public String paddress;
    public Integer port;
    public DeployInfo deploy;
    public ClientInfo client;
    public LogInfo log;
  }

  @Schema(title = "배포정보")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder @SecureOut
  public static class DeployInfo {
    public String type;
    public String data;
  }

  @Schema(title = "접속제한")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder @SecureOut
  public static class ClientInfo {
    public IpAddressInfo allow;
    public IpAddressInfo deny;
  }

  @Schema(title = "IP주소정보")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder @SecureOut
  public static class IpAddressInfo {
    public String address;
    public List<String> addressList;
  }

  @Schema(title = "로그정보")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder @SecureOut
  public static class LogInfo {
    public String type;
    public String path;
    public String nameptn;
  }


  @Schema(title = "관리자정보 (TB_MNGR / Mngr)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder @SecureOut
  public static class Mngr implements DateUpdatable, UpdusrId, Search {
    @Schema(title = "관리자ID")
    @PrimaryKey public String mngrId;
    @Schema(title = "관리자명")
    public String mngrNm;
    @Schema(title = "비밀번호")
    @SecureOut public String password;
    @Schema(title = "연락처")
    public String cttpc;
    @Schema(title = "이메일")
    public String email;
    @Schema(title = "등록일")
    public Date rgsde;
    @Schema(title = "등록자")
    @SecureOut public String registerId;
    @Schema(title = "최종수정자")
    @SecureOut public String lastUpdusrId;
    @Schema(title = "최종수정일")
    public Date lastUpdde;
    @Schema(title = "상태, 01:정상, 99:삭제")
    @SecureOut public String sttus;

    /** 테이블 외 정보 */
    @Schema(title = "소속그룹목록", hidden = true)
    public List<Group> groupList;
    @Schema(title = "검색키워드", hidden = true)
    public String keyword;
    @Schema(title = "검색시작번호", hidden = true)
    public Integer rowStart;
    @Schema(title = "검색갯수", hidden = true)
    public Integer rowCount;
    @Schema(title = "날자검색조건", hidden = true)
    public Object dateSc;
  }

  @Schema(title = "메뉴정보 (TB_MENU / Menu)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder @SecureOut
  public static class Menu implements Search {
    @Schema(title = "메뉴일련번호")
    @PrimaryKey public Integer menuSn;
    @Schema(title = "메뉴명")
    public String menuNm;
    @Schema(title = "메뉴URL")
    public String menuUrl;
    @Schema(title = "메뉴설명")
    public String menuDc;
    @Schema(title = "숨김여부")
    public String hideAt;
    @Schema(title = "노출순번")
    public Integer sn;

    /** 테이블 외 정보 */
    @Schema(title = "메뉴권한정보", hidden = true)
    public List<MenuAuthor> authorList;
    @Schema(title = "관련그룹정보", hidden = true)
    public List<Group> groupList;
    @Schema(title = "검색키워드", hidden = true)
    public String keyword;
    @Schema(title = "검색시작번호", hidden = true)
    public Integer rowStart;
    @Schema(title = "검색갯수", hidden = true)
    public Integer rowCount;
    @Schema(title = "날자검색조건", hidden = true)
    public Object dateSc;
  }

  @Schema(title = "메뉴권한정보 (TB_MENU_AUTHOR / MenuAuthor)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder @SecureOut
  public static class MenuAuthor {
    @Schema(title = "메뉴일련번호")
    @PrimaryKey public Integer menuSn;
    @Schema(title = "그룹일련번호")
    @PrimaryKey public Integer groupSn;
    @Schema(title = "권한")
    public Integer author;
    @Schema(title = "권한설명")
    public String authorDc;

    @Schema(title = "그룹명", hidden = true)
    public String groupNm;
    @Schema(title = "검색조건", hidden = true)
    public Object menuSc;
    @Schema(title = "검색조건", hidden = true)
    public Object groupSc;
  }

  @Schema(title = "그룹사용자 (TB_GROUP_USER / GroupUser)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder @SecureOut
  public static class GroupUser {
    @Schema(title = "그룹일련번호")
    @PrimaryKey public Integer groupSn;
    @Schema(title = "관리자ID")
    @PrimaryKey public String mngrId;

    @Schema(title = "사용자명", hidden = true)
    public String mngrNm;
    @Schema(title = "사용여부", hidden = true)
    public Boolean checked;
    @Schema(title = "검색조건", hidden = true)
    public Object groupSc;
    @Schema(title = "검색조건", hidden = true)
    public Object mngrSc;
  }

  @Schema(title = "그룹 (TB_GROUP / Group)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder @SecureOut
  public static class Group implements DateUpdatable, UpdusrId, Search {
    @Schema(title = "그룹일련번호")
    public Integer groupSn;
    @Schema(title = "그룹명")
    public String groupNm;
    @Schema(title = "설명")
    public String dc;
    @Schema(title = "등록일")
    public Date rgsde;
    @Schema(title = "등록자ID")
    public String registerId;
    @Schema(title = "최종수정자ID")
    public String lastUpdusrId;
    @Schema(title = "최종수정일")
    public Date lastUpdde;

    /** 테이블 외 정보 */
    @Schema(title = "그룹사용자수", hidden = true)
    public Integer userCnt;
    @Schema(title = "그룹사용자목록", hidden = true)
    public List<GroupUser> userList;
    @Schema(title = "검색키워드", hidden = true)
    public String keyword;
    @Schema(title = "검색시작번호", hidden = true)
    public Integer rowStart;
    @Schema(title = "검색갯수", hidden = true)
    public Integer rowCount;
    @Schema(title = "검색조건", hidden = true)
    public Object groupSc;
    @Schema(title = "검색조건", hidden = true)
    public Object userSc;
    @Schema(title = "날자검색조건", hidden = true)
    public Object dateSc;
    @Schema(title = "검색조건", hidden = true)
    public Object menuSc;
  }

  @Schema(title = "관리자 로그인 기록 (TB_MNGR_LOGIN_HIST / MngrLoginHist)")
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder
  public static class MngrLoginHist {
    @Schema(title = "로그인일련번호")
    public Integer mngrLoginSn;
    @Schema(title = "사용자명")
    public String mngrId;
    @Schema(title = "로그인 성공여부")
    public String succesAt;
    @Schema(title = "접속IP")
    public String conectIp;
    @Schema(title = "로그인일시")
    public Date loginDt;
    @Schema(title = "비고")
    public String rm;
  }

  @Schema(title = "MYBATIS camelcase 매핑 (내부사용)", hidden = true)
  public static class CamelMap<K, V> extends HashMap<String, V> {
    @Override public V put(String k, V v) {
      return super.put(camelCase(k), v);
    }
  }
}