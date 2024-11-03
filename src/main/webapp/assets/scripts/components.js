/**
 * @File        : components.js
 * @Author      : 정재백
 * @Since       : 2024-10-31
 * @Description : 입력컴포넌트
 * @Site        : https://devlog.ntiple.com
 **/

function registerComponent($SCRIPTPRM) {
  const ref = Vue.ref;
  const useAttrs = Vue.useAttrs;
  const defineProps = Vue.defineProps;
  const defineComponent = Vue.defineComponent;
  const getCurrentInstance = Vue.getCurrentInstance;
  const {
  BIND_VALUES,
  KEYCODE_REV_TABLE,
  KEYCODE_TABLE,
  MOUNT_HOOK_PROCS,
  UNMOUNT_HOOK_PROCS,
  cancelEvent,
  clone,
  copyExclude,
  copyExists,
  dateStrFormat,
  dialog,
  dialogvars,
  doModal,
  equals,
  equalsIgnoreCase,
  find,
  formatDate,
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
  lodash,
  log,
  lpad,
  makeDate,
  max,
  mergeAll,
  mergeObj,
  min,
  near,
  nitem,
  num,
  numberOnly,
  numeric,
  numToHangul,
  nval,
  Paging,
  parseDate,
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
  validateForm,
  } = $SCRIPTPRM;
  const app = $SCRIPTPRM.app;
  const { debounce, throttle } = lodash;
  {
    const name = "c-form";
    const CForm = defineComponent({
      name,
      template: `
      \ <form
      \   v-bind="attrs"
      \   :ref="vars.elem"
      \   :data-form-ref="vars.formId"
      \   >
      \   <slot />
      \ </form>`,
      props: {
      },
      setup: function(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const formId = genId();
        const vars = {
          elem: ref(),
          formId,
          items: [],
        };
        function registFormElement(compo, elem) {
          log.debug("REGIST-FORM:", compo, elem);
          vars.items.push({ compo, elem });
        };

        function parseRules(props, attrs, elem) {
          let ret = "";
          let list = ((props && props.vrules) ? String(props.vrules) : "").split(/\s*\|\s*/);
          let auto = false;
          let pinx = 0;
          for (let inx = 0; inx < list.length; inx++) {
            const item = list[inx];
            switch (item) {
            case "auto": {
              auto = true;
              list.splice(inx, 1);
              inx--;
            } break;
            case "required": {
              list.splice(inx, 1);
              list.splice(0, 0, "required");
              pinx = 1;
            } break;
            };
          };
          if (auto) {
            if (props.required !== undefined && list.indexOf("required") == -1) {
              list.splice(pinx, 0, `required`);
              pinx++;
            };
            {
              /** type */
              log.trace("DATA-TYPE:", props.type);
              let rule = "";
              switch (props.type) {
              case "number":   rule = "number";   break;
              case "numeric":  rule = "numeric";  break;
              case "alpha":    rule = "alpha";    break;
              case "alphanum": rule = "alphanum"; break;
              case "ascii":    rule = "ascii";    break;
              case "email":    rule = "email";    break;
              // case C.PASSWORD: rule = C.PASSWORD; break
              // case C.DATE: rule = C.DATE; break
              // case C.DATETIME: rule = C.DATETIME; break
              };
              if (rule) {
                list.splice(pinx, 0, `${rule}`);
                pinx++;
              };
            };
            {
              /** min-max length */
              let min = 0;
              let max = 0;
              if (props.minlength !== undefined) { min = Number(props.minlength); };
              if (props.maxlength !== undefined) { max = Number(props.maxlength); };
              if (min != 0 || max != 0) {
                list.splice(pinx, 0, `len:${min},${max}`);
                pinx++;
              };
            };
            {
              /** min-max value */
              let minv = 0;
              let maxv = 0;
              if (props.minvalue !== undefined) { minv = Number(props.minvalue); };
              if (props.maxvalue !== undefined) { maxv = Number(props.maxvalue); };
              if (minv != 0) {
                list.splice(pinx, 0, `minv:${minv}`);
                pinx++;
              };
              if (maxv != 0) {
                list.splice(pinx, 0, `maxv:${maxv}`);
                pinx++;
              };
            };
          };
          log.trace("VALID-RULES:", list, props.vrules);
          ret = list.join("|");
          return ret;
        };
        const DATA_VALID_INX = "data-valid-inx";
        async function validateForm(opt, validations) {
          log.trace("VALIDATE-FORM", vars.items[formId]);
          let ret = true;
          const elist = [];
          // if (form)
          {
            let qlst = "";
            LOOP: for (let inx in vars.items) {
              const item = vars.items[inx];
              if (!item || !item.compo) { continue LOOP; };
              // const { props, vars } = modelValue(item.self()) as any
              const { attrs, props } = item.compo;
              const ivars = item.vars;
              const elem = item.elem;
              // const ref = item.ref()
              // const elem = ref.current ? ref.current : C.UNDEFINED
              item.rules = parseRules(props, attrs, elem);
              if (elem) {
                log.trace("ELEM:", elem);
                elem.setAttribute(DATA_VALID_INX, inx);
                if (qlst) { qlst = `${qlst},`; };
                qlst = `${qlst}[${DATA_VALID_INX}="${inx}"]`;
                elist[inx] = item;
              };
              // vars.valid.error = false
            };
            const list = $(document.body).find(qlst);
            for (let inx = 0; inx < list.length; inx++) {
              const elem = list[inx];
              const vinx = Number(elem.getAttribute(DATA_VALID_INX));
              const item = elist[vinx];
              elem.removeAttribute(DATA_VALID_INX);
              item.seq = inx;
            };
            elist.sort(function (a, b) { return Number(a.seq) - Number(b.seq); });
            log.trace("VALIDATION-COUNT:", elist.length);
            LOOP: for (const item of elist) {
              if (!item || !item.compo) { continue LOOP; };
              // if (!item.self) { continue }
              let res = await validate(item, opt, validations);
              if (res === false) {
                log.trace("INVALID:", item, opt);
                // const { props } = item.self()
                // putAll(opt, { element: item.el })
                // if (props.onError) {
                //   props.onError(opt)
                // } else if (vform.current.onError) {
                //   vform.current.onError(opt)
                // }
                putAll(opt, { valid: false, element: item.elem });
                ret = false;
                break;
              };
            };
            // app.state(1)
          };
          return ret;
        };
        function validate(item, opt, validations) {
          return new Promise(function (resolve, _) {
            let ret = true;
            let result;
            // const self = modelValue(item.self()) as any
            // const { props, vars } = self
            // let { value } = self
            const { props, vars } = item.compo;
            let value = props.modelValue;
            try {
              const rlist = String(item.rules).split(/\|/g);
              let label = props.label ? props.label : props.name;
              let name = props.name;
              for (const rule of rlist) {
                const rdata = rule.split(/\:/g);
                const rparm = rdata.length > 1 ? String(rdata[1]).split(/\,/g) : [];
                /** NULL, UNDEFINED 값 통일 */
                value = nval(value, undefined);
                log.trace("RULE:", rule, rdata, value, props);
                if (!rdata || rdata.length < 1) { continue; };
                if (rdata[0] == "atleast" && /\.[0-9]+$/g.test(name)) {
                  name = name.replace(/\.[0-9]+$/g, "");
                  label = label.replace(/\.[0-9]+$/g, "");
                  // value = (props.model : props.model : {})[name];
                  if (rparm.length == 0) { rparm.push("1"); };
                  if (props.value !== undefined) { rparm.push(props.value); };
                };
                let vitm = undefined, ufnc = undefined;
                /** 사용자함수 를 우선한다 (원래함수 덮어쓰기 용도) */
                if (!vitm && props && props.validctx) { ufnc = vitm = props.validctx[rdata[0]]; };
                // if (!vitm && vform.current.validctx) { ufnc = vitm = vform.current.validctx[rdata[0]] }
                if (!vitm) { vitm = validations()[rdata[0]]; };
                log.trace("VITM:", rule, rdata[0], vitm ? true: false, value, rparm);
                if (!vitm) { continue; };
                if (rule !== "required" && !ufnc && (value === "" || value === undefined)) {
                  result = true;
                } else {
                  result = vitm({ value, name: label }, rparm, vars.valid);
                };
                log.trace("RESULT:", result, typeof result);
                if (typeof result === "string") {
                  if (!(opt && opt.noerror)) {
                    // vars.valid.error = true;
                    // vars.valid.message = result;
                  };
                  if (opt) { opt.message = result; };
                  result = false;
                };
                if (result === false) {
                  ret = false;
                  break;
                };
              };
              // log.trace("FINAL-RESULT:", props.name, ret)
            } catch (e) {
              log.debug("E:", e);
            };
            // vars.valid.isValidated = true
            // vars.valid.isValid = ret
            resolve(ret);
            return ret;
          });
        };
        putAll(vars, { validateForm });
        expose({ registFormElement, validateForm });
        const v = {
          attrs,
          vars,
        };
        return v;
      },
      async mounted() {
        const self = getCurrentInstance();
        const { vars } = self.setupState;
        vars.elem.value.validateForm = vars.validateForm;
        log.debug("FORM-MOUNTED!!!", this, vars.elem.value, vars.formId);
        vars.elem.value.TEST = function() {
          dialog.alert("OK");
        };
      },
    });
    app.component(name, CForm);
  };
  {
    const name = "c-button";
    const CButton = defineComponent({
      name,
      template: `
      \ <button
      \   v-bind="attrs"
      \   class="btn"
      \   @click="onClick"
      \   >
      \   <slot />
      \ </button>`,
      props: {
      },
      setup: function(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const vars = {
        };
        const onClick = throttle(function(e) { return emit("onclick", e); }, 300);
        const v = {
          attrs,
          vars,
          onClick,
        };
        return v;
      }
    });
    app.component(name, CButton);
  };
  {
    const name = "c-input";
    const CInput = defineComponent({
      name,
      template: `
      \ <input
      \   v-bind="attrs"
      \   class="form-control"
      \   :ref="vars.elem"
      \   :name="props.name"
      \   :type="props.type"
      \   @keydown="onKeydown"
      \   @keyup="onKeyup"
      \   @focus="onFocus"
      \   @blur="onBlur"
      \   />`,
      props: {
        formatter: function() { },
        rtformatter: function() { },
        form: undefined,
        modelValue: "",
        type: "",
        minvalue: undefined,
        maxvalue: undefined,
        minlength: undefined,
        maxlength: undefined,
        name: "",
        label: "",
        required: false,
        vrules: "",
      },
      setup: function(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const vars = {
          itype: props.type,
          avail: true,
          elem: ref(),
        };
        /** 입력컴포넌트 키입력 이벤트 처리 */
        const onKeydown = async function(e) {
          if (vars.avail) {
            await onKeydownProc(e);
          } else {
            cancelEvent(e);
          };
        };
        async function onKeyup(e) {
          if (vars.avail) {
            emit("onkeyup", e);
          } else {
            cancelEvent(e);
          };
        };
        async function onFocus(e) {
          emit("onfocus", e)
        };
        async function onBlur(e) {
          const el = (o = $(vars.elem.value)[0]) ? o : {};
          if (props.formatter) { el.value = props.formatter(el.value); };
          emit("onblur", e);
        };
        async function onKeydownProc(e) {
          vars.avail = false;
          const vprev = vars.elem.value.value;
          const cdnm = e.code;
          const kcode = Number((e && e.keyCode) ? e.keyCode : 0);
          // // log.debug("E:", e.code, e.keyCode)
          // /** 이벤트가 존재하면 */
          if (isEvent(e)) {
            let o;
            /** 1. 선처리, 직접적인 하드웨어 키보드 (scan-code) 입력에 대한 이벤트처리 */
            const el = (o = $(vars.elem.value)[0]) ? o : {};
            let st = Number(el.selectionStart ? el.selectionStart : 0);
            let ed = Number(el.selectionEnd ? el.selectionEnd : 0);
            /** 허용키 : ctrl+c ctrl+v 방향키 bs delete tab enter space */
            if (vars.itype === "number" || vars.itype === "numeric") {
              let v = 0;
              switch (kcode) {
              case KEYCODE_TABLE.PC.Esc:
              case KEYCODE_TABLE.PC.Enter:
              case KEYCODE_TABLE.PC.Delete:
              case KEYCODE_TABLE.PC.Backspace:
              case KEYCODE_TABLE.PC.Insert:
              case KEYCODE_TABLE.PC.Tab:
              case KEYCODE_TABLE.PC.Backslash:
              case KEYCODE_TABLE.PC.ArrowLeft:
              case KEYCODE_TABLE.PC.ArrowRight:
              case KEYCODE_TABLE.PC.Home:
              case KEYCODE_TABLE.PC.End:
              case KEYCODE_TABLE.PC.PageUp:
              case KEYCODE_TABLE.PC.PageDown:
              case KEYCODE_TABLE.PC.MetaLeft: 
              case KEYCODE_TABLE.PC.MetaRight: {
                vars.avail = true;
                // return;
              } break;
              case undefined: { /** NO-OP */ } break;
              case KEYCODE_TABLE.PC.ArrowUp: {
                let d = String(el.value).substring((st - 1) ? (st - 1) : 0, st);
                if (/[0-9]/.test(d)) {
                  d = String(Number(d) - 1);
                  log.trace("CHECK:", d);
                };
                const minv = props.minvalue ? Number(props.minvalue) : undefined;
                const maxv = props.maxvalue ? Number(props.maxvalue) : undefined;
                v = Number(numberOnly(el.value ? el.value : 0)) - 1;
                if (minv !== undefined && v < minv) { v = minv; };
                if (maxv !== undefined && v > maxv) { v = maxv; };
                if (props.rtformatter) {
                  el.value = props.rtformatter(v);
                } else {
                  el.value = v;
                };
                el.selectionStart = st;
                el.selectionEnd = ed;
                cancelEvent(e);
                vars.avail = true;
                emit("update:model-value", el.value);
                emit("onkeydown", e);
                return;
              } break;
              case KEYCODE_TABLE.PC.ArrowDown: {
                let d = String(el.value).substring((st - 1) ? (st - 1) : 0, st);
                if (/[0-9]/.test(d)) {
                  d = String(Number(d) + 1);
                  log.trace("CHECK:", d);
                };
                const minv = props.minvalue ? Number(props.minvalue) : undefined;
                const maxv = props.maxvalue ? Number(props.maxvalue) : undefined;
                v = Number(numberOnly(el.value ? el.value : 0)) + 1;
                if (minv !== undefined && v < minv) { v = minv; };
                if (maxv !== undefined && v > maxv) { v = maxv; };
                if (props.rtformatter) {
                  el.value = props.rtformatter(v);
                } else {
                  el.value = v;
                };
                el.selectionStart = st;
                el.selectionEnd = ed;
                cancelEvent(e);
                vars.avail = true;
                emit("update:model-value", el.value);
                emit("onkeydown", e);
                return;
              } break;
              default: {
                if (
                  (kcode >= KEYCODE_TABLE.PC.Digit0 && kcode <= KEYCODE_TABLE.PC.Digit9) ||
                  (kcode >= KEYCODE_TABLE.PC.Numpad0 && kcode <= KEYCODE_TABLE.PC.Numpad9) ||
                  ( cdnm === "Key1" || cdnm === "Key2" || cdnm === "Key3" ||
                    cdnm === "Key4" || cdnm === "Key5" || cdnm === "Key6" ||
                    cdnm === "Key7" || cdnm === "Key8" || cdnm === "Key9" ||
                    cdnm === "Key0") ||
                  ( cdnm === "Digit1" || cdnm === "Digit2" || cdnm === "Digit3" ||
                    cdnm === "Digit4" || cdnm === "Digit5" || cdnm === "Digit6" ||
                    cdnm === "Digit7" || cdnm === "Digit8" || cdnm === "Digit9" ||
                    cdnm === "Digit0")) {
                  /** NO-OP */
                } else if ((
                  /** Ctrl+C, Ctrl+V, Ctrl-A, Ctrl+R 허용 */
                  ([KEYCODE_TABLE.PC.KeyA, KEYCODE_TABLE.PC.KeyC, KEYCODE_TABLE.PC.KeyV, KEYCODE_TABLE.PC.KeyR, KEYCODE_TABLE.PC.KeyW].indexOf(kcode) !== -1) &&
                  e.ctrlKey)) {
                  /** NO-OP */
                } else {
                  cancelEvent(e)
                }
              } };
            };
            /** 2. 후처리, 키입력이 이루어진 후 DOM 에 반영된 결과물을 2차 가공하는 과정 */
            setTimeout(async function() {
              let value = "";
              if ([KEYCODE_TABLE.PC.Backspace, KEYCODE_TABLE.PC.Delete].indexOf(kcode) !== -1) {
                /** 삭제키인(backspace, delete) 경우 별도처리 */
                let v1, v2, l1, l2;
                let st = Number(el.selectionStart ? el.selectionStart : 0);
                let ed = Number(el.selectionEnd ? el.selectionEnd : 0);
                v1 = (props && props.rtformatter) ? props.rtformatter(vprev) : vprev;
                v2 = (props && props.rtformatter) ? props.rtformatter(el.value) : el.value;
                if (el.value === null || el.value === undefined || el.value === "") {
                  emit("update:model-value", value = el.value = "");
                  emit("onkeydown", e);
                  return vars.avail = true;
                };
                LOOP: while(true) {
                  l1 = v1.length;
                  l2 = v2.length;
                  // // log.debug("LENGTH:", l1, l2, v1, v2, st, ed, el.value)
                  if (l2 === l1) {
                    // // if (st > 1 && kcode === KEYCODE_TABLE.PC.Backspace)
                    if (kcode === KEYCODE_TABLE.PC.Backspace) {
                      v2 = `${v2.substring(0, st - 1)}${v2.substring(st)}`;
                      v2 = (props && props.rtformatter) ? props.rtformatter(v2) : v2;
                      l2 --;
                      st --;
                      ed --;
                    // // } else if (ed < l2 && kcode === KEYCODE_TABLE.PC.Delete) {
                    } else if (kcode === KEYCODE_TABLE.PC.Delete) {
                      v2 = `${v2.substring(0, st)}${v2.substring(st + 2)}`;
                      v2 = (props && props.rtformatter) ? props.rtformatter(v2) : v2;
                      l2 --;
                    };
                  };
                  if (st < 0) { st = 0; };
                  if (ed < 0) { ed = 0; };
                  // // log.debug("CHECK:", l1, l2, st, ed, v2)
                  el.value = v2;
                  await sleep(1);
                  el.selectionStart = st;
                  el.selectionEnd = ed;
                  el.value = value = v2;
                  break LOOP;
                };
              } else {
                /** 일반키인경우 처리 */
                let v = el.value;
                let st = Number(el.selectionStart ? el.selectionStart : 0);
                let ed = Number(el.selectionEnd ? el.selectionEnd : 0);
                let ch = String(v).substring(st - 1, ed);
                // // log.debug("CHAR:", `"${ch}"`, st, ed, v.length, kcode, v)
                if (vars.itype === "number" || vars.itype === "numeric") {
                  v = (props && props.rtformatter) ? props.rtformatter(el.value) : v;
                  if (props.maxlength && v.length > props.maxlength) {
                    v = (props && props.rtformatter) ? props.rtformatter(vprev) : vprev;
                    v = vprev;
                    st--;
                    ed--;
                  };
                  const l1 = String(el.value).length;
                  const l2 = v.length;
                  el.value = v;
                  await sleep(2);
                  /** TODO 기존에 선택상태였는지 체크, 삭제의 경우, 붙여넣기의 경우 */
                  if (l2 > l1) {
                    st ++;
                    ed ++;
                  };
                  el.selectionStart = st;
                  el.selectionEnd = ed;
                  await sleep(5)
                };
                el.value = value = v;
              };
              emit("onkeydown", e);
              emit("update:model-value", `${value}`);
              if (e.keyCode === KEYCODE_TABLE.PC.Enter) { setTimeout(function() { emit("onenter", e); }, 50); };
              vars.avail = true
            }, 50);
          };
        };
        const v = {
          props,
          attrs,
          onBlur,
          onFocus,
          onKeydown,
          onKeyup,
          vars,
        };
        return v;
      },
      async mounted() {
        const self = getCurrentInstance();
        const { vars } = self.setupState;
        registFormElement(self, vars.elem.value);
      }
    });
    app.component(name, CInput);
  };
  {
    const name = "c-check";
    const CCheck = defineComponent({
      name,
      template: `
      \ <input
      \   v-bind="attrs"
      \   class="form-check-input"
      \   :type="vars.type"
      \   :ref="vars.elem"
      \   :name="props.name"
      \   />`,
      props: {
        form: undefined,
        modelValue: "",
        type: "",
        name: "",
        label: "",
        required: false,
        vrules: "",
      },
      setup: function(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const vars = {
          type: props.type === 'radio' ? props.type : 'checkbox',
          avail: true,
          elem: ref(),
        };
        const v = {
          props,
          attrs,
          vars,
        };
        return v;
      },
      async mounted() {
        const self = getCurrentInstance();
        const { vars } = self.setupState;
        registFormElement(self, vars.elem.value);
      }
    });
    app.component(name, CCheck);
  };
};