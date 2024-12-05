/**
 * @File        : JasyptConfig.java
 * @Author      : 정재백
 * @Since       : 2024-12-05
 * @Description : swagger 설정파일
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ntiple.system.Settings;

@Configuration
public class JasyptConfig {

  @Autowired Settings settings;

  public static StringEncryptor getEncryptor(String encryptkey) {
    PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
    SimpleStringPBEConfig config = new SimpleStringPBEConfig();
    config.setPassword(encryptkey);
    config.setAlgorithm("PBEWithMD5AndDES");
    config.setKeyObtentionIterations("1000");
    config.setPoolSize("1");
    config.setProviderName("SunJCE");
    config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
    config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator");
    config.setStringOutputType("base64");
    encryptor.setConfig(config);
    return encryptor;
  }

  @Bean("jasyptStringEncryptor")
  public StringEncryptor stringEncryptor() {
    return getEncryptor(settings.getEncryptSeed());
  }
}
