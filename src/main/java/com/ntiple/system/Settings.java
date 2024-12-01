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
import static com.ntiple.commons.ConvertUtil.getCascade;
import static com.ntiple.commons.ConvertUtil.mergeMap;
import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.commons.IOUtil.openResourceStream;
import static com.ntiple.commons.IOUtil.reader;
import static com.ntiple.commons.IOUtil.safeclose;
import static com.ntiple.commons.StringUtil.cat;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.ntiple.Application;
import com.ntiple.work.cmn01.Cmn01001Entity.SystemInfo;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Component @Getter @Setter
public class Settings {
  private static Settings instance;

  @Value("${system.profile}") private String profile;

  /** dbcrypto 관련 파라메터 */
  @Value("${security.dbcrypt.cipher:aes}") private String dbcCipher;
  @Value("${security.dbcrypt.encode:base64}") private String dbcEncode;
  @Value("${security.dbcrypt.secret:}") private String dbcSecret;
  @Value("${security.dbcrypt.charset:utf-8}") private String dbcCharset;

  @Value("${system.assets.cache.dir:}") private String cacheDir;
  @Value("${system.assets.scss.minify:true}") private boolean scssMinify;
  @Value("${system.assets.js.minify:true}") private boolean jsMinify;

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
    reload();
  }

  private static final Pattern PTN_PLACEHOLDER = Pattern.compile("[$][{]([a-zA-Z0-9_.-]+)([:].+){0,1}[}]");

  public static String getProfile() {
    return instance.profile;
  }

  public static boolean isProfile(String... profiles) {
    boolean ret = false;
    log.debug("CHECK:{}{} / {}", "", profiles, instance.profile);
    if (profiles != null) {
      for (String v : profiles) {
        if (instance.profile.equals(v)) {
          return true;
        }
      }
    }
    return ret;
  }

  public static Settings getInstance() {
    return instance;
  }

  public Settings reload() {
    Yaml yaml = new Yaml();
    Reader reader = null;
    Map<String, Object> map = newMap();
    for (int inx = 0; inx < 2; inx++) {
      String fn = "";
      try {
        switch (inx) {
        case 0:   fn = cat("/application.yml"); break;
        default:  fn = cat("/application-", profile, ".yml"); break;
        }
        reader = reader(openResourceStream(Application.class, fn), UTF8);
        map = mergeMap(map, yaml.load(reader));
      } catch (Exception e) {
        log.error("", e);
      } finally {
        safeclose(reader);
      }
    }
    for (Field field: getClass().getDeclaredFields()) {
      try {
        if (field == null) { continue; }
        Value anon = field.getAnnotation(Value.class);
        if (anon == null) { continue; }
        String ak = anon.value();
        String nam = "";
        Object val = null;
        String def = "";
        Matcher mat = PTN_PLACEHOLDER.matcher(ak);
        // log.debug("KEY:{} / {}", key, mat);
        if (mat.find() && mat.groupCount() > 0) {
          nam = mat.group(1);
          if (mat.groupCount() > 1) { def = String.valueOf(mat.group(2)).replaceAll("^[:]", ""); }
          val = getCascade(map, nam.split("[.]"));
          if (val == null) { val = def; }
          log.debug("KEY:{} / {} / {} / {}", nam, val, def, field.get(this));
          field.set(this, val);
        }
      } catch (Exception e) {
        log.debug("E:", e);
      }
    }
    log.debug("YML:{} / {}", profile, map);
    return this;
  }
}
