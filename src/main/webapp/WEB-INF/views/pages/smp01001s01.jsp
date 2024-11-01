<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  샘플페이지 01 [${cbase}]
  <div>
    <c-input
      :value="vars.message"
      @keydown="vars.onKeydown"
      />
  </div>
  <div>
  {{ vars.pricetext }}
  </div>
  <template v-if="vars.test"> ABCD </template>
  <form name="form" action="${cbase}/smp/smp01001s02">
    <input type="hidden" name="value1" value="AAA" />
    <input type="hidden" name="value2" value="BBB" />
  </form>
  <button class="btn btn-primary mx-1" @click="vars.doProgress()">
    대기
  </button>
  <button class="btn btn-primary mx-1" @click="vars.doAlert()">
    경고팝업
  </button>
  <button class="btn btn-primary mx-1" @click="vars.doConfirm()">
    확인팝업
  </button>
  <button class="btn btn-primary mx-1" @click="vars.doWinpopup()">
    물리팝업
  </button>
  <div>백틱(``) 사용시 플레이스홀더 \${} 앞 '$' 에 이스케이프 문자열을 넣어주어야 한다. [{{ `\${vars.message}` }}]</div>
  <button class="btn btn-primary mx-1" @click="vars.doSubmit()">
    SUBMIT
  </button>
  <div>
  </div>
</page:ex>
<script:ex name="smp01001s01">
log.debug("MAIN-PAGE-LOADED!");
putAll(vars, {
  pricetext: numToHangul('1234'),
  doProgress: async function() {
    setTimeout(function() {
      log.debug("HIDE-OVERLAY...");
      dialog.progress(false);
    }, 3000);
    log.debug("SHOW-OVERLAY...");
    dialog.progress(true);
  },
  doAlert: async function() {
    dialog.alert("알림!");
  },
  doConfirm: async function() {
    if (await dialog.confirm("확실한가요?")) {
      vars.message = "예";
    } else {
      vars.message = "아니오";
    }
  },
  doWinpopup: async function() {
    dialog.winpopup("/smp/smp01001p01", vars);
  },
  doSubmit: async function() {
    $("form[name='form']")
      .attr("method", "post")
      .attr("enctype", "application/x-www-form-urlencoded")
      .submit();
  },
  onKeydown: async function(e) {
    vars.message = e.target.value;
  },
});
</script:ex>