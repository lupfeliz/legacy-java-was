package com.ntiple.work.mai;

import static com.ntiple.system.WebUtil.params;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class MainService {

  @PostConstruct public void init() {
  }

  public String mai01001s01(HttpServletRequest req) throws Exception {
    log.debug("CHECK-PARAM:{}", params(req));
    return "/mai01001s01";
  }

  public String mai01001s02(HttpServletRequest req) throws Exception { 
    log.debug("CHECK-PARAM:{}", params(req));
    return "/mai01001s02";
  }
}
