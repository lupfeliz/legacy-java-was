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
const vars = Vue.ref({
  message: "hello!",
  test: false
});
Vue.createApp({
  setup: function() {
    return { vars: vars }
  },
  mounted: function() {
    setTimeout(function() {
      vars.value.test = true;
    }, 3000)
    {
      <ex:script-names var="scripts"/>
      <c:forEach items="${scripts}" var="itm">
      <c:if test="${itm != '#launcher#'}"> { <ex:script name="${itm}" /> } </c:if>
      </c:forEach>
    }
  }
}).mount("#page-main");
}, 0)
</script>
</ex:script>
<script> <ex:script name="#launcher#" /> </script>