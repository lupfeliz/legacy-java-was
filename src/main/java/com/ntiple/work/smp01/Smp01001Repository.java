/**
 * @File        : Smp01001Repository.java
 * @Author      : 정재백
 * @Since       : 2024-12-29
 * @Description : 공통 sqlmap
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.work.smp01;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.ntiple.work.smp01.Smp01001Entity.SampleArticle;

public interface Smp01001Repository {
  
  List<SampleArticle> findSample(@Param("prm") Object prm) throws Exception;

  Integer countSample(@Param("prm") Object prm) throws Exception;

  Integer addSample(@Param("prm") Object prm) throws Exception;

  Object dbTest(@Param("prm") Object prm, @Param("prm2") Object prm2) throws Exception;
}
