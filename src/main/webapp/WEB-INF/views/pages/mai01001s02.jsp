<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<ex:page>
  메인페이지 02
  <div>{{ vars.message }}</div>
  <template v-if="vars.test"> ABCD </template>
  <form name="form" action="/mai/mai01001s01">
    <input type="hidden" name="value1" value="" />
    <input type="hidden" name="value2" value="" />
  </form>
  <button class="btn btn-primary" @click="vars.clicked()">
    OK
  </button>
  <div>
  </div>
</ex:page>
<ex:script name="mainpage">
<script>
log.debug("MAIN-PAGE-LOADED!");
vars.value.clicked = async function(e) {
  $("form[name='form']")
    .attr("method", "post")
    .submit();
  await log.debug("OK");
};
</script>
</ex:script>