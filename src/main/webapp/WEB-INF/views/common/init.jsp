<%!
/**
 * @File        : init.jsp
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 스크립트 적재 공통 스크립트
 * @Site        : https://devlog.ntiple.com
 **/
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="page" uri="/WEB-INF/libs/tag-page.tld" %>
<%@ taglib prefix="script" uri="/WEB-INF/libs/tag-script.tld" %>
<%!
// <c:set>	변수명에 값을 할당
// <c:out>	값을 출력
// <c:if>	조건식에 해당하는 블럭과 사용될 scope설정
// <c:choose>	다른 언어의 switch와 비슷
// <c:when>	switch문의 case에 해당
// <c:otherwise>	switch문의 default에 해당
// <c:forEach>	다른언어의 loop문 items 속성에 배열을 할당할 수 있음
// <c:if test=""></c:if>
// <c:forEach items=""></c:forEach>
%>