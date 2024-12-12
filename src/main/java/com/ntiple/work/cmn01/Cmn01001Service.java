/**
 * @File        : Cmn01001Service.java
 * @Author      : 정재백
 * @Since       : 2024-10-29
 * @Description : 공통적으로 사용할 시스템 서비스
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.work.cmn01;

import static com.ntiple.commons.Constants.CTYPE_FILE;
import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.IOUtil.file;
import static com.ntiple.commons.IOUtil.istream;
import static com.ntiple.commons.IOUtil.mkdirs;
import static com.ntiple.commons.IOUtil.ostream;
import static com.ntiple.commons.IOUtil.passthrough;
import static com.ntiple.commons.IOUtil.reader;
import static com.ntiple.commons.IOUtil.safeclose;
import static com.ntiple.commons.ValuesUtil.codeSplit;
import static com.ntiple.commons.WebUtil.curResponse;
import static com.ntiple.commons.WebUtil.getUri;
import static com.ntiple.commons.WebUtil.referer;
import static com.ntiple.commons.WebUtil.remoteAddr;
import static com.ntiple.config.PersistentConfig.SQLTRANSCT_MAIN;
import static com.ntiple.system.Constants.PROF_DEV;
import static com.ntiple.system.Constants.PROF_LOCAL;
import static com.ntiple.system.Constants.STATIC_ACCESS;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ntiple.system.SystemException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntiple.commons.WebUtil;
import com.ntiple.system.Settings;
import com.ntiple.work.cmn01.Cmn01001Entity.CmmnFile;
import com.ntiple.work.cmn01.Cmn01001Entity.Code;
import com.ntiple.work.cmn01.Cmn01001Entity.DateUpdatable;
import com.ntiple.work.cmn01.Cmn01001Entity.FileInterface;
import com.ntiple.work.cmn01.Cmn01001Entity.Group;
import com.ntiple.work.cmn01.Cmn01001Entity.InitObj;
import com.ntiple.work.cmn01.Cmn01001Entity.Login;
import com.ntiple.work.cmn01.Cmn01001Entity.Menu;
import com.ntiple.work.cmn01.Cmn01001Entity.MenuAuthor;
import com.ntiple.work.cmn01.Cmn01001Entity.SecureOut;
import com.ntiple.work.cmn01.Cmn01001Entity.UpdusrId;
import com.ntiple.work.sys01.Sys01001Repository;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Service @SuppressWarnings(STATIC_ACCESS)
public class Cmn01001Service {

  private static com.ntiple.commons.ConvertUtil CU;
  private static com.ntiple.commons.StringUtil SU;
  private static com.ntiple.commons.ReflectionUtil RU;
  private static com.ntiple.system.Constants S;

  @Autowired Settings settings;
  @Autowired Cmn01001Repository repository;
  @Autowired Sys01001Repository systemrepo;

  boolean aliveState;

  public static Pattern PTN_ADMIN_URI_PFX = Pattern.compile("^(\\/api){0,1}(\\/admin\\/[a-z]{4}\\/[a-z]{4}[0-9]{2})[0-9]{3}");
  public static Map<String, Map<String, String>> URI_AUTH_MAP = new LinkedHashMap<>();

  private static final String FORMAT_SVC_DATE_STR = "yyyyMMddHHmmss";
  private static final SimpleDateFormat FORMAT_SVC_DATE = new SimpleDateFormat(FORMAT_SVC_DATE_STR);

  private static final Pattern PTN_COLUMN_DESC = Pattern.compile("\\s+([a-zA-Z0-9_-]+)$");

  static Map<String, List<Code>> cachedCode = new LinkedHashMap<>();

  static Cmn01001Service instance;

  @PostConstruct public void init() throws Exception {
    log.trace("INIT {}", Cmn01001Service.class);
    instance = this;

    aliveState = true;

    /** 자주 사용하는 코드 */
    try {
      // List<Code> lst = findCode(Code.builder()
      //   .clCd(C.HNFSTTUS)
      //   .build());
      // cachedCode.put(C.HNFSTTUS, lst);
    } catch (Exception ignore) { }
    // refreshURLAuthorities();
    // settings.reloadYmlSettings();
    // overloadSettings();
  }

  @Transactional(SQLTRANSCT_MAIN)
  public void refreshURLAuthorities() throws Exception {
    /** URL 권한정보 */
    URI_AUTH_MAP.clear();
    List<Menu> menuList = null;
    List<MenuAuthor> authList = null;
    Map<String, String> authMap;
    Date current = curDate();
    try {
      Group sag = systemrepo.getGroup(
        Group.builder().groupSn(CU.parseInt(S.SUPER_ADMIN_GRP)).build());
      if (sag == null) {
        systemrepo.saveGroup(Group.builder()
          .groupSn(CU.parseInt(S.SUPER_ADMIN_GRP))
          .groupNm(S.SUPER_ADMIN_GRP_NM)
          .registerId(S.ADMIN)
          .lastUpdusrId(S.ADMIN)
          .rgsde(current)
          .lastUpdde(current)
        .build());
      }

      menuList = systemrepo.findMenu(Menu.builder().rowCount(-1).build());
      for (Menu menu : menuList) {
        Matcher mat = PTN_ADMIN_URI_PFX.matcher(menu.menuUrl);
        if (mat.find()) {
          String uripfx = mat.group(2);
          if ((authMap = URI_AUTH_MAP.get(uripfx)) == null) {
            URI_AUTH_MAP.put(uripfx, authMap = new LinkedHashMap<>());
          }
          authList = systemrepo.findMenuAuthor(
            MenuAuthor.builder().menuSn(menu.menuSn).build());
          for (MenuAuthor auth : authList) {
            if (auth.author > 0) {
              authMap.put(CU.parseStr(auth.groupSn), CU.parseStr(auth.author));
            }
          }
          /** 전체관리자는 기본적으로 모든 메뉴의 권한을 가진다. */
          if (!authMap.containsKey(S.SUPER_ADMIN_GRP)) {
            authMap.put(S.SUPER_ADMIN_GRP, S.AUTH_READ_WRITE);
            systemrepo.saveMenuAuthor(MenuAuthor.builder()
              .menuSn(menu.menuSn)
              .groupSn(CU.parseInt(S.SUPER_ADMIN_GRP))
              .author(CU.parseInt(S.AUTH_READ_WRITE))
              .build());
          }
        }
      }
      log.debug("URI_AUTH_MAP:{}", URI_AUTH_MAP);
    } catch (Exception e) {
      log.debug("E:", e);
    }
  }

  /**
   * 데이터베이스에서 추가적인 속성을 읽어온다 (application.yml 속성에 대응)
   **/
  public void overloadSettings () throws Exception {
    List<Code> list = repository.findCodeDetail(Code.builder().clCd(S.SYSPROP).build());
    for (Code cd : list) {
      log.debug("SYSPROPS-RELOAD:{} = {}", cd.cd, cd.cdDc);
      if (cd.cd == null || "".equals(cd.cd)) { continue; }
      if (cd.cdDc == null || "".equals(cd.cdDc)) { continue; }
      /** NOTE: (HARD-CODING) 구조 특성상 이 부분은 하드코딩이 필수적임. */
      // switch(cd.cd) {
      // /** 관리자 불허IP (허용IP 레벨이 더 높음) */
      // case "system.adm.client.deny.address": {
      //   IpAddressInfo ipinfo = IpAddressInfo.builder().address(cd.cdDc).build();
      //   settings.parseClientAddr(ipinfo);
      //   settings.getAdmInfo().client.deny.addressList.addAll(ipinfo.addressList);
      //   log.debug("SYSTEM.ADM.CLIENT.DENY.ADDRESS:{}", settings.getAdmInfo().client.deny.addressList);
      // } break;
      // /** 관리자 허용IP */
      // case "system.adm.client.allow.address": {
      //   IpAddressInfo ipinfo = IpAddressInfo.builder().address(cd.cdDc).build();
      //   settings.parseClientAddr(ipinfo);
      //   settings.getAdmInfo().client.allow.addressList.addAll(ipinfo.addressList);
      //   log.debug("SYSTEM.ADM.CLIENT.ALLOW.ADDRESS:{}", settings.getAdmInfo().client.allow.addressList);
      // } break;
      // }
    }
  }

  public boolean isAlive() {
    return aliveState;
  }

  public void setAlive(boolean aliveState) {
    this.aliveState = aliveState;
  }

  public boolean isAllowedClient() throws Exception {
    boolean ret = true;
    HttpServletRequest req = curRequest();
    String ipaddr = remoteAddr(req);
    String referer = referer(req);
    String refuri = getUri(referer, settings.getHostNames());

    switch (settings.getProfile()) {
    case PROF_LOCAL:
    case PROF_DEV:
      // Iterator<String> iter = req.getHeaderNames().asIterator();
      // while (iter.hasNext()) {
      //   String key = iter.next();
      //   log.debug("CHECK-HEADER:{} = {}", key, req.getHeader(key));
      // }
    default: break;
    }
    
    log.debug("CHECK-IP:{}, {}, {}, {}, {} / {}", ret, ipaddr, refuri, referer, req.getRequestURL(), settings.getAdmInfo().client.allow.addressList);
    if (ipaddr == null || "".equals(ipaddr)) { return false; }
    if (refuri != null && !"".equals(refuri)) {
    } else {
      ret = false;
    }
    if (!ret) { return false; }
    return true;
  }

  public boolean checkIp(String ipaddr, boolean ret) throws Exception {
    // List<String> addressList = null;
    // /** IP허용목록 체크 */
    // boolean allowed = true;
    // /** 불허목록 체크 */
    // addressList = settings.getAdmInfo().client.deny.addressList;
    // if (addressList != null && addressList.size() > 0) {
    //   boolean found = false;
    //   LOOP:
    //   for (String address : addressList) {
    //     // log.debug("COMPARE-DENY:'{}' - '{}' / {}", address, ipaddr, checkIpMatch(ipaddr, address));
    //     if (address != null && checkIpMatch(ipaddr, address)) {
    //       found = true;
    //       break LOOP;
    //     }
    //   }
    //   if (found) { allowed = false; }
    // }
    // /** 허용목록 체크 */
    // addressList = settings.getAdmInfo().client.allow.addressList;
    // if (addressList != null && addressList.size() > 0) {
    //   boolean found = false;
    //   LOOP:
    //   for (String address : addressList) {
    //     // log.debug("COMPARE-ALLOW:'{}' - '{}' / {}", address, ipaddr, checkIpMatch(ipaddr, address));
    //     if (address != null && checkIpMatch(ipaddr, address)) {
    //       found = true;
    //       break LOOP;
    //     }
    //   }
    //   if (found) { allowed = true; }
    //   if (!allowed) { ret = false; }
    // }
    // log.debug("ALLOW-IP: {} / {}", ipaddr, ret);
    return ret;
  }

  public InitObj getEnv() throws Exception {
    if (!isAllowedClient()) {
      HttpServletRequest req = curRequest();
      log.debug("NOT-ALLOWED: {} / {} / {}", remoteAddr(req), getUri(referer(req), settings.getHostNames()), settings.getAdmInfo().client.allow);
      throw new SystemException(999, S.NOT_ALLOWED, S.SC_FORBIDDEN);
    }

    long timestamp = curTime();
    return InitObj.builder()
      .current(new Date(timestamp))
      // .locale(C.KOKR)
      .encoding(UTF8)
      // .expirecon(settings.exptAcc)
      // .check(settings.enc(String.valueOf(timestamp), true))
      .build();
  }

  public List<Code> findCode(Code prm) throws Exception {
    log.debug("PRM:{}", prm);
    if (prm.clCdLst != null && prm.clCdLst instanceof String) {
      prm.clCdLst = codeSplit(prm.clCdLst, S.COMMA);
    }
    if (prm.cdLst != null && prm.cdLst instanceof String) {
      prm.cdLst = codeSplit(prm.cdLst, S.COMMA);
    }
    return repository.findCode(prm);
  }

  public List<Code> findCodeDetail(Code prm) throws Exception {
    log.debug("PRM:{}", prm);
    if (prm.clCdLst != null && prm.clCdLst instanceof String) {
      prm.clCdLst = codeSplit(prm.clCdLst, S.COMMA);
    }
    if (prm.cdLst != null && prm.cdLst instanceof String) {
      prm.cdLst = codeSplit(prm.cdLst, S.COMMA);
    }
    return repository.findCodeDetail(prm);
  }
  
  public static Code getCode(String clCd, String cd) throws Exception {
    Code ret = null;
    if (instance == null) { return ret; }
    List<Code> lst = null;
    if (cachedCode != null && cachedCode.containsKey(clCd)) {
      lst = cachedCode.get(clCd);

    } else {
      lst = instance.repository.findCode(
        Code.builder()
          .clCd(clCd)
          .cd(cd)
          .build()
      );
    }
    if (lst.size() == 1) {
      ret = lst.get(0);
    } else if (lst.size() > 0) {
      for (Code itm : lst) {
        if (cd != null && cd.equals(itm.cd)) {
          ret = itm;
          break;
        }
      }
    }
    return ret;
  }

  @Transactional(SQLTRANSCT_MAIN)
  public Object saveCode(Code prm) throws Exception {
    List<Code> list = repository.findCode(prm);
    Code code = null;
    if (list.size() == 0) {
      repository.saveCode(autoFill(prm));
      code = prm;
    } else if (list.size() > 0) {
      code = list.get(0);
      repository.updateCode(autoFill(prm), prm.clCd, prm.cd);
    }
    return code;
  }

  @Transactional(SQLTRANSCT_MAIN)
  public Object updateCode(Code prm, String clCd, String cd) throws Exception {
    return repository.updateCode(autoFill(prm), clCd, cd);
  }

  public String dbEncrypt(String value) throws Exception {
    return repository.dbEncrypt(value, settings.getDbcCipher(), settings.getDbcSecret(), settings.getDbcEncode(), settings.getDbcCharset());
  }

  public String dbDecrypt(String value) throws Exception {
    return repository.dbDecrypt(value, settings.getDbcCipher(), settings.getDbcSecret(), settings.getDbcEncode(), settings.getDbcCharset());
  }

  public Date dbCurrent() throws Exception {
    return repository.dbCurrent();
  }

  public long curTime() throws Exception {
    /** 
     * DB 시간을 기준으로 할지 WAS 시간을 기준으로 할지 통일화 하기 위한 메소드
     */
    long ret = 0;
    /** DB시간기준(서비스 초기화 이후에 정상작동) */
    // ret = instance.dbCurrent().getTime();
    /** WAS시간기준 */
    ret = System.currentTimeMillis();
    return ret;
  }

  public Date curDate() throws Exception {
    return new Date(curTime());
  }

  public CmmnFile getCmmnFile(CmmnFile prm) throws Exception {
    return repository.getCmmnFile(prm);
  }

  public List<CmmnFile> findCmmnFile(CmmnFile prm) throws Exception {
    return repository.findCmmnFile(prm);
  }

  public Integer deleteCmmnFile(CmmnFile prm) throws Exception {
    File file = null;
    log.debug("DELETE-CMMN-FILE1:{}", prm);
    if (prm != null && prm.fileSn != null) {
      if (!(prm.streFlpth != null && !"".equals(prm.streFlpth)
        && prm.streFileNm != null && !"".equals(prm.streFileNm))) {
        prm = repository.getCmmnFile(prm);
      }
      if ((file = getRealFile(prm)) != null && file.exists()) {
        deleteFile(file, prm);
      }
    } else if (prm != null && prm.fileSnSc != null) {

    }
    log.debug("DELETE-CMMN-FILE2:{}", prm);
    return repository.deleteCmmnFile(prm);
  }

  public File getRealFile(CmmnFile prm) throws Exception {
    if (prm != null) {
      prm.size = prm.size;
      prm.orginlFileNm = prm.orginlFileNm;
      File base = file(settings.getStoragePath(), S.CMMN);
      File ofile = file(base, String.valueOf(prm.fileSn));
      mkdirs(base);
      return ofile;
    }
    return null;
  }

  public File getRealFile(CmmnFile prm, String tbName, String pk) throws Exception {
    CmmnFile cfile = getCmmnFile(CmmnFile.builder()
      .relateTable(tbName)
      .relateTablePk(pk)
    .build());
    if (cfile != null) {
      prm.size = cfile.size;
      prm.orginlFileNm = cfile.orginlFileNm;
      File base = file(settings.getStoragePath(), S.CMMN);
      File ofile = file(base, String.valueOf(cfile.fileSn));
      mkdirs(base);
      return ofile;
    }
    return null;
  }

  public CmmnFile saveCmmnFile(MultipartFile mfile, String tbName, String pk, String cn, String fieldNm) throws Exception {
    CmmnFile cfile = null;
    InputStream istream = null;
    OutputStream ostream = null;
    cfile = repository.getCmmnFile(
      /** FIXME: 임시! 테이블명 과 PK로 선조사 */
      CmmnFile.builder()
        .relateTable(tbName)
        .relateTablePk(pk)
      .build()
    );
    log.debug("FIND-FILE:{}", cfile);

    try {
      if (cfile != null)  {
        /** UPDATE FILE */
        cfile = CU.convert(CmmnFile.builder()
          .cn(cn)
          .fieldNm(fieldNm)
          .relateTable(tbName)
          .relateTablePk(pk)
          .build(), cfile);
        uploadFile(CU.array(S.CMMN, CU.parseStr(cfile.fileSn)), mfile, cfile);
        repository.updateCmmnFile(autoFill(cfile));
      } else {
        /** SAVE FILE */
        /** 물리파일을 저장한다 */
        Integer fileSn = repository.incCmmnFileSn();
        cfile = CmmnFile.builder()
          .fileSn(fileSn)
          .cn(cn)
          .fieldNm(fieldNm)
          .relateTable(tbName)
          .relateTablePk(pk)
          .build();
        uploadFile(CU.array(S.CMMN, CU.parseStr(fileSn)), mfile, cfile);
        repository.saveCmmnFile(autoFill(cfile));
      }
    } finally {
      safeclose(istream);
      safeclose(ostream);
    }
    return cfile;
  }

  public Object downloadCmmnFile(Integer fileSn) throws Exception {
    Object ret = null;
    CmmnFile cfile = null;
    try {
      cfile = repository.getCmmnFile(CmmnFile.builder().fileSn(fileSn).build());
      if (cfile != null) {
        File file = file(settings.getStoragePath(), S.CMMN, CU.parseStr(fileSn));
        // mkdirs(file.getParentFile());
        if (file.exists()) {
          downloadFile(file, cfile);
        }
      }
    } catch (Exception e) {
      if (e instanceof ClientAbortException) {
        HttpServletRequest req = curRequest();
        log.debug("CLIENT-ABORTED!! {} / {} / {}", remoteAddr(req), req.getRequestURI(), getCurrentUserId(req));
      } else {
        log.error("", e);
      }
    }
    return ret;
  }

  public boolean uploadFile(String[] path, MultipartFile mprt, FileInterface info) throws Exception {
    File ofile = file(instance.settings.getStoragePath(), path);
    return uploadFile(ofile, mprt, info);
  }

  private static boolean uploadFile(File file, MultipartFile mprt, FileInterface info) throws Exception {
    InputStream istream = null;
    OutputStream ostream = null;
    boolean ret = false;
    File parent = file.getParentFile();
    File mfile = file(parent, SU.cat(file.getName(), ".meta"));
    String oname = "";
    String ext = "";
    int size = 0;
    try {
      if (!parent.exists()) { mkdirs(parent); }
      oname = mprt.getOriginalFilename();
      ext = "";
      Pattern ptn = Pattern.compile("[.]([a-zA-Z0-9_-]+)$");
      Matcher mat = ptn.matcher(oname);
      if (mat.find()) { ext = mat.group(1).toLowerCase(); }
      istream = mprt.getInputStream();
      ostream = ostream(file);
      size = passthrough(istream, ostream);
    } finally {
      safeclose(istream);
      safeclose(ostream);
    }

    try {
      /** 메타파일 */
      String root = file(instance.settings.getStoragePath()).getAbsolutePath();
      String path = file.getParentFile().getAbsolutePath();
      log.debug("CHECK-PATH:{} / {} / {}", path.startsWith(root), root, path);
      if (path.startsWith(root)) { path = path.substring(root.length()); }
      if (info != null) {
        CU.convert(CmmnFile.builder()
          .orginlFileNm(oname)
          .streFileNm(file.getName())
          .streFlpth(path)
          .eventn(ext)
          .size(size)
          .build(), info);
        ostream = ostream(mfile);
        byte[] data = new JSONObject(info).toString().getBytes(UTF8);
        ostream.write(data);
      }
      ret = true;
    } finally {
      safeclose(istream);
      safeclose(ostream);
    }
    return ret;
  }

  public boolean deleteFile(String[] path, FileInterface info) throws Exception {
    File ofile = file(settings.getStoragePath(), path);
    return deleteFile(ofile, info);
  }

  public static boolean deleteFile(File file, FileInterface info) throws Exception {
    InputStream istream = null;
    boolean ret = false;
    File parent = file.getParentFile();
    File mfile = file(parent, SU.cat(file.getName(), ".meta"));
    try {
      /** 메타파일 */
      if (info != null && mfile.exists()) {
        istream = istream(mfile);
        FileInterface v = readObject(istream, info.getClass());
        CU.convert(v, info);
      }
      file.delete();
      mfile.delete();
      ret = true;
    } finally {
      safeclose(istream);
    }
    return ret;
  }

  public boolean downloadFile(String[] path, FileInterface info) throws Exception {
    File ofile = file(settings.getStoragePath(), path);
    return downloadFile(ofile, info);
  }

  public boolean fileExists(String[] path, FileInterface info) {
    return file(settings.getStoragePath(), path).exists();
  }

  public static boolean downloadFile(File file, FileInterface info) throws Exception {
    boolean ret = false;
    InputStream istream = null;
    try {
      if (file.exists()) {
        if (info != null) { info.setSize(CU.parseInt(file.length())); }
        istream = istream(file);
      }
      ret = downloadFile(istream, info);
    } catch (Exception e) {
      log.error("", e);
    } finally {
      safeclose(istream);
    }
    return ret;
  }

  public static boolean downloadFile(InputStream istream, FileInterface info) throws Exception {
    boolean ret = false;
    HttpServletRequest req = curRequest();
    HttpServletResponse res = null;
    OutputStream ostream = null;
    try {
      if (istream != null) {
        if ((res = curResponse(req, HttpServletResponse.class)) != null) {
          res.setContentType(CTYPE_FILE);
          res.setCharacterEncoding(UTF8);
          if (info != null) {
            if (info.getSize() > 0) { res.setContentLength(info.getSize()); }
            if (info.getOrginlFileNm() != null && !"".equals(info.getOrginlFileNm())) {
              res.setHeader(S.CONTENT_DISPOSITION, SU.cat("attachment; filename=",
                URLEncoder.encode(info.getOrginlFileNm(), UTF8)));
            }
          }
          ostream = res.getOutputStream();
          passthrough(istream, ostream);
          ret = true;
        }
      } else {
        /** FIXME: 파일찾기 실패시 결과 출력 */
      }
    } catch (ClientAbortException e) {
      // log.debug("CLIENT-ABORTED!!:{}{}", req.getRequestURI(), instance.getCurrentUserId(req), errstack(e));
    } catch (Exception e) {
      log.error("", e);
    } finally {
      safeclose(ostream);
    }
    return ret;
  }

  public File getFile(String[] path) {
    return file(settings.getStoragePath(), path);
  }

  public static HttpServletRequest curRequest() { return WebUtil.curRequest(HttpServletRequest.class); }
  public Authentication getCurrentAuth() { return getCurrentAuth(curRequest()); }
  public Authentication getCurrentAuth(HttpServletRequest req) {
    Authentication ret = null;
    Object o = req.getAttribute(S.ATTR_KEY_AUTH);
    if (o == S.TMPOBJ) { return ret; }
    if (o != null) {
      ret = RU.cast(o, ret);
    } else {
      ret = SecurityContextHolder.getContext().getAuthentication();
      if (ret != null) {
        List<SimpleGrantedAuthority> alst = RU.cast(ret.getAuthorities(), alst = null);
        log.debug("AUTHLST:{} / {}", alst);
        req.setAttribute(S.ATTR_KEY_AUTH, ret);
        req.setAttribute(S.ATTR_KEY_USER_ID, ret.getName());
        req.setAttribute(S.ATTR_KEY_GRANT, ret.getAuthorities());
        // if ((o = ret.getDetails()) != null) {
        //   Claims claims = cast(ret.getDetails(), claims = null);
        //   ExtraInfo extrainfo = convert(claims.get(EXTRA_INFO), new ExtraInfo());
        //   // req.setAttribute(ATTR_KEY_EXTRAINFO, true);
        // } else {
        //   // req.setAttribute(ATTR_KEY_EXTRAINFO, true);
        // }
      } else {
        req.setAttribute(S.ATTR_KEY_AUTH, S.TMPOBJ);
        req.setAttribute(S.ATTR_KEY_USER_ID, S.TMPOBJ);
        req.setAttribute(S.ATTR_KEY_GRANT, S.TMPOBJ);
        req.setAttribute(S.ATTR_KEY_IS_ADMIN, S.TMPOBJ);
        req.setAttribute(S.ATTR_KEY_IS_USER, S.TMPOBJ);
        req.setAttribute(S.ATTR_KEY_EXTRAINFO, S.TMPOBJ);
        req.setAttribute(S.ATTR_KEY_IS_GUEST, true);
      }
    }
    return ret;
  }

  public List<SimpleGrantedAuthority> getCurrentGrants() { return getCurrentGrants(curRequest()); }
  public List<SimpleGrantedAuthority> getCurrentGrants(HttpServletRequest req) {
    List<SimpleGrantedAuthority> ret = null;
    Object o = req.getAttribute(S.ATTR_KEY_GRANT);
    if (o == S.TMPOBJ) { return ret; }
    if (o != null) {
      ret = RU.cast(o, ret);
    } else {
      Authentication auth = getCurrentAuth(req);
      if (auth != null) {

      }
    }
    return ret;
  }

  // public ExtraInfo getExtraInfo() { return getExtraInfo(curRequest()); }
  // public ExtraInfo getExtraInfo(HttpServletRequest req) {
  //   ExtraInfo ret = new ExtraInfo();
  //   Object o = req.getAttribute(S.ATTR_KEY_EXTRAINFO);
  //   if (o == S.TMPOBJ) { return ret; }
  //   if (o != null) {
  //     ret = CU.cast(o, ret);
  //   } else {
  //     Authentication auth = getCurrentAuth(req);
  //     if (auth != null) {
  //       Claims claims = CU.cast(auth.getDetails(), claims = null);
  //       ret = CU.convert(claims.get(C.EXTRA_INFO), ret);
  //       req.setAttribute(S.ATTR_KEY_EXTRAINFO, ret);
  //     }
  //   }
  //   return ret;
  // }

  public String getCurrentUserId() { return getCurrentUserId(curRequest()); }
  public String getCurrentUserId(HttpServletRequest req) {
    String ret = null;
    Object o = req.getAttribute(S.ATTR_KEY_USER_ID);
    if (o == S.TMPOBJ) { return ret; }
    if (o != null) {
      ret = RU.cast(o, ret);
    } else {
      Authentication auth = getCurrentAuth(req);
      if (auth != null) {
        ret = auth.getName();
        req.setAttribute(S.ATTR_KEY_USER_ID, ret);
      }
    }
    return ret;
  }

  public boolean isAdmin() { return isAdmin(curRequest()); }
  public boolean isAdmin(HttpServletRequest req) {
    Boolean ret = false;
    Object o = req.getAttribute(S.ATTR_KEY_IS_ADMIN);
    if (o == S.TMPOBJ) { return ret; }
    if (o != null) {
      ret = RU.cast(o, ret);
    } else {
      List<SimpleGrantedAuthority> grants = getCurrentGrants(req);
      if (grants != null) {
        for (SimpleGrantedAuthority grant : grants) {
          if (grant.getAuthority().equals(SU.cat(S.PRFX_ROLE, S.ADMIN))) {
            ret = true;
            req.setAttribute(S.ATTR_KEY_IS_ADMIN, ret);
          }
        }
      }
    }
    return ret;
  }

  public boolean isUser() { return isUser(curRequest()); }
  public boolean isUser(HttpServletRequest req) {
    Boolean ret = false;
    Object o = req.getAttribute(S.ATTR_KEY_IS_USER);
    if (o == S.TMPOBJ) { return ret; }
    if (o != null) {
      ret = RU.cast(o, ret);
    } else {
      List<SimpleGrantedAuthority> grants = getCurrentGrants(req);
      if (grants != null) {
        for (SimpleGrantedAuthority grant : grants) {
          if (grant.getAuthority().equals(SU.cat(S.PRFX_ROLE, S.USER))) {
            ret = true;
            req.setAttribute(S.ATTR_KEY_IS_USER, ret);
          }
        }
      }
    }
    return ret;
  }

  public boolean isGuest() { return isGuest(curRequest()); }
  public boolean isGuest(HttpServletRequest req) {
    Boolean ret = false;
    Object o = req.getAttribute(S.ATTR_KEY_IS_GUEST);
    if (o == S.TMPOBJ) { return ret; }
    if (o != null) {
      ret = RU.cast(o, ret);
    } else {
      if (!isAdmin(req) && isUser(req)) {
        ret = true;
        req.setAttribute(S.ATTR_KEY_IS_GUEST, ret);
      }
    }
    return ret;
  }

  public Integer getMaxValueFromFileName(String[] path, String ptnStr, String grp, Integer def) throws Exception {
    File base = file(settings.getStoragePath(), path);
    return getMaxValueFromFileName(base, ptnStr, grp, def);
  }

  public static Integer getMaxValueFromFileName(File base, String ptnStr, String grp, Integer def) throws Exception {
    Integer[] ret = new Integer[] { def };
    if (base != null && base.exists() && base.isDirectory()) {
      try {
        final Pattern ptn = Pattern.compile(ptnStr);
        base.listFiles(new FileFilter() {
          @Override public boolean accept(File file) {
            Matcher mat = ptn.matcher(file.getAbsolutePath());
            if (mat.find()) {
              Integer num = CU.parseInt(mat.group(grp));
              if (ret[0] == def || (num != null && num > ret[0])) {
                ret[0] = num;
              }
            }
            return false;
          }
        });
      } catch (Exception e) {
        log.debug("E:{}", e);
      }
    }
    return ret[0];
  }

  public <T> List<T> findFilesMeta(String[] path, String ptnStr, Class<T> cls) throws Exception {
    File base = file(settings.getStoragePath(), path);
    return findFilesMeta(base, ptnStr, cls);
  }

  public static <T> List<T> findFilesMeta(File base, String ptnStr, Class<T> cls) throws Exception {
    List<T> ret = new ArrayList<>();
    if (base != null && base.exists() && base.isDirectory()) {
      try {
        final Pattern ptn = Pattern.compile(ptnStr);
        base.listFiles(new FileFilter() {
          @Override public boolean accept(File file) {
            Matcher mat = ptn.matcher(file.getAbsolutePath());
            if (mat.find()) { ret.add(0, readObject(file, cls)); }
            return false;
          }
        });
      } catch (Exception e) {
        log.debug("E:{}", e);
      }
    }
    return ret;
  }

  public static <T> List<T> findFiles(Object prm) {
    List<T> ret = null;
    return ret;
  }

  public static <T> T autoFill(T prm) { return autoFill(prm, false); }
  public static <T> T autoFill(T prm, boolean addPrefix) {
    T ret = prm;
    if (ret == null) { return ret; }
    Date curdate = null;
    String userId = null;
    String prfxUserId = "";
    Boolean isAdmin = false;
    Boolean isUser = false;
    HttpServletRequest req = curRequest();
    if (instance != null) {
      userId = instance.getCurrentUserId(req);
      isAdmin = instance.isAdmin(req);
      isUser = instance.isUser(req);
      /** 관리자인 경우 id 앞에 a: 붙임 */
      if (isAdmin && addPrefix) { prfxUserId = "a:"; }
      try {
        curdate = instance.curDate();
      } catch (Exception ignore) { }
    }
    log.debug("AUTO-FILL-USER-ID:{}{}", prfxUserId, userId, isAdmin, isUser);
    if (ret instanceof DateUpdatable) {
      DateUpdatable v = RU.cast(prm, v = null);
      v.setRgsde(curdate);
      v.setLastUpdde(curdate);
    }
    // if (ret instanceof User) {
    //   User v = CU.cast(prm, v = null);
    //   v.setCreatDt(curdate);
    //   v.setLastUpDt(curdate);
    // }
    // if (ret instanceof NtcnRecptn) {
    //   NtcnRecptn v = CU.cast(prm, v = null);
    //   v.setRegistDt(curdate);
    // }
    // if (ret instanceof LoginHist) {
    //   LoginHist v = CU.cast(prm, v = null);
    //   v.setLoginDt(curdate);
    // }
    if (ret instanceof UpdusrId && userId != null) {
      UpdusrId v = RU.cast(prm, v = null);
      v.setRegisterId(SU.cat(prfxUserId, userId));
      v.setLastUpdusrId(SU.cat(prfxUserId, userId));
    }
    if (ret instanceof Login) {
      // Login v = CU.cast(prm, v = null);
      // String remoteAddr = remoteAddr(req);
      // v.setExtraInfo(ExtraInfo.builder()
      //   .remoteAddr(remoteAddr)
      //   .userAgent(req.getHeader(S.USER_AGENT))
      //   .build());
    }
    // if (ret instanceof HasHnfSn) {
    //   HasHnfSn v = cast(prm, v = null);
    //   v.setHnfSn();
    // }
    return ret;
  }

  public static <T> T secureOut(T prm) {
    if (prm == null) { return prm; }
    T ret = prm;
    Class<?> cls = prm.getClass();
    if (CU.isPrimeType(cls)) { return prm; }
    log.trace("CHECK1:{} : {}", cls.getSimpleName(), cls.isAnnotationPresent(SecureOut.class));
    if (cls.isAnnotationPresent(SecureOut.class)) {
      Field[] fields = cls.getDeclaredFields();
      for (Field field : fields) {
        log.trace("CHECK2:{}.{} : {}", cls.getSimpleName(), field.getName(), field.isAnnotationPresent(SecureOut.class));
        if (field.isAnnotationPresent(SecureOut.class)) {
          try {
            field.set(prm, null);
          } catch (Exception ignore) { }
        } else {
          Object val = null;
          // try {
          //   val = field.get(prm);
          // } catch (Exception e) {
          //   log.debug("E:{}", e.getMessage());
          // }
          // if (val == null) {
            Method mtd = null;
            try {
              // log.debug("FIND-METHOD:{}.{}", cls.getSimpleName(), cat("get", capitalize(field.getName())));
              mtd = cls.getMethod(SU.cat("get", SU.capitalize(field.getName())), RU.EMPTY_CLS);
              if (mtd != null) { val = mtd.invoke(prm, RU.EMPTY_OBJ); }
            } catch (Exception e) {
              log.debug("E:{}", e.getMessage());
            }
          // }
          // log.debug("CHECK3:{}.{} : {}", cls.getSimpleName(), field.getName(), val);
          if (val != null) {
            if (val instanceof List) {
              List<?> list = RU.cast(val, list = null);
              for (Object itm : list) {
                secureOut(itm);
              }
            } else if (val instanceof Map) {
              Map<String, Object> map = RU.cast(val, map = null);
              for (String key : map.keySet()) {
                secureOut(map.get(key));
              }
            } else {
              secureOut(val);
            }
          }
        }
      }
    }
    return ret;
  }

  public String formattedDate() {
    try {
      return FORMAT_SVC_DATE.format(curDate());
    } catch (Exception ignore) { }
    return null;
  }

  public Map<String, String> getFieldTitleMap(Object o) { return getFieldTitleMap(o, null, PTN_COLUMN_DESC); }
  public Map<String, String> getFieldTitleMap(Object o, Pattern ptnRemove) { return getFieldTitleMap(o, null, ptnRemove); }
  public Map<String, String> getFieldTitleMap(Object o, Set<String> set) { return getFieldTitleMap(o, set, PTN_COLUMN_DESC); }
  public Map<String, String> getFieldTitleMap(Object o, Set<String> set, Pattern ptnRemove) {
    Map<String, String> ret = new LinkedHashMap<>();
    if (o == null) { return ret; }
    Class<?> cls = o.getClass();
    Field[] fields = cls.getFields();
    try {
      LOOP:
      for (Field field : fields) {
        String name = field.getName();
        if (set != null) { if (!set.contains(name)) { continue LOOP; } }
        Annotation[] anons = field.getDeclaredAnnotations();
        for (Annotation anon : anons) {
          if (anon instanceof Schema) {
            Schema schm = RU.cast(anon, schm = null);
            String title = schm.title();
            title = ptnRemove.matcher(title).replaceAll("");
            ret.put(name, title);
            log.trace("ANON FOR {} = {}", name, title);
          }
        }
      }
    } catch (Exception e) {
      log.debug("ERROR:{}", e.getMessage());
    }
    return ret;
  }

  /**
   * 데이터 소유자 확인하여 권한체크
   * @param cid : 현재 조회중인 데이터의 hnfSn 또는 userId
   * @throws SystemException
   */
  public int checkOwner(String cid) throws SystemException {
    int ret = 0;
    HttpServletRequest req = curRequest();
    Authentication auth = getCurrentAuth(req);
    // ExtraInfo ext = getExtraInfo(req);
    boolean isUser = isUser(req);
    // log.debug("CHECK:{} : {},{},{}", isUser, cid, ext.hnfSn, auth.getName());
    if (isUser && !(
        cid != null && (
        // cid.equals(ext.hnfSn) ||
        cid.equals(auth.getName())
      ))) {
      throw new SystemException(999, "", S.SC_FORBIDDEN);
    }
    return ret;
  }

  // public boolean checkURIAuth(HttpServletRequest req, String uri, boolean rthrow) throws Exception {
  //   boolean ret = false;
  //   Matcher mat = null;
  //   String method = req.getMethod();
  //   String userId = getCurrentUserId();
  //   String ipaddr = remoteAddr(req);
  //   List<SimpleGrantedAuthority> authlst = getCurrentGrants(req);
  //   boolean allowed = false;
  //   log.debug("URI:{} / {}", uri, authlst);
  //   /** 관리자인 경우 URI 에 의한 권한체크 */
  //   if ((uri.startsWith(C.PTH_ADMIN) && uri.length() > C.PTH_ADMIN.length()) ||
  //     uri.startsWith(CU.cat(C.PTH_API, C.PTH_ADMIN))) {
  //     /** 관리자 페이지 & API 통신 */
  //     Object o;
  //     allowed = false;
  //     /** NOTE: (HARD-CODING) 로그인 및 속성 API 는 모든 권한에서 조회 가능 */
  //     if ((
  //       uri.contains("/api/admin/algn/algn01001") ||
  //       uri.contains("/api/admin/acmn/acmn01001"))) {
  //       allowed = true;
  //     }

  //     if (!allowed && authlst != null && (mat = PTN_ADMIN_URI_PFX.matcher(uri)) != null && mat.find()) {
  //       String uripfx = mat.group(2);
  //       log.debug("CHECK-ADMIN! {} / {} / {} / {}", method, uripfx, URI_AUTH_MAP.containsKey(uripfx), authlst);
  //       if (URI_AUTH_MAP.containsKey(uripfx)) {
  //         Map<String, String> authMap = URI_AUTH_MAP.get(uripfx);
  //         for (SimpleGrantedAuthority grant : authlst) {
  //           String grp = grant.getAuthority().substring(S.PRFX_ROLE.length());
  //           log.debug("CHECK-GRP:{} / {}", grant.getAuthority(), authMap.get(grp));
  //           if ((o = authMap.get(grp)) != null) {
  //             int lvl = CU.parseInt(o, 0);
  //             switch (method) {
  //             case C_GET: case C_POST: {
  //               if (lvl >= 1) { allowed = true; }
  //             } break;
  //             case C_PUT: case C_DELETE: {
  //               if (lvl >= 2) { allowed = true; }
  //             } break;
  //             }
  //           }
  //         }
  //       }
  //     }
  //     // log.debug("ALLOWED:{} / {}", allowed, checkIp(ipaddr, allowed));
  //     if (allowed && !checkIp(ipaddr, allowed)) {
  //       allowed = false;
  //     }
  //     if (allowed) {
  //       ret = true;
  //     } else if (rthrow) {
  //       throw new SystemException(999, "", S.SC_FORBIDDEN);
  //     }
  //   } else if (C.PTH_ADMIN.equals(uri)) {
  //     /** 관리자 로그인 페이지 */
  //     allowed = true;
  //     if (allowed && !checkIp(ipaddr, allowed)) {
  //       allowed = false;
  //     }
  //     if (allowed) {
  //       ret = true;
  //     } else if (rthrow) {
  //       throw new SystemException(999, "", S.SC_FORBIDDEN);
  //     }
  //   } else {
  //     /** 일반인 페이지 */
  //     allowed = false;
  //     /** NOTE: (HARD-CODING) COMMON API 는 모두 접근 가능 */
  //     if ((
  //       uri.contains("/api/cmn/cmn00000") ||
  //       uri.contains("/api/cmn/cmn01001"))) {
  //       allowed = true;
  //     }
  //     if (!allowed && authlst != null) {
  //       log.debug("URI:{} / {}", uri, userId);
  //     }
  //     ret = true;
  //   }
  //   return ret;
  // }

  public static <T> T readObject(Object src, Class<T> type) {
    T ret = null;
    Reader reader = null;
    try {
      ObjectMapper mapper = new ObjectMapper();
      if (src instanceof Reader) {
        ret = mapper.readValue((Reader) src, type);
      } else if (src instanceof InputStream) {
        reader = reader((InputStream) src, UTF8);
        ret = mapper.readValue(reader, type);
      } else if (src instanceof File) {
        File file = (File) src;
        if (file.exists()) {
          reader = reader(file, UTF8);
          ret = mapper.readValue(reader, type);
        }
      } else if (src instanceof byte[]) {
        ret = mapper.readValue((byte[]) src, type);
      }
    } catch (Exception e) {
      log.error("", e);
    } finally {
      safeclose(reader);
    }
    return ret;
  }
}