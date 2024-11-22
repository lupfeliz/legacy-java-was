/**
 * @File        : SystemException.java
 * @Author      : 정재백
 * @Since       : 2024-10-29
 * @Description : 공통 오류
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import static com.ntiple.commons.StringUtil.cat;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter @ToString @Builder
public class SystemException extends RuntimeException {
  public HttpStatus status;
  public Integer errcd;
  public String errmsg;

  public SystemException(Integer errcd, String errmsg) {
    super(cat("[", errcd, "]", errmsg, ""));
    this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    this.errcd = errcd;
    this.errmsg = errmsg;
  }

  public SystemException(Integer errcd, String errmsg, HttpStatus status) {
    super(cat("[", errcd, "]", errmsg, "/", status));
    this.status = status;
    this.errcd = errcd;
    this.errmsg = errmsg;
  }
}
