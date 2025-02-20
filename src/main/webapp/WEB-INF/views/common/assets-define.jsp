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
<!--
<meta httpEquiv="cache-control" content="max-age=0" />
<meta httpEquiv="cache-control" content="no-cache" />
<meta httpEquiv="expires" content="0" />
<meta httpEquiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
<meta httpEquiv="pragma" content="no-cache" />
-->
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
<link rel="preload" href="${cbase}/assets/fonts/pretendard-medium.woff2" as="font" type="font/woff2" crossorigin="anonymous" />
<link rel="preload" href="${cbase}/assets/fonts/pretendard-regular.woff2" as="font" type="font/woff2" crossorigin="anonymous" />
<link rel="stylesheet" href="${cbase}/assets/libs/bootstrap-5.3.3.min.css" />
<link rel="stylesheet" href="${cbase}/assets/libs/bootstrap-icons-1.11.3.min.css" />
<link rel="stylesheet" href="${cbase}/assets/libs/jquery-ui-1.14.0.min.css" />
<link rel="stylesheet" href="${cbase}/assets/libs/jquery.datepicker2-1.0.min.css" />
<link rel="stylesheet" href="${cbase}/assets/libs/jquery.toast-1.3.2.min.css" />
<link rel="stylesheet" href="${cbase}/assets/libs/font-awesome-all-6.6.0.min.css" />
<link rel="stylesheet" href="${cbase}/assets/libs/vanilla-calendar-2.9.10.min.css" />
<link rel="stylesheet" href="${cbase}/assets/fonts/fonts.css" />
<link rel="stylesheet" href="${cbase}/assets/styles/globals.scss" />
<link rel="stylesheet" href="${cbase}/assets/styles/components.scss" />
<link rel="stylesheet" href="${cbase}/assets/styles/util.scss" />
<script src="${cbase}/assets/libs/core-js-3.38.1.min.js"></script>
<script src="${cbase}/assets/libs/crypto-js-4.2.0.min.js"></script>
<script src="${cbase}/assets/libs/abortcontroller-polyfill-1.7.5.min.js"></script>
<%-- <script src="${cbase}/assets/libs/lrucache-1.0.3.min.js"></script> --%>
<script src="${cbase}/assets/libs/jsencrypt-3.3.2.min.js"></script>
<script src="${cbase}/assets/libs/jsbn-1.4.min.js"></script>
<script src="${cbase}/assets/libs/jsbn2-1.4.min.js"></script>
<script src="${cbase}/assets/libs/jsbn-base64-1.4.min.js"></script>
<script src="${cbase}/assets/libs/jsbn-prng4-1.4.min.js"></script>
<script src="${cbase}/assets/libs/jsbn-rng-1.4.min.js"></script>
<script src="${cbase}/assets/libs/jsbn-rsa-1.4.min.js"></script>
<script src="${cbase}/assets/libs/lodash-4.17.21.min.js"></script>
<script src="${cbase}/assets/libs/moment-2.30.1.min.js"></script>
<script src="${cbase}/assets/libs/jquery-3.7.1.min.js"></script>
<script src="${cbase}/assets/libs/jquery-ui-1.14.0.min.js"></script>
<script src="${cbase}/assets/libs/jquery.datepicker2-1.0.min.js"></script>
<script src="${cbase}/assets/libs/jquery.toast-1.3.2.min.js"></script>
<script src="${cbase}/assets/libs/bootstrap-5.3.3.min.js"></script>
<script src="${cbase}/assets/libs/font-awesome-all-6.6.0.min.js"></script>
<script src="${cbase}/assets/libs/vanilla-calendar-2.9.10.min.js"></script>
<script src="${cbase}/assets/libs/popper-2.11.8.min.js"></script>
<script src="${cbase}/assets/libs/vue-3.5.12.min.js"></script>
<script src="${cbase}/assets/ckeditor/ckeditor.js"></script>
<script src="${cbase}/assets/scripts/components.js"></script>
<script src="${cbase}/assets/scripts/entry.js"></script>