/**
 * @File        : Smp01001ApiService.java
 * @Author      : 정재백
 * @Since       : 2024-10-30
 * @Description : 웹유틸
 * @Site        : https://gitlab.ntiple.com/developers
 **/
package com.ntiple.work.smp01;

import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.commons.ReflectionUtil.cast;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.ntiple.commons.CryptoUtil;
import com.ntiple.work.cmn01.Cmn01001Entity.Login;
import com.ntiple.work.cmn01.Cmn01001Entity.Result;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class Smp01001ApiService {

  @PostConstruct public void init() { }

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
