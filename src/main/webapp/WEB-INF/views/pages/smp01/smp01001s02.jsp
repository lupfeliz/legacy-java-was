<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  <h1>샘플페이지 02</h1>
  <section>
    <article>[${session.getAttribute("TEST")}]</article>
    <article>
      <c-input
        v-model="vars.popdata.message"
        :value="vars.popdata.message"
        @onkeydown="vars.onKeydown"
        >
      </c-input>
    </article>
    <article>
      <form name="form" action="/smp/smp01001s01">
        <c-input type="hidden" name="value1" value="123"></c-input>
        <c-input type="hidden" name="value2" value="456"></c-input>
      </form>
      <c-button variant="primary" @onclick="vars.clicked()">
        OK
      </c-button>
    </article>
  </section>
</page:ex>
<script:ex name="smp01001s02">
log.debug("MAIN-PAGE-LOADED!");
/** 팝업데이터는 최상단에 hoisting 되어져야 상호작용이 가능하다 */
const popdata = initpopup();
/** 굳이 vars 에 넣는 이유는 화면에 바인딩 하기 위해 */
vars.popdata = popdata;

log.debug("POPDATA:", vars.popdata);

vars.onKeydown = async function(e) {
  popdata.message = String(e.target.value);
};

vars.clicked = async function(e) {
  // $("form[name='form']")
  //   .attr("method", "post")
  //   .attr("enctype", "application/x-www-form-urlencoded")
  //   .submit();
  log.debug("OK");
};
</script:ex>