<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  <h1>샘플페이지 03</h1>
  <section>
    <article class="my-1">
      <c-accordion>
        <template #1="{ title }">
          <teleport defer :to="title">
            아코디언 테스트1
          </teleport>
          <div>
            <p>아코디언 컴포넌트 테스트 중입니다.</p>
            <p>html 컨텐츠를 기술합니다</p>
            <p>
              기능버튼도 표시 가능합니다. (스크립트 수행 가능)
              <c-button
                variant="primary"
                @onclick="vars.onClick"
                >
                버튼
              </c-button>
            </p>
          </div>
        </template>
        <template #2="{ title }">
          <teleport defer :to="title">
            <b>아코디언 테스트2</b>
          </teleport>
          <div>
            <p>아코디언 컴포넌트 테스트 중입니다.</p>
            <p>이것은 수동으로 만들어진 예제입니다</p>
          </div>
        </template>
      </c-accordion>
    </article>
    <article class="my-1">
      <c-accordion
        v-model="vars.accordionId"
        @onchange="vars.accordionChange"
        >
        <template v-for="(itm, inx) in vars.accordions" #[inx]="{ title }">
          <teleport defer :to="title">
            <span v-html="itm.title"></span>
          </teleport>
          <div v-html="itm.content"></div>
        </template>
      </c-accordion>
    </article>
  </section>
</page:ex>
<script:ex name="smp01001s03">
const accordions = [ ];
for (let inx = 0; inx < 5; inx++) {
  accordions.push(
    { title: `아코디언-\${inx}`, content: `<p>ABCD \${inx}</p><p>이것은 동적으로 만들어진 예제입니다</p><p>동적으로 만들어진 html 은 스크립트를 수행할수 없습니다.</p>` }
  );
};
putAll(vars, {
  accordions,
  accordionId: '1',
  accordionChange: function(v) {
    log.debug("ACCORDION:", v);
  },
  onClick() {
    log.debug("OK");
  }
});
</script:ex>