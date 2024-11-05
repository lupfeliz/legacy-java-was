/**
 * @File        : SampleService.java
 * @Author      : 정재백
 * @Since       : 2024-10-30
 * @Description : 웹유틸
 * @Site        : https://gitlab.ntiple.com/developers
 **/
package com.ntiple.work.smp;

import static com.ntiple.commons.ConvertUtil.cast;
import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.system.WebUtil.params;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.ntiple.commons.CryptoUtil;
import com.ntiple.system.WebUtil.RequestParameter;
import com.ntiple.work.cmn.CommonEntity.Login;
import com.ntiple.work.cmn.CommonEntity.Result;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class SampleService {

  @PostConstruct public void init() { }

  /** 페이지용 서비스 */
  public String smp01001s01() throws Exception {
    log.debug("CHECK-PARAM:{}", params());
    return "/smp01001s01";
  }

  /** 페이지용 서비스 */
  public String smp01001s02() throws Exception { 
    RequestParameter params = params();
    log.debug("CHECK-PARAM:{} / {}", params.keys(), params);
    for (String key : params.keys()) {
      log.debug("PARAMS:{}", params.get(key));
    }
    return "/smp01001s02";
  }

  public String smp01001p01() {
    log.debug("CHECK-PARAM:{}", params().toString());
    return "/smp01001p01";
  }

  /** REST-API 용 서비스 */
  public Map<String, Object> smp01001a01() throws Exception { 
    Map<String, Object> ret = newMap();
    return ret;
  }

  public Result<?> smp01001a02(Login prm) {
    Result<?> ret = cast(Result.builder()
      .msg("OK")
      .build(), ret = null);
    return ret;
  }

  public Object smp01001a03(Map<String, Object> prm) throws Exception {
    String msg = cast(prm.get("message"), msg = null);
    String key = cast(prm.get("key"), key = null);
    log.debug("MESSAGE:{}", msg);
    log.debug("KEY:{}", key);
    String dec = CryptoUtil.RSA.decrypt(1, key, msg);
    Map<String, Object> ret = newMap();
    ret.put("dec", dec);
    return ret;
  }
}
