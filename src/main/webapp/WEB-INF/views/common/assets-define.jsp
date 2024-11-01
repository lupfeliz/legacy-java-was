<%!
/**
 * @File        : assets.jsp
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 스크립트 적재 공통 스크립트
 * @Site        : https://devlog.ntiple.com
 **/
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<link rel="stylesheet" href="<c:out value="${base}"/>/assets/libs/bootstrap-5.3.3.min.css" />
<link rel="stylesheet" href="<c:out value="${base}"/>/assets/libs/jquery-ui-1.14.0.min.css" />
<link rel="stylesheet" href="<c:out value="${base}"/>/assets/fonts/fonts.css" />
<link rel="stylesheet" href="<c:out value="${base}"/>/assets/styles/globals.scss" />
<link rel="stylesheet" href="<c:out value="${base}"/>/assets/styles/util.scss" />
<script src="<c:out value="${base}"/>/assets/libs/core-js-3.38.1.min.js"></script>
<script src="<c:out value="${base}"/>/assets/libs/jquery-3.7.1.min.js"></script>
<script src="<c:out value="${base}"/>/assets/libs/jquery-ui-1.14.0.min.js"></script>
<script src="<c:out value="${base}"/>/assets/libs/bootstrap-5.3.3.min.js"></script>
<script src="<c:out value="${base}"/>/assets/libs/vue-3.5.12.min.js"></script>
<script src="<c:out value="${base}"/>/assets/ckeditor/ckeditor.js"></script>
<script src="<c:out value="${base}"/>/assets/scripts/entry.js"></script>