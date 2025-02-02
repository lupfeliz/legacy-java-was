/**
 * @File        : Smp01001ApiControl.java
 * @Author      : 정재백
 * @Since       : 2024-10-30
 * @Description : 메인 REST 컨트롤러
 * @Site        : https://gitlab.ntiple.com/developers
 **/
package com.ntiple.work.smp01;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ntiple.work.cmn01.Cmn01001Entity.Login;
import com.ntiple.work.cmn01.Cmn01001Entity.SearchEntity;
import com.ntiple.work.smp01.Smp01001Entity.SampleArticle;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;


@Slf4j @RestController @RequestMapping({"/api/smp"})
public class Smp01001ApiControl {
  static final String CONTROLLER_TAG1 = "샘플 API"; 

  @Autowired Smp01001ApiService service;

  @PostConstruct public void init() {
    log.trace("INIT:{}", Smp01001ApiControl.class);
  }

  @Operation(summary = "샘플페이지API (smp01001a01)", tags = { CONTROLLER_TAG1 })
  @GetMapping("smp01001a01")
  public Object smp01001a01() throws Exception {
    return service.smp01001a01();
  }

  @Operation(summary = "테스트API (smp01001a02)", tags = { CONTROLLER_TAG1 })
  @PostMapping("smp01001a02")
  public Object smp01001a02(@RequestBody Login prm) throws Exception {
    return service.smp01001a02(prm);
  }

  @Operation(summary = "테스트API (smp01001a03)", tags = { CONTROLLER_TAG1 })
  @PostMapping("smp01001a03")
  public Object smp01001a03(@RequestBody Map<String, Object> prm) throws Exception {
    return service.smp01001a03(prm);
  }

  @Operation(summary = "테스트API (smp01001a04)", tags = { CONTROLLER_TAG1 })
  @PostMapping("smp01001a04")
  public SearchEntity<SampleArticle> smp01001a04(@RequestBody Map<String, Object> prm) throws Exception {
    return service.smp01001a04(prm);
  }
}
