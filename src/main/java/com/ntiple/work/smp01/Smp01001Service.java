/**
 * @File        : Smp01001Service.java
 * @Author      : 정재백
 * @Since       : 2024-10-30
 * @Description : 웹유틸
 * @Site        : https://gitlab.ntiple.com/developers
 **/
package com.ntiple.work.smp01;

import static com.ntiple.commons.WebUtil.params;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.ntiple.commons.WebUtil.RequestParameter;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class Smp01001Service {

  @PostConstruct public void init() { }

  /** 페이지용 서비스 */
  public String smp01001s01(Model model) throws Exception {
    model.addAttribute("TEST", "TEST-VALUE");
    log.debug("CHECK-PARAM:{}", params());
    return "smp01/smp01001s01";
  }

  /** 페이지용 서비스 */
  public String smp01001s02(Model model) throws Exception { 
    RequestParameter params = params();
    log.debug("CHECK-PARAM:{} / {}", params.keys(), params);
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
    return "smp01/smp01001p01";
  }
}
