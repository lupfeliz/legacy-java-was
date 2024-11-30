<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  <h1>샘플페이지 04</h1>
  <section>
    <article>
      <c-pagination
        :rows="15"
        :pages="10"
        :current="${request.getParameter("page")}"
        :total="999"
        @onchange="vars.paginationClick"
        >
      </c-pagination>
    </article>
    <article>
      <c-pagination
        :rows="15"
        :pages="10"
        :current="${request.getParameter("page")}"
        :total="999"
        input="true"
        @onchange="vars.paginationClick"
        >
      </c-pagination>
    </article>
    <article>
      <c-pagination
        :rows="15"
        :pages="10"
        :current="${request.getParameter("page")}"
        :total="999"
        href="/smp/smp01001s04?page=#page"
        >
      </c-pagination>
    </article>
  </section>
</page:ex>
<script:ex name="smp01001s04">
putAll(vars, {
  pagination: {
    rows: 15,
    pages: 10,
    current: 16,
    total: 999,
  },
  paginationClick(e, v) {
    log.debug("PAGE:", e, v);
  }
});
</script:ex>