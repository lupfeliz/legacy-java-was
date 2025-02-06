/**
 * @File        : Smp01001ApiService.java
 * @Author      : 정재백
 * @Since       : 2024-10-30
 * @Description : 웹유틸
 * @Site        : https://gitlab.ntiple.com/developers
 **/
package com.ntiple.work.smp01;

import static com.ntiple.commons.ConvertUtil.array;
import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.StringUtil.cat;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.stereotype.Service;

import com.ntiple.commons.CryptoUtil;
import com.ntiple.work.cmn01.Cmn01001Entity.Login;
import com.ntiple.work.cmn01.Cmn01001Entity.Result;
import com.ntiple.work.cmn01.Cmn01001Entity.SearchEntity;
import com.ntiple.work.cmn01.Cmn01001Repository;
import com.ntiple.work.smp01.Smp01001Entity.SampleArticle;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class Smp01001ApiService {

  @PostConstruct public void init() { }

  @Autowired private Smp01001Repository smpRepo;
  @Autowired private Cmn01001Repository cmnRepo;

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

  public SearchEntity<SampleArticle> smp01001a04(Map<String, Object> prm) throws Exception{
    SearchEntity<SampleArticle> ret = new SearchEntity<>();
    ret.setList(smpRepo.findSample(prm));
    return ret;
  }

  public Result<Object> smp01001a05(Map<String, Object> prm) throws Exception {
    Result<Object> ret = new Result<>();
    // ret.setData(String.valueOf(smpRepo.dbTest(prm)));
    Object obj = prm;
    obj = Proxy.newProxyInstance(this.getClass().getClassLoader(), array(Map.class), (prx, mtd, arg) -> {
      switch (mtd.getName()) {
      case "get": {
        log.debug("ARG:{}{} / {}", "", arg[0], arg[0].getClass());
        return arg[0];
      }
      }
      return null;
    });
    ret.setData(smpRepo.dbTest(obj, prm));
    return ret;
  }

  public static String testStr(String str) {
    log.debug("STR:{}", str);
    return cat("[", str, "]");
  }
}
