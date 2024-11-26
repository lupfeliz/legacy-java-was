<%!
/**
 * @File        : header.jsp
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 메뉴탐색기
 * @Site        : https://devlog.ntiple.com
 **/
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="script" uri="/WEB-INF/libs/tag-script.tld" %>
<c-aside
  position="right"
  :visible="pagevars.aside"
  >
  ASIDE
</c-aside>