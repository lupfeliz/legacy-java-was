<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  <h1>샘플페이지 06</h1>
  <section>
    <article>
      <c-tabview
        v-model="vars.tabindex"
        >
        <template v-for="(itm, inx) in vars.tabs" #[inx]="{ title }">
          <teleport defer :to="title">
          {{ itm.name }}
          </teleport>
          <template v-if="inx == 0">
            <div>
              <p>첫번째 탭입니다</p>
              <br/>
              <br/>
              <br/>
              <br/>
              <p>탭구분은 vars.tabs 엘리먼트를 변경시켜 사용하세요
              <br/>
              <br/>
              <br/>
              <br/>
              <c-button
                variant="primary"
                @onclick="vars.onClick"
                >
                버튼테스트
              </c-button>
            </div>
          </template>
          <template v-if="inx == 1">
            <div>
            <p> 두번째 탭 </p>
            </div>
          </template>
          <template v-if="inx == 2">
            <div>
            <p> 세번째 탭 </p>
            </div>
          </template>
        </template>
      </c-tabview>
    </article>
  </section>
</page:ex>
<script:ex name="smp01001s06">
putAll(vars, {
  tabindex: 0,
  tabs: [
    { name: "첫번째 탭" },
    { name: "두번째 탭"},
    { name: "세번째"},
  ],
  onClick() {
    log.debug("OK");
  }
});
</script:ex>