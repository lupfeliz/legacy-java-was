/**
 * @File        : RequestAspect.java
 * @Author      : 정재백
 * @Since       : 2024-10-29
 * @Description : 공통적으로 사용할 Aspect, 인증 및 오류처리 등에 관련된 사항들을 처리한다.
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import static com.ntiple.system.WebUtil.curRequest;

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

  @Around("(execution(* *.*(..))) && (getMapPointcut() || postMapPointcut() || putMapPointcut() || delMapPointcut() || reqMapPointcut())")
  public Object aroundAdvice(ProceedingJoinPoint joint) throws Throwable {
    Object ret = null;
    try {
      HttpServletRequest req = curRequest();
      String uri = req.getRequestURI();
      log.debug("BEFORE:{} / {}", uri, joint.toShortString());
      /** TODO: 인증 / 오류처리 등을 수행한다. */
      ret = joint.proceed();
      // if (ret != null && ret instanceof JSONObject) { ret = convert(ret, newMap()); }
      log.debug("AFTER:{} / {}", uri, joint.toShortString());
    } catch (Exception e) {
      log.debug("E:", e);
    }
    return ret;
  }
}
