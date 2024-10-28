/**
 * @File        : TestUtil.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 테스트 레벨링 모듈
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestUtil {
  public static enum TestLevel {
    NONE(0),
    SIMPLE(1),
    DBCON(2),
    API(3),
    FULL(4),
    MANUAL(99);
    
    private final int value;
    TestLevel(int value) { this.value = value; }
    public int value() { return value; }
  }

  public static boolean isEnabled(String testName, TestLevel lvl) {
    boolean ret = false;
    if (lvl == null) { lvl = TestLevel.NONE; }
    String enabled = "";
    try {
      enabled = System.getProperty("project.build.test");
      if (enabled == null || "".equals(enabled)) { enabled = TestLevel.SIMPLE.name(); }
      TestLevel target = TestLevel.valueOf(enabled);
      if (target.value() >= lvl.value()) { ret = true; }
      log.info("LEVEL CHECK:{} / {}[{}] / {}[{}], {}, {}", testName,
        target, target.name(), lvl, lvl.name(), enabled, ret);
    } catch (Exception ignore) { }
    return ret;
  }
}
