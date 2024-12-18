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
import org.springframework.web.bind.annotation.RequestMethod;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Controller @RequestMapping({"/"})
public class Smp01001Control {

  @Autowired Smp01001Service service;

  @RequestMapping(value = {"/", "/smp/smp01001s01"}, method = { RequestMethod.GET, RequestMethod.POST })
  public String smp01001s01(Model model) throws Exception {
    return service.smp01001s01(model);
  }

  @RequestMapping(value = {"/smp/smp01001p01"}, method = { RequestMethod.GET, RequestMethod.POST })
  public String smp01001p01(Model model) throws Exception {
    return service.smp01001p01(model);
  }

  @RequestMapping(value = {"/smp/smp01001s02"}, method = { RequestMethod.GET, RequestMethod.POST })
  public String smp01001s02(Model model) throws Exception {
    return service.smp01001s02(model);
  }

  @RequestMapping(value = {"/smp/smp01001s03"}, method = { RequestMethod.GET, RequestMethod.POST })
  public String smp01001s03(Model model) throws Exception {
    return service.smp01001s03(model);
  }

  @RequestMapping(value = {"/smp/smp01001s04"}, method = { RequestMethod.GET, RequestMethod.POST })
  public String smp01001s04(Model model) throws Exception {
    return service.smp01001s04(model);
  }

  @RequestMapping(value = {"/smp/smp01001s05"}, method = { RequestMethod.GET, RequestMethod.POST })
  public String smp01001s05(Model model) throws Exception {
    return service.smp01001s05(model);
  }

  @RequestMapping(value = {"/smp/smp01001s06"}, method = { RequestMethod.GET, RequestMethod.POST })
  public String smp01001s06(Model model) throws Exception {
    return service.smp01001s06(model);
  }
}
