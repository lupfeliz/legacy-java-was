<%!
/**
 * @File        : launch-script.jsp
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 페이지 런치 스크립트
 * @Site        : https://devlog.ntiple.com
 **/
%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<ex:script name="#launcher#">
<script>
setTimeout(function() {
Vue.createApp({
  setup: function(props, context) {
    $(document.body).removeClass('hide-onload');
    const vars = Vue.ref({
      message: "hello!",
      test: false,
    });
    initEntryScript(function(context) {
      const log = context.log
      <ex:script-names var="scripts"/>
      <c:forEach items="${scripts}" var="name">
        <c:if test="${name != '#launcher#'}">
        try { <ex:script name="${name}" /> } catch (e) { log.debug("E:", e); }
        </c:if>
      </c:forEach>
    });
    return { vars: vars };
  },
}).mount("#page-main");
}, 0)
</script>
</ex:script>
<script> <ex:script name="#launcher#" /> </script>