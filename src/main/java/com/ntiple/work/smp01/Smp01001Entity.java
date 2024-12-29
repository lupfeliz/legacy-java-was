/**
 * @File        : Smp01001Entity.java
 * @Author      : 정재백
 * @Since       : 2024-12-29
 * @Description : 공통 sqlmap
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.work.smp01;

import java.util.Date;

import org.apache.ibatis.type.Alias;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Smp01001Entity {
  
  @Alias("SampleArticle")
  @Schema(title = "SampleArticle", hidden = true)
  @AllArgsConstructor @NoArgsConstructor
  @Getter @Setter @ToString @Builder
  public static class SampleArticle {
    private long uid;
    private String title;
    private String contents;
    private String userId;
    private String userNm;
    private Date ctime;
    private Date utime;
  }
}
