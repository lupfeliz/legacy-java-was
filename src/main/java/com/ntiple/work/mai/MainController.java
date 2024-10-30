/**
 * @File        : MainController.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 메인 페이지 컨트롤러
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.work.mai;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;

@Slf4j @Controller @RequestMapping({"/"})
public class MainController {

  @GetMapping({"/", "/mai/mai01001s01"})
  public String main() {
    log.debug("MAIN Request");
    return "/mai01001s01";
  }
}
