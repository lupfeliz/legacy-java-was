<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  샘플팝업 01
  <div>
    <c-input
      :value="vars.popdata.message"
      :formatter="vars.numeric"
      @onkeydown="vars.onKeydown"
      />
  </div>
  <c-button class="btn-primary mx-1" @onclick="vars.onClick(1)">
    OK
  </c-button>
  <c-button class="btn-primary mx-1" @onclick="vars.onClick(2)">
    닫기
  </c-button>
  <div>
  </div>
</page:ex>
<script:ex name="smp01001p01">
log.debug("MAIN-PAGE-LOADED!");
/** 팝업데이터는 최상단에 hoisting 되어져야 상호작용이 가능하다 */
const popdata = initpopup();
putAll(vars, {
  /** 굳이 vars 에 넣는 이유는 화면에 바인딩 하기 위해 */
  popdata,
  numeric,
  async onKeydown(e) {
    log.debug("CHECK:", e.target.value);
    popdata.message = String(e.target.value);
  },
  async onClick(v) {
    if (v == 1) {
      log.debug("OK");
    } else {
      window.close();
    };
  }
});
</script:ex>