/**
 * @File        : SampleService.java
 * @Author      : 정재백
 * @Since       : 2024-10-30
 * @Description : 웹유틸
 * @Site        : https://gitlab.ntiple.com/developers
 **/
package com.ntiple.work.smp;

import static com.ntiple.commons.ConvertUtil.cast;
import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.system.WebUtil.params;
import static com.ntiple.commons.CryptoUtil.RSA.encrypt;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;

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

    // Key pubk = KeyFactory.getInstance("RSA")
    //   .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(key)));
    // log.debug("PUBLIC-KEY:{}", pubk);

    Key prvk = KeyFactory.getInstance("RSA")
      .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key)));
    log.debug("PRIVATE-KEY:{}", prvk);

    // Cipher cipher = Cipher.getInstance("RSA");
    // cipher.init(Cipher.DECRYPT_MODE, pubk);
    // byte[] dec = cipher.doFinal(Base64.getDecoder().decode(msg));
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.DECRYPT_MODE, prvk);
    byte[] dec = cipher.doFinal(Base64.getDecoder().decode(msg));

    log.debug("DEC:{}", new String(dec, UTF8));

    
    // String dec = CryptoUtil.RSA.decrypt(1, key, msg);
    // log.debug("DECRYPTED:{}", dec);
    Map<String, Object> ret = newMap();
    // ret.put("DEC", dec);
    return ret;
  }
}
