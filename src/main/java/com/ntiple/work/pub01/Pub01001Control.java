package com.ntiple.work.pub01;

import static com.ntiple.commons.StringUtil.cat;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * @File        : Pub01001Control.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 퍼블샘플 페이지 컨트롤러
 * @Site        : https://devlog.ntiple.com
 **/

@Slf4j @Controller @RequestMapping({"/pbl"})
public class Pub01001Control {
  
  @GetMapping({"/{fname}"})
  public String pubpage(Model model, @PathVariable String fname) throws Exception {
    return cat("pub01/", fname);
  }
}
