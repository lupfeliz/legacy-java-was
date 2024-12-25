/**
 * @File        : Cmn01001ApiControl.java
 * @Author      : 정재백
 * @Since       : 2024-12-25
 * @Description : 공통적으로 사용할 컨트롤
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.work.cmn01;

import static com.ntiple.system.Constants.EMPTY_MAP;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ntiple.system.Settings;

import lombok.extern.slf4j.Slf4j;


@Slf4j @RestController @RequestMapping({"/api/cmn"})
public class Cmn01001ApiControl {

  @Autowired private Settings settings;

  @PostMapping("cmn01001a01")
  public Object cmn01001a01(@RequestBody Map<String, Object> prm) {
    log.debug("================================================================================");
    log.debug("PARAM:{} / {}", prm, settings.getAppctx().hashCode());
    return EMPTY_MAP;
  }
}
