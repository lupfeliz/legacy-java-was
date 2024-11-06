<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
<page:ex>
  <h4>샘플페이지 01</h4>
  <template v-if="vars.test"> ABCD </template>
  <c-form
    ref="form"
    name="form"
    action="${cbase}/smp/smp01001s02"
    :validctx="vars.validctx"
    >
    <div>
      <i class="fa-solid fa-ghost"></i>
      <i class="bi bi-backspace"></i>
      <i class="bi bi-eye"></i>
      <i class="bi bi-eye-slash"></i>
    </div>
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
        >
      </c-input>
    </div>
    <div>
      <c-check
        v-model="vars.check"
        :checked="vars.check == 'Y'"
        form="form"
        name="check"
        label="체크박스"
        value="Y"
        required
        vrules="auto"
        >
      </c-check>
    </div>
    <div>
      <c-check
        v-model="vars.check"
        :checked="vars.check != 'Y'"
        name="check"
        label="체크박스"
        value="N"
        >
      </c-check>
    </div>
    <div>
      <c-check
        v-for="(itm, inx) in vars.checklist"
        v-model="vars.checklist"
        form="form"
        :name="'checklist.' + inx"
        :checked="vars.checklist[inx] == 'Y'"
        label="체크리스트"
        value="Y"
        nvalue="N"
        required
        vrules="auto|atleast:2|atmost:3"
        >
      </c-check>
    </div>
    <div>
      <div class="input-group">
        <c-input
          type="password"
          >
        </c-input>
        <c-input></c-input>
        <c-input></c-input>
        <c-input></c-input>
      </div>
    </div>
    <div>
      <c-select></c-select>
      <c-select></c-select>
      <c-select></c-select>
    </div>
    <div>
      <div class="input-group">
        <c-input></c-input>
        <span class="input-group-text">@</span>
        <c-select
          v-model="vars.select"
          form="form"
          name="select"
          label="선택박스"
          required
          :options="[
            { name: '선택해주세요', value: '' },
            'gmail.com',
            'hotmail.com',
            'honmail.com',
            'naver.com',
            { name: '직접입력', value: '_' }
          ]"
          vrules="auto"
          variant="primary"
          >
        </c-select>
      </div>
    </div>
    <div>
      <c-datepicker
        >
      </c-datepicker>
    </div>
  </c-form>
  <%-- <div class="x-mark" data-element-id="test">테스트</div> --%>
  <div>{{ vars.numToHangul(vars.price) }}원</div>
  <div>[{{ vars.check }}]</div>
  <div>[{{ vars.select }}]</div>
  <div>[{{ vars.checklist }}]</div>
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
  <c-button class="btn-primary mx-1" @onclick="vars.doGetJson">
    JSON
  </c-button>
  <c-button class="btn-primary mx-1" @onclick="vars.testEncrypt">
    CRYPTO
  </c-button>
  <div>
  </div>
</page:ex>
<script:ex name="smp01001s01">
log.debug("MAIN-PAGE-LOADED!");
putAll(vars, {
  numeric,
  numToHangul,
  price: numeric(1000),
  check: "Y",
  checklist: ["Y", "", "", "", ""],
  select: "hotmail.com",
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
    const $form = $("form[name='form']");
    if (await validateForm($form, result)) {
      log.debug("SUCCESS.");
      formSubmit($form);
    } else {
      log.debug("FAIL.", result);
      await dialog.alert(result.message);
      $(result.element).focus();
    }
  },
  async doGetJson() {
    const result = {};
    const $form = $("form[name='form']");
    if (await validateForm($form, result)) {
      let json = JSON.stringify(await formToJson($form));
      log.debug("SUCCESS.", json);
      await dialog.alert(String(`결과값 : \${json}`));
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
  validctx: {
    check2: function(v, p) {
      /** 숫자 2 는 사용할수 없도록 하는 규칙. */
      log.debug("VALIDATION-CHECK2:", v, p, String(v).indexOf("2"));
      if (String(v.value).indexOf("2") != -1) { return `숫자 "2" 는 사용할 수 없어요.`; };
      return true;
    }
  },
  async testEncrypt() {
    const keys = crypto.rsa.keygen();
    let msg = crypto.rsa.encrypt("암호화 테스트중입니다.", keys[0]);
    const res = await api.post("smp01001a03", {
      message: msg,
      key: keys[1]
    });
    log.debug("RESULT:", res);
  },
});
</script:ex>