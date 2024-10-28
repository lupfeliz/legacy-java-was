/**
 * @File        : HelloTag.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : Custom taglib sample (hello)
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons.tags;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j @Setter @Getter
public class HelloTag extends SimpleTagSupport {

  private String message;

  public void doTag() throws JspException, IOException {
    // log.debug("CHECK-MESSAGE:{}", message);
    JspWriter out = getJspContext().getOut();
    if (message != null) {
      out.println(message);
    } else {
      JspFragment body = getJspBody();
      if (body != null) {
        StringWriter sw = new StringWriter();
        getJspBody().invoke(sw);
        getJspContext().getOut().println(sw.toString());
      }
    }
  }
}