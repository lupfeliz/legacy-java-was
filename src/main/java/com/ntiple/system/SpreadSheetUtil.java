/**
 * @File        : SpreadSheetUtil.java
 * @Author      : 정재백
 * @Since       : 2023-11-20
 * @Description : 엑셀시트 유틸
 * @Site        : https://devlog.ntiple.com
 * 
 * 다음 형식으로 사용할 수 있다.
 * List<Map<String, Object>> list = repository.findData();
 * List<String> keys = new ArrayList<>();
 * List<String> titles = new ArrayList<>();
 * Map<String, Object> filters = new LinkedHashMap<>();
 * File file = new File("/tmp/abc.xls");
 * SpreadSheetUtil.createSheet('xls', list, keys, titles, filters, file);
 **/
package com.ntiple.system;

import static com.ntiple.commons.ConvertUtil.convert;
import static com.ntiple.commons.ConvertUtil.parseInt;
import static com.ntiple.commons.ConvertUtil.parseStr;
import static com.ntiple.commons.IOUtil.safeclose;
import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.system.Constants.TYPE_XLS;
import static com.ntiple.system.Constants.TYPE_XLSX;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpreadSheetUtil {

  @PostConstruct public void init() {
    log.trace("INIT:{}", SpreadSheetUtil.class);
  }

  public static <T> File createSheet(String type, List<T> list,
    List<String> keys, List<String> titles,
    Map<String, Object> filters,
    File file) throws Exception {
    File ret = null;
    Map<String, Object> map = null;
    OutputStream ostream = null;
    Workbook wbook = null;
    Sheet sheet = null;
    Row row = null;
    Cell cell = null;
    Object t;
    try {
      switch (type) {
      case TYPE_XLS: wbook = new HSSFWorkbook(); break;
      case TYPE_XLSX:
      default:
        wbook = new XSSFWorkbook(); break;
      }
      sheet = wbook.createSheet();
      int rownum = 0;
      for (T itm : list) {
        if (itm instanceof XlsSubject) {
          XlsSubject subject = cast(itm, subject = null);
          {
            row = sheet.createRow(rownum);
            cell = row.createCell(0);
            cell.setCellValue(subject.subject);
            rownum++;
          }
          if (subject.titles != null && subject.titles.size() > 0) {
            row = sheet.createRow(rownum);
            for (int cinx = 0; cinx < subject.titles.size(); cinx++) {
              cell = row.createCell(cinx);
              cell.setCellValue(subject.titles.get(cinx));
            }
            rownum++;
          }
        } else {
          map = convert(itm, new LinkedHashMap<>());
          if (rownum == 0) {
            if (keys == null) {
              keys = new ArrayList<>();
              titles = new ArrayList<>();
              for (String k : map.keySet()) {
                keys.add(k);
                titles.add(k);
              }
            }
            row = sheet.createRow(rownum);
            /** 헤더출력 */
            if (keys != null && titles == null || keys.size() != titles.size()) { titles = keys; }
            if (titles != null) {
              for (int cinx = 0; cinx < titles.size(); cinx++) {
                cell = row.createCell(cinx);
                cell.setCellValue(titles.get(cinx));
              }
            }
            rownum++;
          }
          row = sheet.createRow(rownum);
          int cinx = -1;
          for (String k : map.keySet()) {
            cinx = keys.indexOf(k);
            if (cinx != -1) {
              cell = row.createCell(cinx);
              if ((t = map.get(k)) != null) {
                t = applyFilter(t, k, filters);
                if (t instanceof Integer) {
                  cell.setCellValue(parseInt(t));
                } else {
                  cell.setCellValue(parseStr(t));
                }
              }
            }
          }
          rownum++;
        }
      }
      if (file != null && file.exists()) {
        ret = file;
      } else {
        ret = File.createTempFile("sheet", ".bin");
      }
      ostream = new FileOutputStream(ret);
      wbook.write(ostream);
    } finally {
      safeclose(ostream);
      safeclose(wbook);
    }
    return ret;
  }

  public static Object applyFilter(Object v, String k, Map<String, Object> filters) {
    Object ret = v;
    if (filters == null) { return ret; }
    if (filters.containsKey(k)) {
      Object fobj = filters.get(k);
      if (fobj instanceof Map) {
        Map<String, Object> flt = cast(fobj, flt = null);
        if (flt == null) { return ret; }
        if (flt.containsKey(v)) {
          ret = parseStr(flt.get(v));
        }
      } else if (fobj instanceof Function) {
        Function<Object, Object> fnc = cast(fobj, fnc = null);
        try {
          Object res = fnc.apply(v);
          ret = parseStr(res, "");
        } catch (Exception ignore) { }
      }
    }
    return ret;
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter @Setter @ToString @Builder
  public static class XlsSubject {
    public String subject;
    public List<String> titles;
  }
}
