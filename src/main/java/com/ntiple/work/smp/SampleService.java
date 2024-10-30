/**
 * @File        : SampleService.java
 * @Author      : 정재백
 * @Since       : 2024-10-30
 * @Description : 웹유틸
 * @Site        : https://gitlab.ntiple.com/developers
 **/
package com.ntiple.work.smp;

import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.system.WebUtil.params;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class SampleService {

  @PostConstruct public void init() {
  }

  /** 페이지용 서비스 */
  public String smp01001s01() throws Exception {
    log.debug("CHECK-PARAM:{}", params());
    return "/smp01001s01";
  }

  /** 페이지용 서비스 */
  public String smp01001s02() throws Exception { 
    log.debug("CHECK-PARAM:{}", params());
    return "/smp01001s02";
  }

  /** REST-API 용 서비스 */
  public Object smp01001a01() throws Exception { 
    Map<String, Object> ret = newMap();
    ret.put("RESULT", "OK");
    return ret;
  }
}
