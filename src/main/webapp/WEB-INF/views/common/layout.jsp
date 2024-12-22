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
<%@ page import="com.ntiple.system.Constants" %>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<!DOCTYPE html>
<html lang="ko" id="project">
  <head>
    <jsp:include page="meta-define.jsp" />
    <jsp:include page="assets-define.jsp" />
  </head>
  <body class="hide-onload">
    <div>
    <c:if test="${reqtype == 'S' || reqtype == 's'}">
      <jsp:include page="header.jsp" />
    </c:if>
    <main class="container">
      <jsp:include page="../pages/${request.getAttribute(Constants.ATTR_KEY_LAYOUT_BODY)}.jsp" />
    </main>
    <c:if test="${reqtype == 'S' || reqtype == 's'}">
      <jsp:include page="footer.jsp" />
    </c:if>
    <jsp:include page="dialog-container.jsp" />
    <jsp:include page="launch-script.jsp" />
    </div>
  </body>
</html>