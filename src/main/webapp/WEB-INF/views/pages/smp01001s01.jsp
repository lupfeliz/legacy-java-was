<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  샘플페이지 01 [${cbase}]
  <div>
    <c-input
      v-model="vars.message"
      :value="vars.message"
      @onkeydown="vars.onKeydown"
      @onenter="vars.onEnter"
      @blur="vars.onBlur"
      name="input1"
      label="금액"
      type="numeric"
      required
      maxlength="20"
      minlength="2"
      minvalue="1000"
      maxvalue="999999999999"
      :rtformatter="vars.numeric"
      :formatter="vars.numeric"
      :vrules="auto|check2"
      />
  </div>
  <div>{{ vars.teststr }}</div>
  <div>{{ vars.pricetext }}원</div>
  <template v-if="vars.test"> ABCD </template>
  <form name="form" action="${cbase}/smp/smp01001s02">
    <input type="hidden" name="value1" value="AAA" />
    <input type="hidden" name="value2" value="BBB" />
  </form>
  <c-button class="btn-primary mx-1" @onclick="vars.doProgress()">
    대기
  </c-button>
  <c-button class="btn-primary mx-1" @onclick="vars.doAlert()">
    경고팝업
  </c-button>
  <c-button class="btn-primary mx-1" @onclick="vars.doConfirm()">
    확인팝업
  </c-button>
  <c-button class="btn-primary mx-1" @onclick="vars.doWinpopup()">
    물리팝업
  </c-button>
  <div>백틱(``) 사용시 플레이스홀더 \${} 앞 '$' 에 이스케이프 문자열을 넣어주어야 한다. [{{ `\${vars.message}` }}]</div>
  <c-button class="btn-primary mx-1" @onclick="vars.doSubmit()">
    SUBMIT
  </c-button>
  <div>
  </div>
</page:ex>
<script:ex name="smp01001s01">
log.debug("MAIN-PAGE-LOADED!");
putAll(vars, {
  numeric,
  pricetext: numToHangul('1234567890'),
  teststr: `
  \ 텍스트 문자열 테스트 중입니다.
  \ 백틱을 사용하는 경우 여러줄에 걸쳐서 작성 할 수 있습니다.
  \ 개행문자 앞에 백슬래시를 붙여 주어야 합니다.
  `,
  async doProgress() {
    setTimeout(function() {
      log.debug("HIDE-OVERLAY...");
      dialog.progress(false);
    }, 3000);
    log.debug("SHOW-OVERLAY...");
    dialog.progress(true);
  },
  async doAlert() {
    dialog.alert("알림!");
  },
  async doConfirm() {
    if (await dialog.confirm("확실한가요?")) {
      vars.message = "예";
    } else {
      vars.message = "아니오";
    }
  },
  async doWinpopup() {
    dialog.winpopup("/smp/smp01001p01", vars);
  },
  async doSubmit() {
    $("form[name='form']")
      .attr("method", "post")
      .attr("enctype", "application/x-www-form-urlencoded")
      .submit();
  },
  async onEnter(e) {
    log.debug("ENTER:", vars.message);
  },
  async onKeydown(e) {
    // log.debug("keyDown:", e.target.value);
    // vars.message = e.target.value;
    // update();
  },
  async onBlur(e) {
    log.debug("BLUR!");
  },
});
</script:ex>