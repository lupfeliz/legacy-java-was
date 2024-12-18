/**
 * @File        : JasyptTest.java
 * @Author      : 정재백
 * @Since       : 2024-12-05
 * @Description : 간단한 테스트 케이스들
 * @Site        : https://devlog.ntiple.com
 * 
 * sh gradlew cleanTest test -Dproject.build.test=MANUAL -Dspring.profiles.active=local -i --no-watch-fs --tests "com.ntiple.JasyptTest.testEncrypt"
 **/
package com.ntiple;

import org.jasypt.encryption.StringEncryptor;
import org.junit.Test;

import com.ntiple.TestUtil.TestLevel;
import com.ntiple.config.JasyptConfig;
import com.ntiple.system.Settings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JasyptTest {

  @Test public void testEncrypt() throws Exception {
    if (!TestUtil.isEnabled("testEncrypt", TestLevel.MANUAL)) { return; }
    Settings settings = Settings.getInstance();
    String seed = settings.getEncryptSeed();
    log.debug("SETTINGS-SEED:{}", settings.getEncryptSeed());
    StringEncryptor encryptor = JasyptConfig.getEncryptor(seed);
    String txt = "";
    String enc = "";
    String dec = "";
    {
      txt = "테스트";
      enc = encryptor.encrypt(txt);
      dec = encryptor.decrypt(enc);
      log.debug("PLAIN TEXT:{}", txt);
      log.debug("ENCRYPTED:{}", enc);
      log.debug("DECRYPTED:{}", dec);
    }
  }
}
