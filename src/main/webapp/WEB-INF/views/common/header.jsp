<%!
/**
 * @File        : header.jsp
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 머리말 컨텐츠
 * @Site        : https://devlog.ntiple.com
 **/
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<header class="container">
  HEADER
  <section>
    <article>
      <c-navbar>
        <template #brand="{ href }">
          {{ href("/") }}
          <i class="bi bi-person-arms-up"></i>
          샘플프로그램
        </template>
        <template #1>
          <a class="nav-link" href="/smp/smp01001s01"> 샘플1 </a>
        </template>
        <template #2>
          <a class="nav-link" href="/smp/smp01001s02"> 샘플2 </a>
        </template>
        <template #3>
          <a class="nav-link" href="/smp/smp01001s03"> 샘플3 </a>
        </template>
        <template #4>
          <a class="nav-link" href="/smp/smp01001s04"> 샘플4 </a>
        </template>
        <template #5>
          <a class="nav-link" href="/smp/smp01001s05"> 샘플5 </a>
        </template>
        <template #6>
          <a class="nav-link" href="/smp/smp01001s06"> 샘플6 </a>
        </template>
      </c-navbar>
    </article>
  </section>
  <jsp:include page="aside.jsp"/>
</header>