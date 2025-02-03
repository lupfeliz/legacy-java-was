/**
 * @File        : DbAccessTest.java
 * @Author      : 정재백
 * @Since       : 2024-12-29
 * @Description : 간단한 테스트 케이스들
 * @Site        : https://devlog.ntiple.com
 * 
 * sh gradlew cleanTest test -Dproject.build.test=MANUAL -Dspring.profiles.active=my -i --no-watch-fs --tests "com.ntiple.DbAccessTest.test1"
 **/
package com.ntiple;

import static com.ntiple.commons.ConvertUtil.convert;
import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.commons.StringUtil.cat;
import static com.ntiple.work.smp01.Smp01001Entity.SampleArticle;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;

import com.ntiple.TestUtil.TestLevel;
import com.ntiple.work.smp01.Smp01001Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbAccessTest {
  @Test public void test1() throws Exception {
    if (!TestUtil.isEnabled("test1", TestLevel.MANUAL)) { return; }
    System.setProperty("spring.profiles.active", "my");
    SqlSessionTemplate sst = TestUtil.initDb("main");
    Smp01001Repository mapper = sst.getMapper(Smp01001Repository.class);
    Date ctime = new Date();
    for (int inx = 0; inx < 1000; inx++) {
      SampleArticle article = SampleArticle.builder()
        .uid(inx)
        .title(cat("TITLE", inx))
        .contents(cat("CONTENTS", inx))
        .userId("userId")
        .userNm("userNm")
        .ctime(ctime)
        .utime(ctime)
        .build();
      mapper.addSample(article);
    }
    Integer count = mapper.countSample(convert(new String[][] {
      {"", ""}
    }, newMap()));
    log.debug("COUNT:{}", count);
  }

  @Test public void test2() throws Exception {
    if (!TestUtil.isEnabled("test2", TestLevel.MANUAL)) { return; }
    System.setProperty("spring.profiles.active", "my");
    SqlSessionTemplate sst = TestUtil.initDb("main");
    Smp01001Repository mapper = sst.getMapper(Smp01001Repository.class);
    Integer count = mapper.countSample(convert(new String[][] {
      {"", ""}
    }, newMap()));
    log.debug("COUNT:{}", count);
  }
}
