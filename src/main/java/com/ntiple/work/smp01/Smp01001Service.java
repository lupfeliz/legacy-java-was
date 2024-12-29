/**
 * @File        : Smp01001Service.java
 * @Author      : 정재백
 * @Since       : 2024-10-30
 * @Description : 웹유틸
 * @Site        : https://gitlab.ntiple.com/developers
 **/
package com.ntiple.work.smp01;

import static com.ntiple.commons.ConvertUtil.parseInt;
import static com.ntiple.commons.WebUtil.curRequest;
import static com.ntiple.commons.WebUtil.params;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.ntiple.commons.WebUtil.RequestParameter;
import com.ntiple.work.cmn01.Cmn01001Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class Smp01001Service {

  @Autowired Cmn01001Service cmnsvc;
  @Autowired Smp01001Repository smprepo;

  @PostConstruct public void init() {
  }

  /** 페이지용 서비스 */
  public String smp01001s01(Model model) throws Exception {
    model.addAttribute("TEST", "TEST-VALUE");
    cmnsvc.dbCurrent();
    log.debug("CHECK-PARAM:{}", params());
    return "smp01/smp01001s01";
  }

  /** 페이지용 서비스 */
  public String smp01001s02(Model model) throws Exception { 
    HttpServletRequest req = curRequest(HttpServletRequest.class);
    HttpSession session = req.getSession();
    RequestParameter params = params();
    Integer cnt = parseInt(session.getAttribute("TEST"), 0) + 1;
    session.setAttribute("TEST", cnt);
    session.setAttribute("LOGIN-ID", "test");
    log.debug("SESSION:{}", session);
    log.debug("CHECK-PARAM:{} / {}", params.keys(), params);
    log.debug("SESSION:{}", cnt);
    for (String key : params.keys()) {
      log.debug("PARAMS:{}", params.get(key));
    }
    return "smp01/smp01001s02";
  }

  public String smp01001s03(Model model) throws Exception {
    return "smp01/smp01001s03";
  }

  public String smp01001s04(Model model) throws Exception {
    return "smp01/smp01001s04";
  }

  public String smp01001s05(Model model) throws Exception {
    return "smp01/smp01001s05";
  }

  public String smp01001s06(Model model) throws Exception {
    return "smp01/smp01001s06";
  }

  public String smp01001p01(Model model) throws Exception {
    log.debug("CHECK-PARAM:{}", params());
    HttpServletRequest req = curRequest(HttpServletRequest.class);
    HttpSession session = req.getSession();
    session.invalidate();
    return "smp01/smp01001p01";
  }
}
