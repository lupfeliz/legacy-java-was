<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<ex:page>
  샘플페이지 01
  <template v-if="vars.test"> ABCD </template>
  <form name="form" action="/smp/smp01001s02">
    <input type="hidden" name="value1" value="AAA" />
    <input type="hidden" name="value2" value="BBB" />
  </form>
  <button class="btn btn-primary mx-1" @click="vars.doAlert()">
    경고팝업
  </button>
  <button class="btn btn-primary mx-1" @click="vars.doConfirm()">
    확인팝업
  </button>
  <button class="btn btn-primary mx-1" @click="vars.doWinpopup()">
    물리팝업
  </button>
  <div>[{{ vars.message }}]</div>
  <button class="btn btn-primary mx-1" @click="vars.doSubmit()">
    SUBMIT
  </button>
  <div>
  </div>
</ex:page>
<ex:script name="smp01001s01">
<script>
log.debug("MAIN-PAGE-LOADED!");
vars.doAlert = async function() {
  dialog.alert("알림!");
};
vars.doConfirm = async function() {
  if (await dialog.confirm("확실한가요?")) {
    vars.message = "예";
  } else {
    vars.message = "아니오";
  }
};
vars.doWinpopup = async function() {
  dialog.winpopup('about:blank', {});
};
vars.doSubmit = async function() {
  $("form[name='form']")
    .attr("method", "post")
    .attr("enctype", "application/x-www-form-urlencoded")
    .submit();
};
</script>
</ex:script>