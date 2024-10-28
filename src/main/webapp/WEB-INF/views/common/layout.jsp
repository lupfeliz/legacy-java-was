<%!
/**
 * @File        : layout.jsp
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 페이지 기본 레이아웃
 * @Site        : https://devlog.ntiple.com
 **/
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<!DOCTYPE html>
<html lang="ko" id="project">
  <head>
    <meta charset="UTF-8">
    <title>테스트</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <tiles:insertAttribute name="assets" />
  </head>
  <body>
    <main id="app" class="container">
      <tiles:insertAttribute name="header"/>
      <tiles:insertAttribute name="body"/>
      <tiles:insertAttribute name="footer"/>
    </main>
    <tiles:insertAttribute name="launch" />
  </body>
</html>