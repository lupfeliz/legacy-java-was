<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  샘플페이지 02
  <div>{{ vars.message }}</div>
  <template v-if="vars.test"> ABCD </template>
  <form name="form" action="/smp/smp01001s01">
    <input type="hidden" name="value1" value="123" />
    <input type="hidden" name="value2" value="456" />
  </form>
  <button class="btn btn-primary" @click="vars.clicked()">
    OK
  </button>
  <div>
  </div>
</page:ex>
<script:ex name="smp01001s02">
log.debug("MAIN-PAGE-LOADED!");

/** 팝업판단은 opener 로 한다 */
if (opener) {
  const odata = getOpenerTmp(getParameter("tid"));
  log = odata.OPENER_LOG;
  log.debug("OK");
};

// getOpenerTmp();
vars.clicked = async function(e) {
  // $("form[name='form']")
  //   .attr("method", "post")
  //   .attr("enctype", "application/x-www-form-urlencoded")
  //   .submit();
  log.debug("OK");
};
</script:ex>