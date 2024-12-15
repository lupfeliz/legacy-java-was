/**
 * @File        : ReflectionTest.java
 * @Author      : 정재백
 * @Since       : 2024-12-15
 * @Description : 리플렉션 테스트 
 * @Site        : https://devlog.ntiple.com
 * 
 * sh gradlew cleanTest test -Dproject.build.test=MANUAL -Dspring.profiles.active=local -i --no-watch-fs --tests "com.ntiple.ReflectionTest.testSimple"
 **/
package com.ntiple;

import org.junit.jupiter.api.Test;

import com.ntiple.commons.ClassWorker;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReflectionTest {

  @Test public void testSimple() {
    ClassWorker.work(Application.class.getClassLoader(),
    cls -> { log.debug("CLASS:{}", cls); },
    "com.ntiple.work", "com.ntiple.system");
  }
}
