<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  <h4>샘플페이지 01</h4>
  <template v-if="vars.test"> ABCD </template>
  <c-form
    ref="form"
    name="form"
    action="${cbase}/smp/smp01001s02"
    >
    <input type="hidden" name="value1" value="AAA" />
    <input type="hidden" name="value2" value="BBB" />
    <div>
      <c-input
        v-model="vars.price"
        :value="vars.price"
        @onkeydown="vars.onKeydown"
        @onenter="vars.onEnter"
        @onblur="vars.onBlur"
        name="price"
        label="금액"
        placeholder="금액을 입력해 주세요"
        type="numeric"
        required
        maxlength="20"
        minlength="2"
        minvalue="1000"
        maxvalue="999999999999"
        form="form"
        :formatter="vars.numeric"
        :rtformatter="vars.numeric"
        vrules="auto|check2"
        />
    </div>
    <div>
      <c-check
        v-model="vars.check"
        required
        vrules="auto"
        />
    </div>
  </c-form>
  <div>{{ vars.numToHangul(vars.price) }}원</div>
  <c-button class="btn-primary mx-1" @onclick="vars.doProgress">
    대기
  </c-button>
  <c-button class="btn-primary mx-1" @onclick="vars.doAlert">
    경고팝업
  </c-button>
  <c-button class="btn-primary mx-1" @onclick="vars.doConfirm">
    확인팝업
  </c-button>
  <c-button class="btn-primary mx-1" @onclick="vars.doWinpopup">
    물리팝업
  </c-button>
  <div>백틱(``) 사용시 플레이스홀더 \${} 앞 '$' 에 이스케이프 문자열을 넣어주어야 한다. [{{ `\${vars.message}` }}]</div>
  <div>{{ vars.teststr }}</div>
  <c-button class="btn-primary mx-1" @onclick="vars.doSubmit">
    SUBMIT
  </c-button>
  <div>
  </div>
</page:ex>
<script:ex name="smp01001s01">
log.debug("MAIN-PAGE-LOADED!");
putAll(vars, {
  numeric,
  numToHangul,
  price: "",
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
    const result = {};
    if (await validateForm($("form[name='form']"), result)) {
      log.debug("SUCCESS.");
    } else {
      log.debug("FAIL.", result);
      await dialog.alert(result.message);
      $(result.element).focus();
    }
  },
  async onEnter(e) {
    log.debug("ENTER:", vars.message);
  },
  async onKeydown(e) {
    log.debug("keyDown:", e.target.value);
  },
  async onBlur(e) {
    log.debug("BLUR!");
  },
});
</script:ex>