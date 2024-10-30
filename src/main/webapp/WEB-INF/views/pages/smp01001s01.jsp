<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<ex:page>
  샘플페이지 01
  <div>{{ vars.message }}</div>
  <template v-if="vars.test"> ABCD </template>
  <form name="form" action="/smp/smp01001s02">
    <input type="hidden" name="value1" value="AAA" />
    <input type="hidden" name="value2" value="BBB" />
  </form>
  <button class="btn btn-primary" @click="vars.clicked()">
    OK
  </button>
  <div>
  </div>
</ex:page>
<ex:script name="smp01001s01">
<script>
log.debug("MAIN-PAGE-LOADED!");
vars.value.clicked = async function(e) {
  $("form[name='form']")
    .attr("method", "post")
    .attr("enctype", "application/x-www-form-urlencoded")
    .submit();
  await log.debug("OK");
};
</script>
</ex:script>