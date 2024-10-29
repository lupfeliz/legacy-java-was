/**
 * @File        : CustomTag.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : Custom taglib sample (hello)
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.system;

import static com.ntiple.commons.ConvertUtil.cast;
import static com.ntiple.system.WebUtil.curRequest;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
public class CustomTags  {
  private static final Pattern PTN_ST_SCRIPT = Pattern.compile("^[<]script[^>]*>");
  private static final Pattern PTN_ED_SCRIPT = Pattern.compile("[<]/script[^>]*>$");
  private static final String ATTR_KEY_SCRIPT_STORE = String.valueOf(CustomTags.class) + ".scriptStore";

  private static final Map<String, String> getStore() {
    Map<String, String> store = null;
    store = cast(curRequest().getAttribute(ATTR_KEY_SCRIPT_STORE), store);
    if (store == null) { curRequest().setAttribute(ATTR_KEY_SCRIPT_STORE, store = new LinkedHashMap<>()); }
    return store;
  }

  public static class SimpleTag extends SimpleTagSupport {
    public void doTag() throws JspException, IOException {
      JspFragment body = this.getJspBody();
      JspWriter out = this.getJspContext().getOut();
      if (body != null) {
        StringWriter sw = new StringWriter();
        body.invoke(sw);
        out.print(sw);
      }
    }
  }

  @Setter @Getter
  public static class LaunchScript extends SimpleTagSupport {
    private String name;
    public void doTag() throws JspException, IOException {
      String content = "";
      Map<String, String> store = getStore();
      JspFragment body = this.getJspBody();
      JspWriter out = this.getJspContext().getOut();
      if (name == null) { name = "ROOT"; }
      // log.debug("BODY:{}", body);
      if (body != null) {
        StringWriter sw = new StringWriter();
        body.invoke(sw);
        String str = String.valueOf(sw).trim();
        str = PTN_ST_SCRIPT.matcher(str).replaceAll("");
        str = PTN_ED_SCRIPT.matcher(str).replaceAll("");
        store.put(name, str);
        // log.debug("STORE:{} / {}", name, store);
      } else {
        content = store.get(name);
        // log.debug("STORE:{} / {}", name, store);
        if (content == null) { content = ""; }
      }
      out.print(content);
    }
  }

  @Setter @Getter
  public static class LaunchScriptNames extends SimpleTagSupport {
    private String var;
    public void doTag() throws JspException, IOException {
      Map<String, String> store = getStore();
      if (var == null) { var = "script-names"; }
      // log.debug("STORE-KEYS:{}", store.keySet());
      this.getJspContext().setAttribute(var, store.keySet());
    }
  }
}