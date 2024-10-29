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

package com.ntiple.commons;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.asList;
import static com.ntiple.commons.ConvertUtil.cat;
import static com.ntiple.commons.ConvertUtil.parseInt;
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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Component @Getter @Setter
public class Settings {
  public static Settings instance;

  @Value("${spring.profiles.active}") private String profile;

  /** 기본URL 주소 */
  private List<String> hostNames;

  /** 사용불가능한 id 목록 */
  private List<String> notAllowedUserId;

  /** JNDI */
  @Value("${spring.datasource.jndi-name:}") private String jndiName;

  @Value("${system.timediff:32400000}") private Long systemTimeDiff;

  @PostConstruct public void init() {
    log.trace("INIT:{}", Settings.class);
    instance = this;
    reloadYmlSettings();
  }

  public void reloadYmlSettings() {
    Yaml yaml = new Yaml();
    Reader reader = null;
    Map<String, Object> map;
    Object o;
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

  public static boolean checkIpMatch(String ipAddr, String filter) {
    boolean ret = false;
    List<String> frgdata = asList(ipAddr.split("[.]"));
    List<String> fltdata = asList(filter.split("[.]"));
    for (int inx = fltdata.size(); inx < frgdata.size(); inx++) {
      fltdata.add("*");
    }
    int inx = 0;
    LOOP:
    for (; inx < fltdata.size(); inx++) {
      String frg = String.valueOf(frgdata.get(inx)).trim();
      String flt = String.valueOf(fltdata.get(inx)).trim();
      log.trace("FRG:{} / FLT:{}", frg, flt);
      if (frg.equals(flt)) { continue LOOP; }
      if (flt.equals("*")) { continue LOOP; }
      if (flt.contains("-")) {
        int num = parseInt(frg, -1);
        if (num == -1) { break LOOP; }
        String[] tmp = flt.split("[-]");
        if (tmp.length != 2) { break LOOP; }
        int[] rng = new int[tmp.length];
        rng[0] = parseInt(tmp[0]);
        rng[1] = parseInt(tmp[1]);
        log.trace("CHECK:{} : {}~{}", num, rng[0], rng[1]);
        if (num >= rng[0] && num <= rng[1]) { continue LOOP; }
      }
      break LOOP;
    }
    if (inx >= fltdata.size()) { ret = true; }
    log.trace("CHECK:{} / {} / {}", inx, fltdata.size(), ret);
    return ret;
  }
}
