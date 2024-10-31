<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<ex:page>
  샘플페이지 01
  <template v-if="vars.test"> ABCD </template>
  <form name="form" action="/smp/smp01001s02">
    <input type="hidden" name="value1" value="AAA" />
    <input type="hidden" name="value2" value="BBB" />
  </form>
  <button class="btn btn-primary" @click="vars.confirm()">
    레이어팝업
  </button>
  <div>[{{ vars.message }}]</div>
  <button class="btn btn-primary" @click="vars.submit()">
    SUBMIT
  </button>
  <div>
  </div>
</ex:page>
<ex:script name="smp01001s01">
<script>
log.debug("MAIN-PAGE-LOADED!");
vars.confirm = async function() {
  if (await dialog.confirm("확실한가요?")) {
    console.log("예");
    vars.message = "예";
  } else {
    console.log("아니오");
    vars.message = "아니오";
  }
};
vars.submit = async function() {
  $("form[name='form']")
    .attr("method", "post")
    .attr("enctype", "application/x-www-form-urlencoded")
    .submit();
};
</script>
</ex:script>