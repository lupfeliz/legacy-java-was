/**
 * @File        : Smp01001Control.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 메인 페이지 컨트롤러
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.work.smp01;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Controller @RequestMapping({"/"})
public class Smp01001Control {

  @Autowired Smp01001Service service;

  @RequestMapping({"/", "/smp/smp01001s01"})
  public String smp01001s01(Model model) throws Exception {
    return service.smp01001s01(model);
  }

  @RequestMapping({"/smp/smp01001p01"})
  public String smp01001p01(Model model) throws Exception {
    return service.smp01001p01(model);
  }

  @RequestMapping({"/smp/smp01001s02"})
  public String smp01001s02(Model model) throws Exception {
    return service.smp01001s02(model);
  }

  @RequestMapping({"/smp/smp01001s03"})
  public String smp01001s03(Model model) throws Exception {
    return service.smp01001s03(model);
  }

  @RequestMapping({"/smp/smp01001s04"})
  public String smp01001s04(Model model) throws Exception {
    return service.smp01001s04(model);
  }

  @RequestMapping({"/smp/smp01001s05"})
  public String smp01001s05(Model model) throws Exception {
    return service.smp01001s05(model);
  }

  @RequestMapping({"/smp/smp01001s06"})
  public String smp01001s06(Model model) throws Exception {
    return service.smp01001s06(model);
  }
}
