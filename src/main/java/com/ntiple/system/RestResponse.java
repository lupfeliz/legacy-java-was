/**
 * @File        : RestResponseUtil.java
 * @Author      : 정재백
 * @Since       : 2024-10-29
 * @Description : restful 통신 api 공통모듈
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import static com.ntiple.commons.Constants.CONTENT_TYPE;
import static com.ntiple.commons.Constants.CTYPE_HTML;
import static com.ntiple.commons.ConvertUtil.convert;
import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.system.Constants.CRNL;

import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ntiple.work.cmn01.Cmn01001Service;
import com.ntiple.work.cmn01.Cmn01001Entity.AuthResult;
import com.ntiple.work.cmn01.Cmn01001Entity.InitObj;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Component
public class RestResponse {
  public static RestResponse instance;
  @Autowired Settings settings;
  @Autowired Cmn01001Service cmnservice;
  @PostConstruct public void init() {
    instance = this;
  }

  public static <T> ResponseEntity<T> response(Callable<T> exec) {
    HttpHeaders hdrs = new HttpHeaders();
    HttpStatus status = HttpStatus.OK;
    // Settings settings = instance.settings;
    // CommonService cmnservice = instance.cmnservice;
    Object res = null;
    try {
      // HttpServletRequest req = curRequest();
      // String ipaddr = remoteAddr(req);
      // String uri = req.getRequestURI();
      // boolean isAdmin = cmnservice.isAdmin(req);
      // String userId = cmnservice.getCurrentUserId(req);
      // ExtraInfo exinf = cmnservice.getExtraInfo(req);
      // log.trace("IP:{} / URI:{} / IS-ADMIN:{} / USER-ID:{} / EXTRA:{}", ipaddr, uri, isAdmin, userId, exinf);

      // cmnservice.checkURIAuth(req, uri, true);
      res = exec.call();
      log.trace("RES:{}", res);
      if (res instanceof String) {
        /** 문자열 결과인 경우 바로 리턴한다. */
        hdrs.add(CONTENT_TYPE, CTYPE_HTML);
        ResponseEntity<T> ret = cast(
          new ResponseEntity<>(res, hdrs, status), ret = null);
        return ret;
      } else if (res instanceof AuthResult) {
        // AuthResult ares = cast(res, ares = null);
        // Long exptAcc = settings.exptAcc;
        // Long exptRef = settings.exptRef;
        // if (ares.restyp == C.TMP) { exptAcc = settings.exptTmp; }
        // if (ares.accessToken != null) {
        //   if (ares.refreshToken != null) {
        //     hdrs.add(C.AUTHORIZATION, cat(
        //       C.BEARER, C.SP, 
        //       /** 응답헤더에 사용자ID, 토큰정보등을 암호화 하여 내려보낸다. (공백구분) */
        //       settings.enc(
        //         cat(ares.userId, C.SP, ares.userNm, C.SP,
        //           ares.accessToken, C.SP, ares.refreshToken, C.SP,
        //           exptAcc, C.SP, exptRef, C.SP
        //         ), true)
        //     ));
        //   } else {
        //     hdrs.add(C.AUTHORIZATION, cat(
        //       C.BEARER, C.SP, 
        //       /** 토큰리프레시 인 경우 액세스토큰만 발급해서 내려보낸다 */
        //       instance.settings.enc(
        //         cat(ares.userId, C.SP, ares.userNm, C.SP,
        //           ares.accessToken, C.SP, exptAcc), true)
        //     ));
        //   }
        // }
      } else if (res instanceof InitObj) {
        InitObj ires = cast(res, ires = null);
        log.trace("CHECK:{}", ires.getCheck());
      }
    } catch (Exception e) {
      if (e instanceof SystemException) {
        log.debug("ERROR:{}{}{}", e.getMessage(), CRNL, errstack(e));
        SystemException ee = cast(e, SystemException.class);
        if (ee.status != null) { status = ee.status; }
        res = convert(new Object[] { "message", ee.errmsg }, res);
      } else {
        log.debug("ERROR:", e);
        status = HttpStatus.INTERNAL_SERVER_ERROR;
      }
    }
    ResponseEntity<T> ret = cast(new ResponseEntity<>(Cmn01001Service.secureOut(res), hdrs, status), ret = null);
    return ret;
  }

  public static void log(String format, Object... args) {
    log.debug(format, args);
  }

  public static String errstack(Exception e) {
    StringBuilder ret = new StringBuilder();
    if (e == null) { return null; }
    // HttpServletRequest req = curRequest();
    // String pkgbase = C.BASE_PKG.getName();
    // ExtraInfo ext = instance.cmnservice.getExtraInfo(req);
    // ret.append("|")
    //   .append(req.getMethod()).append(C.SP)
    //   .append(req.getRequestURI()).append(C.SP)
    //   .append(instance.cmnservice.getCurrentUserId(req)).append(C.SP)
    //   .append(ext.userType).append(C.SP)
    //   .append(ext.remoteAddr).append(C.SP)
    //   .append(C.CRNL);
    // for (StackTraceElement itm : e.getStackTrace()) {
    //   if (itm.getClassName().startsWith(pkgbase)) {
    //     if (itm.getLineNumber() != -1) {
    //       ret.append("|").append(itm.getFileName()).append(":").append(itm.getLineNumber()).append(C.CRNL);
    //     }
    //   }
    // }
    return String.valueOf(ret);
  }
}