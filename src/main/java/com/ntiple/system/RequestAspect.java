/**
 * @File        : RequestAspect.java
 * @Author      : 정재백
 * @Since       : 2024-10-29
 * @Description : 공통적으로 사용할 Aspect, 인증 및 오류처리 등에 관련된 사항들을 처리한다.
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.StringUtil.cat;
import static com.ntiple.commons.WebUtil.curRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component @Aspect @Slf4j
public class RequestAspect {
  @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
  public void getMapPointcut() { }
  @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
  public void postMapPointcut() { }
  @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
  public void putMapPointcut() { }
  @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
  public void delMapPointcut() { }
  @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
  public void reqMapPointcut() { }

  private static final Pattern PTN_WORK = Pattern.compile("^[/][a-z]{3}[/](?<cate>[a-z]{3})(?<wkno>[0-9]{2}[0-9]{3})(?<rqty>[a-z])(?<stno>[0-9]{2})$");

  @Around("(execution(* *.*(..))) && (getMapPointcut() || postMapPointcut() || putMapPointcut() || delMapPointcut() || reqMapPointcut())")
  public Object aroundAdvice(ProceedingJoinPoint joint) throws Throwable {
    Object ret = null;
    try {
      HttpServletRequest req = curRequest(HttpServletRequest.class);
      String uri = cast(req.getAttribute("uri"), uri = "");
      Matcher mat = null;
      String cate = "", wkno = "", rqty = "", stno = "";
      if ("/".equals(uri)) { uri = "/smp/smp01001s01"; }
      if ((mat = PTN_WORK.matcher(uri)) != null && mat.find()) {
        req.setAttribute("category", cate = mat.group("cate"));
        req.setAttribute("worknumber", wkno = mat.group("wkno"));
        req.setAttribute("reqtype", rqty = mat.group("rqty"));
        req.setAttribute("stepnumber", stno = mat.group("stno"));
        if (ret == null || "".equals(ret)) { ret = cat("/", cate, wkno, rqty, stno); }
      }
      log.debug("BEFORE:{} / {}", uri, joint.toShortString());
      /** TODO: 인증 / 오류처리 등을 수행한다. */
      ret = joint.proceed();
      if ((ret == null || "".equals(ret)) && !"".equals(wkno)) {
        ret = cat(cate, wkno, "/", cate, wkno, rqty, stno);
      }
      if (ret != null && ret instanceof String) {
        ret = cat("!", cat(ret).replaceAll("^/", ""));
      }
      log.debug("AFTER:{} / {} / {} / {}", uri, joint.toShortString(), ret);
    } catch (Exception e) {
      log.debug("E:", e);
    }
    return ret;
  }
}
