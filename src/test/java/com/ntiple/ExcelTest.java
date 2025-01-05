package com.ntiple;

import static com.ntiple.commons.ConvertUtil.convert;
import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.commons.StringUtil.cat;
import static com.ntiple.system.Constants.TYPE_XLS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ntiple.TestUtil.TestLevel;
import com.ntiple.system.SpreadSheetUtil;
import com.ntiple.system.SpreadSheetUtil.XlsSubject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelTest {
  @Test public void test1() throws Exception {
    if (!TestUtil.isEnabled("test1", TestLevel.MANUAL)) { return; }
    List<Object> list = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    Map<String, Object> filters = newMap();
    list.add(XlsSubject.builder()
      .subject("테스트")
      .titles(keys)
      .build());
    for (int inx = 0; inx < 10; inx++) {
      list.add(convert(new String[][] {
        { "uid", cat(inx) },
        { "title", cat("타이틀-", inx) },
        { "content", cat("내용-", inx) }
      }, newMap()));
    }
    list.add(XlsSubject.builder()
      .subject("테스트2")
      .titles(keys)
      .build());
    keys.add("uid");
    keys.add("title");
    keys.add("content");
    log.debug("LIST:{}", list);
    File file = new File("/home/coder/pub/Documents/test.xls");
    SpreadSheetUtil.createSheet(TYPE_XLS, list, keys, keys, filters, file);
    log.debug("FILE:{}", file.getAbsolutePath());
  }
}
