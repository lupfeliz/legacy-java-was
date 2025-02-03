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
import static com.ntiple.commons.ConvertUtil.parseBoolean;
import static com.ntiple.commons.ConvertUtil.parseByte;
import static com.ntiple.commons.ConvertUtil.parseDouble;
import static com.ntiple.commons.ConvertUtil.parseFloat;
import static com.ntiple.commons.ConvertUtil.parseInt;
import static com.ntiple.commons.ConvertUtil.parseLong;
import static com.ntiple.commons.ConvertUtil.parseShort;
import static com.ntiple.commons.IOUtil.openResourceStream;
import static com.ntiple.commons.IOUtil.reader;
import static com.ntiple.commons.IOUtil.safeclose;
import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.StringUtil.cat;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.ntiple.Application;
import com.ntiple.commons.ObjectStore;
import com.ntiple.config.JasyptConfig;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Component @Getter @Setter
public class Settings {
  private static final ObjectStore<Settings> instance = new ObjectStore<>();

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

  /** 암호화 seed */
  @Value("${system.key-seed:}") private String encryptSeed;

  @Value("${system.timediff:0}") private Long systemTimeDiff;

  /** 기본URL 주소 */
  private List<String> hostNames;

  /** 사용불가능한 id 목록 */
  private List<String> notAllowedUserId;

  @Autowired private ApplicationContext appctx;
  private ConfigurableListableBeanFactory beanFactory;

  /** YML 셋팅맷 */
  private Map<String, Object> settingMap;

  @PostConstruct public void init() {
    log.trace("INIT:{}", Settings.class);
    instance.set(this);
    this.beanFactory = ((ConfigurableApplicationContext) this.getAppctx()).getBeanFactory();
    reload();
  }

  private static final Pattern PTN_PLACEHOLDER = Pattern.compile("[$][{]([a-zA-Z0-9_.-]+)([:].*){0,1}[}]");
  private static final Pattern PTN_ENCPATTERN = Pattern.compile("ENC\\(([a-zA-Z0-9\\/+=]+)\\)");

  public static String getProfile() {
    return instance.get().profile;
  }

  public static boolean isProfile(String... profiles) {
    boolean ret = false;
    log.debug("CHECK:{}{} / {}", "", profiles, instance.get().profile);
    if (profiles != null) {
      for (String v : profiles) {
        if (instance.get().profile.equals(v)) {
          return true;
        }
      }
    }
    return ret;
  }

  public static Settings getInstance() {
    if (instance.get() == null) {
      instance.set(new Settings());
      instance.get().reload();
    }
    return instance.get();
  }

  public Object getProperty(String key) {
    Object ret = null;
    ret = getCascade(this.settingMap, key.split("[.]"));
    if (ret != null && ret instanceof String) {
      ret = fillPlaceholder(String.valueOf(ret), key);
    }
    return ret;
  }

  private String fillPlaceholder(String val, String nam) {
    {
      String str = val;
      String v = null;
      Matcher vmat = PTN_PLACEHOLDER.matcher(str);
      if (vmat.find()) {
        String k = vmat.group(1);
        LOOP: for (int kinx = 0; kinx < 10; kinx++) {
          SW: switch (kinx) {
          case 0: { v = System.getProperty(k); } break SW;
          default: { v = null; } break SW; }
          if (v != null && !"".equals(v)) { break LOOP; }
        }
        if (v != null && !"".equals(v)) {
          str = cat(str.substring(0, vmat.start()), v, str.substring(vmat.end()));
          log.debug("VALUE:{} = {} / {} = {}", nam, str, k, v);
          val = str;
        }
      }
    }
    {
      String str = String.valueOf(val);
      Matcher vmat = PTN_ENCPATTERN.matcher(str);
      if (vmat.find()) {
        StringEncryptor enc = JasyptConfig.getEncryptor(cast(getCascade(settingMap, "system", "key-seed"), ""));
        str = enc.decrypt(vmat.group(1));
        log.debug("VALUE:{} = {}", nam, str);
        val = str;
      }
    }
    return val;
  }

  public Settings reload() {
    Yaml yaml = new Yaml();
    Reader reader = null;
    this.settingMap = newMap();
    String profile = System.getProperty("spring.profiles.active");
    for (int inx = 0; inx < 2; inx++) {
      String fn = "";
      try {
        switch (inx) {
        case 0:   fn = cat("/application.yml"); break;
        default:  fn = cat("/application-", profile, ".yml"); break;
        }
        reader = reader(openResourceStream(Application.class, fn), UTF8);
        settingMap = mergeMap(settingMap, yaml.load(reader));
      } catch (Exception e) {
        log.debug("PROFILE {} NOT FOUND", fn);
        // log.error("", e);
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
        Object tmp = null;
        String def = "";
        Matcher mat = PTN_PLACEHOLDER.matcher(ak);
        // log.debug("KEY:{} / {}", key, mat);
        if (mat.find() && mat.groupCount() > 0) {
          Class<?> type = field.getType();
          nam = mat.group(1);
          if (mat.groupCount() > 1) { def = String.valueOf(mat.group(2)).replaceAll("^[:]", ""); }
          val = getCascade(settingMap, nam.split("[.]"));
          if (val == null) { val = def; }
          log.trace("KEY:{} / {} / {} / {} / {}", nam, val, def, type, field.get(this));
          if (
            ((type == int.class || type == Integer.class)
              && (tmp = parseInt(val, null)) != null) ||
            ((type == long.class || type == Long.class)
              && (tmp = parseLong(val, null)) != null) ||
            ((type == short.class || type == Short.class)
              && (tmp = parseShort(val, null)) != null) ||
            ((type == byte.class || type == Byte.class)
              && (tmp = parseByte(val, null)) != null) ||
            ((type == float.class || type == Float.class)
              && (tmp = parseFloat(val, null)) != null) ||
            ((type == double.class || type == Double.class)
              && (tmp = parseDouble(val, null)) != null) ||
            ((type == boolean.class || type == Boolean.class)
              && (tmp = parseBoolean(val, null)) != null)) {
            val = tmp;
          } else  if (type == String.class) {
            val = fillPlaceholder(String.valueOf(val), nam);
          }
          if (val != null) { field.set(this, val); }
        }
      } catch (Exception e) {
        log.debug("E:", e);
      }
    }
    log.trace("YML:{} / {}", profile, settingMap);
    return this;
  }
}
