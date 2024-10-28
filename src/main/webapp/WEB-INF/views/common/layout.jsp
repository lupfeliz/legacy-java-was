<%!
/**
 * @File        : layout.jsp
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 페이지 기본 레이아웃
 * @Site        : https://devlog.ntiple.com
 **/
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html>
<html lang="ko" id="project">
  <head>
    <tiles:insertAttribute name="defines" />
    <tiles:insertAttribute name="assets" />
  </head>
  <body>
    <tiles:insertAttribute name="header"/>
    <main id="page-main" class="container">
      <tiles:insertAttribute name="body"/>
    </main>
    <tiles:insertAttribute name="footer"/>
    <tiles:insertAttribute name="dcontainer" />
    <tiles:insertAttribute name="launch" />
  </body>
</html>