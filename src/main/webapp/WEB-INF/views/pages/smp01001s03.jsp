<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  <h1>샘플페이지 03</h1>
  <section>
    <article>
      <c-accordion
        v-model="vars.accordionId"
        @onchange="vars.accordionChange"
        >
        <template v-for="(itm, inx) in vars.accordions" #[inx]="{ title }">
          {{ title(itm.title) }}
          <div v-html="itm.content"></div>
        </template>
      </c-accordion>
    </article>
  </section>
</page:ex>
<script:ex name="smp01001s03">
const accordions = [ ];
for (let inx = 0; inx < 10; inx++) {
  accordions.push(
    { title: `아코디언-\${inx}`, content: `<div>ABCD \${inx}</div>` }
  );
};
putAll(vars, {
  accordions,
  accordionId: '1',
  accordionChange: function(v) {
    log.debug("ACCORDION:", v);
  }
});
</script:ex>