/**
 * @File        : MainController.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 메인 페이지 컨트롤러
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.work.mai;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Controller @RequestMapping({"/"})
public class MainController {

  @Autowired MainService service;

  @RequestMapping({"/", "/mai/mai01001s01"})
  public String mai01001s01(HttpServletRequest req) throws Exception {
    return service.mai01001s01(req);
  }

  @RequestMapping({"/mai/mai01001s02"})
  public String mai01001s02(HttpServletRequest req) throws Exception {
    return service.mai01001s02(req);
  }
}
