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
const getCurrentInstance = Vue.getCurrentInstance;
const ref = Vue.ref;
const vars = ref({ });
/** 로그 */
const log = { };
/** [ 페이지 스크립트 실행 */

initEntryScript(async function($SCRIPTPRM) {
  const {
  vars,
  BIND_VALUES,
  KEYCODE_REV_TABLE,
  KEYCODE_TABLE,
  MOUNT_HOOK_PROCS,
  UNMOUNT_HOOK_PROCS,
  cancelEvent,
  clone,
  copyExclude,
  copyExists,
  dialog,
  dialogvars,
  doModal,
  equals,
  equalsIgnoreCase,
  find,
  genId,
  getFrom,
  getGlobalTmp,
  getOpenerTmp,
  getParameter,
  getPattern,
  getRandom,
  getText,
  getUri,
  getUrl,
  hangul,
  hierarchy,
  initpopup,
  isEvent,
  item,
  lodash,
  log,
  lpad,
  max,
  mergeAll,
  mergeObj,
  min,
  near,
  num,
  numberOnly,
  numeric,
  numToHangul,
  nval,
  Paging,
  put,
  putAll,
  px2rem,
  randomChar,
  randomStr,
  registFormElement,
  rem2px,
  replaceLink,
  rpad,
  setGlobalTmp,
  setOpenerTmp,
  sleep,
  sort,
  strm,
  swap,
  trim,
  until,
  update,
  val,
  } = $SCRIPTPRM;
  const vueapp = createApp({
  setup: function(props, context) {
    const self = getCurrentInstance();
    const refs = function(name) {
      let ret = self.refs[name];
      if (!ret) { ret = {}; };
      return ret;
    };
    log.debug("SELF:", self, refs);
    <script:names var="scripts"/>
    <c:forEach items="${scripts}" var="name">
      <c:if test="${name != '#launcher#'}">
      try { <script:ex name="${name}" /> } catch (e) { log.debug("E:", e); };
      </c:if>
    </c:forEach>
    return putAll(BIND_VALUES({ props, context }), {
      app: vueapp, instance: getCurrentInstance(),
    });
  },
  mounted: async function() { for (const proc of MOUNT_HOOK_PROCS) { proc(this); }; },
  beforeUnmount: async function() { for (const proc of UNMOUNT_HOOK_PROCS) { proc(this); }; }
  });
  putAll($SCRIPTPRM, { app: vueapp });
  registerComponent($SCRIPTPRM);
  vueapp.mount(document.body);
}, { vars, log, cbase: "${cbase}" });
/** ] 페이지 스크립트 실행 */
}, 0);
</script:ex>
<%-- 실제 #launcher# 스크립트 가 브라우저에 뿌려지는 곳 --%>
<script><script:ex name="#launcher#" /></script>