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
<script:ex name="#launcher#">
/** window 에 중요 변수들이 바인드 되지 않도록 setTimeout 상에서 실행한다. */
setTimeout(function() {
  /** [ 리소스 구동적재 대기 스크립트 --%>*/
  /** 글꼴과 전역 스타일까지 로드된 시점에서 화면을 보여준다 (깜빡거림 이슈) */
  var body = document.body;
  function fnunload() {
    window.removeEventListener("beforeunload", fnunload);
    body.classList.add("hide-onload");
  };
  /** [ CSS 적재 완료 판단 */
  function findcss() {
    var s = false;
    var o = false;
    for (var i = document.styleSheets.length; i >= 0; i--) {
      if (!(s = document.styleSheets[i])) { continue; };
      if (!(s = s.rules)) { continue; };
      for (var j = 0; j < s.length; j++) {
        if (!(o = s[j])) { continue; };
        if (String(o.selectorText).startsWith("html#project")) { return true; };
      };
    };
    return false;
  };
  /** ] CSS 적재 완료 판단 */
  function fnload() {
    if (findcss() || ((c = c + 1) > 1000)) {
      window.addEventListener("beforeunload", fnunload);
      document.removeEventListener("DOMContentLoaded", fnload);
      body.classList.remove("hide-onload");
    } else {
      setTimeout(fnload, 10);
    }
  };
  fnload();
  /** ] 리소스 구동적재 대기 스크립트 */
}, 0);
setTimeout(function() {
const createApp = Vue.createApp;
const ref = Vue.ref;
const vars = ref({ });
/** 로그 */
const log = { };
/** [ 페이지 스크립트 실행 */
initEntryScript(async function({
  clone,
  dialog,
  dialogvars,
  doModal,
  genId,
  getGlobalTmp,
  getOpenerTmp,
  getParameter,
  getRandom,
  getUri,
  getUrl,
  hierarchy,
  initpopup,
  log,
  numberOnly,
  numToHangul,
  putAll,
  randomChar,
  randomStr,
  setGlobalTmp,
  setOpenerTmp,
  vars,
  M_SHOWN,
  M_HIDDEN,
  }) {
  const vueapp = createApp({
  setup: function(props, context) {
    <script:names var="scripts"/>
    <c:forEach items="${scripts}" var="name">
      <c:if test="${name != '#launcher#'}">
      try { <script:ex name="${name}" /> } catch (e) { log.debug("E:", e); };
      </c:if>
    </c:forEach>
    return {
      vars,
      dialogvars,
    };
  },
  mounted: async function() {
    /** [ 레이어팝업 관련 스크립트 */
    {
      const modalref = this.$refs["dialogvars.modal.ref"];
      const progressref = this.$refs["dialogvars.progress.ref"];
      dialogvars.value.modal.instance = new bootstrap.Modal(modalref, {});
      dialogvars.value.progress.instance = new bootstrap.Modal(progressref, {});
      progressref.addEventListener(M_SHOWN, dialogvars.value.progress.handlevis);
      progressref.addEventListener(M_HIDDEN, dialogvars.value.progress.handlevis);
      modalref.addEventListener(M_HIDDEN, doModal);
    }
    /** ] 레이어팝업 관련 스크립트 */
  },
  beforeUnmount: async function() {
    {
      const modalref = this.$refs["dialogvars.modal.ref"];
      const progressref = this.$refs["dialogvars.progress.ref"];
      progressref.removeEventListener(M_SHOWN, dialogvars.value.progress.handlevis);
      progressref.removeEventListener(M_HIDDEN, dialogvars.value.progress.handlevis);
      modalref.removeEventListener(M_HIDDEN, doModal);
    }
  }
  });
  registerComponent({
    app: vueapp,
    log: log,
  });
  vueapp.mount(document.body);
}, { vars: vars.value, log, cbase: "${cbase}" });
/** ] 페이지 스크립트 실행 */
}, 0);
</script:ex>
<%-- 실제 #launcher# 스크립트 가 브라우저에 뿌려지는 곳 --%>
<script><script:ex name="#launcher#" /></script>