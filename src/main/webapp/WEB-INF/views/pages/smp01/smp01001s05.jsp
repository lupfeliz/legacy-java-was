<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  <h1>샘플페이지 05</h1>
  <section>
    <article>
      <c-navbar
        >
        <template #brand="{ href }">
          {{ href("/smp/smp01001s05") }}
          샘플프로그램
        </template>
        <template #1>
          <a class="nav-link"> OK </a>
        </template>
        <template #2>
          <a class="nav-link">ABCD</a>
        </template>
        <template #form>
          <c-input class="form-control me-2" type="search" placeholder="Search" aria-label="Search"></c-input>
          <c-button variant="primary">
            <i class="bi bi-search"></i>
          </c-button>
        </template>
      </c-navbar>
    </article>
  </section>
</page:ex>
<script:ex name="smp01001s05">
putAll(vars, {
});
</script:ex>