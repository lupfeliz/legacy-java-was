/**
 * @File        : MainRestController.java
 * @Author      : 정재백
 * @Since       : 2024-10-30
 * @Description : 메인 REST 컨트롤러
 * @Site        : https://gitlab.ntiple.com/developers
 **/
package com.ntiple.work.mai;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j @RestController @RequestMapping({"/mai"})
public class MainRestController {
  static final String CONTROLLER_TAG1 = "메인 API"; 

  @Autowired MainService service;

  @PostConstruct public void init() {
    log.trace("INIT:{}", MainRestController.class);
  }

  @Operation(summary = "메인페이지API (mai01001a01)", tags = { CONTROLLER_TAG1 })
  @GetMapping("mai01001a01")
  public Object mai01001a01() throws Exception {
    return service.mai01001a01();
  }
}
