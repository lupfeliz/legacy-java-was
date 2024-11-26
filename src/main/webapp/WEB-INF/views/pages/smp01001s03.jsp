<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  샘플페이지 03
  <div>
    <c-accordion>
      <template
        v-for="k in 10"
        #[k]="{ title }"
        >
        {{ title(`<span>아코디언 \${k}</span>`) }}
        <div>A {{ k }}</div>
      </template>
    </c-accordion>
  </div>
</page:ex>
<script:ex name="smp01001s03">
</script:ex>