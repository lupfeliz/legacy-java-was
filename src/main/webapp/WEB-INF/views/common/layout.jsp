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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko" id="project">
  <head>
    <tiles:insertAttribute name="defines" />
    <tiles:insertAttribute name="assets" />
  </head>
  <body class="hide-onload">
    <div>
    <c:if test="${reqtype == 's'}">
      <tiles:insertAttribute name="header"/>
    </c:if>
    <main class="container">
      <tiles:insertAttribute name="body"/>
    </main>
    <c:if test="${reqtype == 's'}">
      <tiles:insertAttribute name="footer"/>
    </c:if>
    <tiles:insertAttribute name="dcontainer" />
    <tiles:insertAttribute name="launch" />
    </div>
  </body>
</html>