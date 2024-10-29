/**
 * @File        : Settings.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 환경변수 간편 참조용 객체
 * @Site        : https://devlog.ntiple.com
 * 
 * 객체 활성화 - wire 순서가 꼬일 수 있으므로 Config 단에서는 사용하지 않거나
 * Autowired 해서 사용한다.
 **/

package com.ntiple.system;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.cat;
import static com.ntiple.commons.IOUtils.openResourceStream;
import static com.ntiple.commons.IOUtils.reader;
import static com.ntiple.commons.IOUtils.safeclose;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.ntiple.Application;
import com.ntiple.work.cmn.CommonEntity.SystemInfo;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Component @Getter @Setter
public class Settings {
  public static Settings instance;

  @Value("${spring.profiles.active}") private String profile;

  /** dbcrypto 관련 파라메터 */
  @Value("${security.dbcrypt.cipher:aes}") private String dbcCipher;
  @Value("${security.dbcrypt.encode:base64}") private String dbcEncode;
  @Value("${security.dbcrypt.secret:}") private String dbcSecret;
  @Value("${security.dbcrypt.charset:utf-8}") private String dbcCharset;

    /** 저장소 경로 */
  @Value("${storage.path:/tmp}") private String storagePath;

  /** 기본URL 주소 */
  private List<String> hostNames;

  /** 사용불가능한 id 목록 */
  private List<String> notAllowedUserId;

  /** JNDI */
  @Value("${spring.datasource.jndi-name:}") private String jndiName;

  /** SYSTEM-ADM */
  private SystemInfo admInfo;

  @Value("${system.timediff:0}") private Long systemTimeDiff;

  @PostConstruct public void init() {
    log.trace("INIT:{}", Settings.class);
    instance = this;
    reloadYmlSettings();
  }

  public void reloadYmlSettings() {
    Yaml yaml = new Yaml();
    Reader reader = null;
    Map<String, Object> map;
    // Object o;
    try {
      reader = reader(openResourceStream(Application.class, cat("/application-", profile, ".yml")), UTF8);
      map = yaml.load(reader);
      log.debug("YML:{}", map);
    } catch (Exception e) {
      log.error("", e);
    } finally {
      safeclose(reader);
    }
  }

  public static void sleep(long time) {
    try { Thread.sleep(time); } catch (Exception ignore) { }
  }
}
