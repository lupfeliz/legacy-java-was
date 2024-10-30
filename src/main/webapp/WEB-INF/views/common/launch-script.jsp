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
<%-- window 에 중요 변수들이 바인드 되지 않도록 setTimeout 상에서 실행한다. --%>
setTimeout(function() {
Vue.createApp({
  setup: function(props, context) {
    const vars = Vue.ref({ });
    <%-- [ 리소스 구동적재 대기 스크립트 --%>
    setTimeout(function() {
      var body = document.body;
      function fnunload() {
        window.removeEventListener('beforeunload', fnunload);
        body.classList.add('hide-onload');
      }
      <%-- [ CSS 적재 완료 판단 --%>
      function findcss () {
        var s = false;
        var o = false;
        for (var i = document.styleSheets.length; i >= 0; i--) {
          if (!(s = document.styleSheets[i])) { continue; }
          if (!(s = s.rules)) { continue; }
          for (var j = 0; j < s.length; j++) {
            if (!(o = s[j])) { continue; }
            if (String(o.selectorText).startsWith('html#project')) { return true; }
          }
        }
        return false;
      }
      <%-- ] CSS 적재 완료 판단 --%>
      function fnload() {
        if (findcss() || ((c = c + 1) > 1000)) {
          window.addEventListener('beforeunload', fnunload);
          document.removeEventListener('DOMContentLoaded', fnload);
          body.classList.remove('hide-onload');
        } else {
          setTimeout(fnload, 10);
        }
      }
      fnload();
    }, 0);
    <%-- ] 리소스 구동적재 대기 스크립트 --%>
    <%-- [ 페이지 스크립트 실행 --%>
    initEntryScript(function(context) {
      const log = context.log
      <ex:script-names var="scripts"/>
      <c:forEach items="${scripts}" var="name">
        <c:if test="${name != '#launcher#'}">
        try { <ex:script name="${name}" /> } catch (e) { log.debug("E:", e); }
        </c:if>
      </c:forEach>
    });
    <%-- ] 페이지 스크립트 실행 --%>
    return { vars: vars };
  },
}).mount("#page-main");
}, 0)
</script>
</ex:script>
<%-- 실제 #launcher# 스크립트 가 브라우저에 뿌려지는 곳 --%>
<script><ex:script name="#launcher#" /></script>