/**
 * @File        : components.js
 * @Author      : 정재백
 * @Since       : 2024-10-31
 * @Description : 입력컴포넌트
 * @Site        : https://devlog.ntiple.com
 **/

function registerComponent($SCRIPTPRM) {
  const { ref, useAttrs, defineComponent, defineProps, getCurrentInstance, watch, onMounted, onBeforeUnmount, onUpdated } = Vue;
  const cinst = getCurrentInstance;
  const UPDATE_MV = "update:model-value";
  /** 이벤트가 두번 호출되는것을 방지하기 위해 접두사 'on' 붙임 */
  const ONMOUSEDOWN = "onmousedown";
  const ONCLICK = "onclick";
  const ONKEYDOWN = "onkeydown";
  const ONKEYUP = "onkeyup";
  const ONFOCUS = "onfocus";
  const ONBLUR = "onblur";
  const ONENTER = "onenter";
  const ONCHANGE = "onchange";
  const DATA_VALID_INX = "data-valid-inx";
  const body = document.body;
  const {
  BIND_VALUES,
  KEYCODE_REV_TABLE,
  KEYCODE_TABLE,
  MOUNT_HOOK_PROCS,
  UNMOUNT_HOOK_PROCS,
  api,
  asideVisible,
  cancelEvent,
  clear,
  clone,
  copyExclude,
  copyExists,
  crypto,
  dateStrFormat,
  dialog,
  dialogvars,
  doModal,
  empty,
  equals,
  equalsIgnoreCase,
  find,
  formatDate,
  formSubmit,
  formToJson,
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
  secureRandom,
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
  const appbody = $SCRIPTPRM.appbody;
  const { debounce, throttle } = lodash;
  {
    const name = "c-form";
    const CForm = defineComponent({
      template: (`
      \ <form
      \   v-bind="attrs"
      \   \:ref="vars.elem"
      \   \:data-form-ref="vars.formId"
      \   >
      \   <slot />
      \ </form>`),
      props: {
        validctx: {},
      },
      setup(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const formId = genId();
        const vars = {
          elem: ref(),
          formId,
          items: [],
        };
        function registFormElement(compo, elem) {
          log.trace("REGIST-FORM:", compo, elem);
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
          const fprops = props;
          return new Promise(function (resolve, _) {
            let ret = true;
            let result;
            const { attrs, props, vars } = item.compo;
            try {
              const rlist = String(item.rules).split(/\|/g);
              let label;
              let name;
              let value;
              for (const rule of rlist) {
                label =  props.label ? props.label : props.name;
                name = props.name;
                value = props.modelValue;
                const rdata = rule.split(/\:/g);
                const rparm = rdata.length > 1 ? String(rdata[1]).split(/\,/g) : [];
                /** NULL, UNDEFINED 값 통일 */
                value = nval(value, undefined);
                log.trace("RULE:", name, rule, rdata, value, props);
                if (!rdata || rdata.length < 1) { continue; };
                if ((rdata[0] == "atleast" || rdata[0] == "atmost") && /\.[0-9]+$/g.test(name)) {
                  name = name.replace(/\.[0-9]+$/g, "");
                  label = label.replace(/\.[0-9]+$/g, "");
                  // value = (props.model : props.model : {})[name];
                  if (rparm.length == 0) { rparm.push("1"); };
                  if (props.value !== undefined) { rparm.push(props.value); };
                };
                let vitm = undefined, ufnc = undefined;
                /** 사용자함수 를 우선한다 (원래함수 덮어쓰기 용도) */
                if (!vitm && fprops && fprops.validctx) { ufnc = vitm = fprops.validctx[rdata[0]]; };
                // if (!vitm && vform.current.validctx) { ufnc = vitm = vform.current.validctx[rdata[0]] }
                if (!vitm) { vitm = validations()[rdata[0]]; };
                log.trace("VITM:", rule, rdata[0], vitm ? true: false, value, rparm);
                if (!vitm) { continue; };
                if (rule !== "required" && !ufnc && (value === "" || value === undefined)) {
                  result = true;
                } else {
                  let type = item.elem.type;
                  /** FIXME: checkbox, radio, combobox 예외조항 간결하게 할 수 있는 방법 강구 */
                  if (["checkbox", "radio"].indexOf($(item.elem).attr("type")) !== -1 && $(item.elem).attr("value")) {
                    if (typeof value !="object" && value != $(item.elem).attr("value")) { value = ""; };
                  };
                  if ($(item.elem).attr("role") === "combobox") { type = "select"; };
                  result = vitm({ value, name: label, type, t: props.value, f: props.nvalue }, rparm, vars.valid);
                };
                log.trace("RESULT:", name, result, typeof result);
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
              log.trace("FINAL-RESULT:", props.name, ret)
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
        onMounted(async function() {
          vars.elem.value.validateForm = vars.validateForm;
          log.debug("FORM-MOUNTED!!!", this, vars.elem.value, vars.formId);
        });
        onBeforeUnmount(async function() { });
        onUpdated(async function() { });
        return {
          attrs,
          vars,
        };
      },
    });
    app.component(name, CForm);
  };
  {
    const name = "c-button";
    const CButton = defineComponent({
      template: (`
      \ <button
      \   v-bind="attrs"
      \   class="btn"
      \   @click="onClick"
      \   >
      \   <slot />
      \ </button>`),
      props: {
      },
      setup(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const vars = {
        };
        const onClick = throttle(function(e) { return emit(ONCLICK, e); }, 300);
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
      template: (`
      \ <span
      \   \:class="className()"
      \   >
      \   <input
      \     v-bind="attrs"
      \     class=""
      \     \:ref="vars.elem"
      \     \:name="props.name"
      \     \:type="props.type"
      \     \:data-element-uid="vars.uid"
      \     @keydown="onKeydown"
      \     @keyup="onKeyup"
      \     @focus="onFocus"
      \     @blur="onBlur"
      \     />
      \   <span>
      \     <a class="xmark"
      \       role="button"
      \       tabindex="0"
      \       @blur="onBlur"
      \       @focus="onFocus"
      \       @click="onClickButton"
      \       @keydown="onClickButton"
      \       \:ref="vars.buttons[0]"
      \       >
      \       <i class="bi bi-backspace"></i>
      \     </a>
      \     <template v-if="props.type === 'password'">
      \     <a class="vmark"
      \       role="button"
      \       tabindex="0"
      \       @blur="onBlur"
      \       @focus="onFocus"
      \       @click="onClickButton"
      \       @keydown="onClickButton"
      \       \:ref="vars.buttons[1]"
      \       >
      \       <i class="bi bi-eye"></i>
      \     </a>
      \     </template>
      \   </span>
      \ </span>`),
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
      setup(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const uid = genId();
        const vars = {
          uid,
          itype: props.type,
          avail: true,
          elem: ref(),
          buttons: [ref(), ref()]
        };

        const emitChange = debounce(function() {
          emit(ONCHANGE, vars.elem.value.value);
        }, 100);

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
            emit(ONKEYUP, e);
          } else {
            cancelEvent(e);
          };
        };
        async function onFocus(e) {
          const parent = $(vars.elem.value).parent();
          if (parent.prop("tagName") === "SPAN" && parent.hasClass("form-control")) {
            parent.addClass("focused");
          };
          emit(ONFOCUS, e);
        };
        async function onBlur(e) {
          const parent = $(vars.elem.value).parent();
          if (parent.prop("tagName") === "SPAN" && parent.hasClass("form-control")) {
            parent.removeClass("focused");
          };
          const el = (o = $(vars.elem.value)[0]) ? o : {};
          if (props.formatter) { el.value = props.formatter(el.value); };
          emit(ONBLUR, e);
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
            let stv = String(el.value ? el.value : "");
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
              } break;
              case undefined: { /** NO-OP */ } break;
              case KEYCODE_TABLE.PC.ArrowUp: {
                if (!st) { st = 0; };
                LOOP: for (; st <= stv.length; st++) {
                  let dgt = String(stv).substring((st - 1) ? (st - 1) : 0, st);
                  if (!/[0-9]/.test(dgt)) { continue LOOP; };
                  let add = Number("1" + String(stv.substring(st)).replace(/[0-9]/g, "0").replace(/[^0-9]/g, ""));
                  const minv = props.minvalue ? Number(props.minvalue) : undefined;
                  const maxv = props.maxvalue ? Number(props.maxvalue) : undefined;
                  v = Number(numberOnly(stv ? stv : 0)) - add;
                  if (minv !== undefined && v < minv) { v = minv; };
                  if (maxv !== undefined && v > maxv) { v = maxv; };
                  if (props.rtformatter) {
                    el.value = props.rtformatter(v);
                  } else {
                    el.value = v;
                  };
                  if (String(stv).replace(/[^0-9]+/g, "").length > String(v).length) {
                    st -= 1;
                    ed -= 1;
                  };
                  el.selectionStart = st;
                  el.selectionEnd = ed;
                  cancelEvent(e);
                  vars.avail = true;
                  emit(UPDATE_MV, el.value);
                  emit(ONKEYDOWN, e);
                  emitChange();
                  return;
                }
              } break;
              case KEYCODE_TABLE.PC.ArrowDown: {
                if (!st) { st = 0; };
                LOOP: for (; st <= stv.length; st++) {
                  let dgt = String(stv).substring((st - 1) ? (st - 1) : 0, st);
                  if (!/[0-9]/.test(dgt)) { continue LOOP; };
                  let add = Number("1" + String(stv.substring(st)).replace(/[0-9]/g, "0").replace(/[^0-9]/g, ""));
                  const minv = props.minvalue ? Number(props.minvalue) : undefined;
                  const maxv = props.maxvalue ? Number(props.maxvalue) : undefined;
                  v = Number(numberOnly(stv ? stv : 0)) + add;
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
                  emit(UPDATE_MV, el.value);
                  emit(ONKEYDOWN, e);
                  emitChange();
                  return;
                }
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
                  /** Ctrl+C, Ctrl+V, Ctrl-A, Ctrl+R, Ctrl+W 허용 */
                  ([KEYCODE_TABLE.PC.KeyA, KEYCODE_TABLE.PC.KeyC, KEYCODE_TABLE.PC.KeyV, KEYCODE_TABLE.PC.KeyR, KEYCODE_TABLE.PC.KeyW].indexOf(kcode) !== -1) &&
                  (e.ctrlKey || e.metaKey)) || (
                  ([KEYCODE_TABLE.PC.KeyD].indexOf(kcode) !== -1) &&
                  e.altKey)) {
                  /** NO-OP */
                } else {
                  cancelEvent(e);
                }
              } };
            };
            /** 2. 후처리, 키입력이 이루어진 후 DOM 에 반영된 결과물을 2차 가공하는 과정 */
            setTimeout(async function() {
              let v = (el && el.value) ? el.value : "";
              let append = false;
              let value = "";
              /** MACOS - SAFARI 계열 브라우저에서 키입력이 반영되지 않는 현상 FIX */
              if (String(stv).length === String(v).length && /^[a-zA-Z0-9]$/.test(String(e.key).trim())) {
                v = String(v) + String(e.key);
                v = (props && props.rtformatter) ? props.rtformatter(v) : v;
                append = true;
              };
              if ([KEYCODE_TABLE.PC.Backspace, KEYCODE_TABLE.PC.Delete].indexOf(kcode) !== -1) {
                /** 삭제키인(backspace, delete) 경우 별도처리 */
                let v1, v2, l1, l2;
                let st = Number(el.selectionStart ? el.selectionStart : 0);
                let ed = Number(el.selectionEnd ? el.selectionEnd : 0);
                v1 = (props && props.rtformatter) ? props.rtformatter(vprev) : vprev;
                v2 = (props && props.rtformatter) ? props.rtformatter(el.value) : el.value;
                if (el.value === null || el.value === undefined || el.value === "") {
                  emit(UPDATE_MV, value = el.value = "");
                  emit(ONKEYDOWN, e);
                  emitChange();
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
                  if (append) {
                    st++;
                    ed++;
                  };
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
                  if (append) {
                    st++;
                    ed++;
                  };
                  el.selectionStart = st;
                  el.selectionEnd = ed;
                  await sleep(5);
                };
                el.value = value = v;
              };
              emit(ONKEYDOWN, e);
              emit(UPDATE_MV, `${value}`);
              emitChange();
              if (e.keyCode === KEYCODE_TABLE.PC.Enter) { setTimeout(function() { emit(ONENTER, e); }, 50); };
              vars.avail = true
            }, 50);
          };
        };
        async function onClickButton(e) {
          log.trace("E:", e);
          if (!e) { return; };
          switch (e.type) {
          case "click": {
          } break;
          case "keydown": {
            if (!(e.keyCode === 13)) {
              return;
            };
          } break;
          };
          for (let binx = 0; binx < vars.buttons.length; binx++) {
            if (
              (e && (e.target == vars.buttons[binx].value)) ||
              (e && e.target && (e.target.parentElement == vars.buttons[binx].value))
              ) {
              switch(binx) {
              case 0: {
                /** 삭제버튼 */
                $(vars.elem.value).val("");
                emit(UPDATE_MV, ``);
                emitChange();
                setTimeout(function() { $(vars.elem.value).trigger("focus"); }, 1);
              } break;
              case 1: {
                /** 비밀번호 보기버튼 */
                if (vars.elem && vars.elem.value) {
                  const $el = $(vars.elem.value);
                  const $mk = $(vars.buttons[1].value).find("i");
                  log.debug("CHECK-TYPE:", $el.attr("type"), $mk);
                  if ($el.attr("type") === "password") {
                    $el.attr("type", "text");
                    $mk.removeClass("bi-eye").addClass("bi-eye-slash");
                  } else {
                    $el.attr("type", "password");
                    $mk.removeClass("bi-eye-slash").addClass("bi-eye");
                  };
                };
              } break;
              };
            };
          };
        };
        function className() {
          return strm(`form-control ${props && props.class ? props.class : ""}`);
        };
        const self = cinst();
        onMounted(async function() { registFormElement(self, vars.elem.value); });
        onBeforeUnmount(async function() { });
        onUpdated(async function() { });
        return {
          props,
          attrs,
          className,
          onBlur,
          onFocus,
          onKeydown,
          onKeyup,
          onClickButton,
          vars,
        };
      }
    });
    app.component(name, CInput);
  };
  {
    const name = "c-check";
    const CCheck = defineComponent({
      template: (`
      \ <input
      \   v-bind="attrs"
      \   class="form-check-input"
      \   \:type="vars.type"
      \   \:ref="vars.elem"
      \   \:name="vars.name"
      \   \:value="vars.value"
      \   \:checked="vars.checked"
      \   \:data-nvalue="props.nvalue"
      \   \:data-group="vars.group"
      \   \:data-index="vars.index"
      \   @click="onClick"
      \   />`),
      props: {
        form: undefined,
        modelValue: "",
        type: "",
        name: "",
        label: "",
        value: "",
        nvalue: "",
        required: false,
        vrules: "",
      },
      setup(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const vars = {
          type: props.type === "radio" ? props.type : "checkbox",
          name: props.name,
          index: undefined,
          avail: true,
          elem: ref(),
          checked: false,
          value: "",
          group: undefined,
        };
        const PTN_GRP = /^([^.]+)[.]([0-9]+)$/g;
        const emitChange = debounce(function() { emit(ONCHANGE, props.modelValue); }, 300);
        let mat;
        if (props.modelValue instanceof Array) {
          if ((mat = PTN_GRP.exec(props.name))) {
            vars.group = vars.name = mat[1];
            const inx = vars.index = Number(mat[2]);
            if (props.value) {
              if (props.modelValue[inx] === props.value) {
                vars.checked = true;
              } else {
                vars.checked = false;
                props.modelValue[inx] = props.nvalue ? props.nvalue : "";
              };
            } else {
              if (props.modelValue[inx]) {
                vars.checked = true;
              } else {
                vars.checked = false;
                props.modelValue[inx] = "";
              };
            };
            vars.value = props.modelValue[inx];
            emit(UPDATE_MV, clone(props.modelValue));
          };
        } else {
          if ((props.value && props.modelValue === props.value) || (!props.value && props.modelValue)) {
            vars.checked = true;
            vars.value = props.modelValue;
          } else {
            vars.value = "";
          };
        };
        async function onClick(e) {
          let o;
          let value = props.modelValue;
          if (value instanceof Array) {
            if ((o = vars.elem.value) && (o.checked) === true) {
              if (props.value !== undefined) {
                value[vars.index] = props.value;
              } else {
                value[vars.index] = true;
              };
              vars.checked = true;
            } else {
              value[vars.index] = props.nvalue ? props.nvalue : "";
              vars.checked = false;
            };
            vars.value = value[vars.index];
            value = clone(value);
            log.trace("CHECK:", value, vars.index, vars.elem.value);
          } else {
            if ((o = vars.elem.value) && (o.checked) === true) {
              if (props.value !== undefined) {
                value = props.value;
              } else {
                value = true;
              };
              vars.checked = true;
            } else {
              value = "";
              vars.checked = false;
            };
          };
          emit(ONCLICK, e);
          emit(UPDATE_MV, value);
          emitChange();
        };
        const self = cinst();
        onMounted(async function() { registFormElement(self, vars.elem.value); });
        onBeforeUnmount(async function() { });
        onUpdated(async function() { });
        return {
          props,
          attrs,
          vars,
          onClick,
        };
      },
    });
    app.component(name, CCheck);
  };
  {
    const name = "c-select";
    const CSelect = defineComponent({
      template: (`
      \ <div
      \   class="dropdown"
      \   @keyup="onKeypress"
      \   >
      \   <button
      \     type="button"
      \     \:ref="vars.elem"
      \     \:class="getClass('btn dropdown-toggle ', 'button')"
      \     data-bs-toggle="dropdown"
      \     aria-expanded="false"
      \     role="combobox"
      \     >
      \     {{ vars.text }}
      \   </button>
      \   <input type="hidden"
      \     \:name="props.name"
      \     \:value="props.modelValue"
      \     />
      \   <ul class="dropdown-menu">
      \     <li
      \       v-for="(itm, inx) in vars.options"
      \       \:key="inx"
      \       >
      \       <a
      \         data=""
      \         \:class="getClass('dropdown-item cursor-pointer ', 'item', inx)"
      \         @click="onSelect(inx)"
      \         >
      \         {{ itm.name }}
      \       </a>
      \     </li>
      \   </ul>
      \ </div>`),
      props: {
        form: undefined,
        modelValue: "",
        type: "",
        name: "",
        label: "",
        required: false,
        options: [],
        vrules: "",
        variant: "",
      },
      setup(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const vars = {
          avail: true,
          elem: ref(),
          text: "",
          index: 0,
          options: [],
          menuvisb: false,
        };
        function setOptions(options) {
          const ret = [];
          if (!options) { return ret; };
          for (let inx = 0; inx < options.length; inx++) {
            const item = options[inx];
            if (!item) { continue; };
            let value = typeof item === "string" ? item : item.value ? item.value : "";
            let name = item.name ? item.name : value;
            let selected = false;
            if (value === props.modelValue) {
              vars.index = inx;
              selected = true;
            };
            ret.push({ name, value, selected });
          };
          if (vars.index < 0 || vars.index >= ret.length) { vars.index = 0; };
          vars.text = ret[vars.index].name;
          return vars.options = ret;
        };
        function onSelect(select) {
          for (var inx = 0; inx < vars.options.length; inx++) { vars.options[inx].selected = false; };
          const item = vars.options[select];
          item.selected = true;
          vars.text = item.name;
          vars.index = select;
          console.log("OK:", select, vars, vars.elem.value);
          /** FIXME: 임시코드 */
          $(vars.elem.value).text(item.name);
          emit(UPDATE_MV, item.value);
          emitChange();
        };
        function onKeypress(e) {
          log.debug("KEYDOWN:", e);
          switch (Number(e.keyCode)) {
          case KEYCODE_TABLE.PC.ArrowUp: {
            if (vars.index > 0) { vars.index -= 1; };
            if (isEvent(e)) { cancelEvent(e); };
          } break;
          case KEYCODE_TABLE.PC.ArrowDown: {
            if (vars.index < vars.options.length - 1) { vars.index += 1; };
            if (isEvent(e)) { cancelEvent(e); };
          } break ;
          default: {
            log.debug("CHECK-KEY:", e.key, vars.index, vars.options.length);
            let linx = vars.index > 0 ? vars.index : 0;
            LOOP: for (let inx = 0; inx < vars.options.length; inx++) {
              linx = ((linx + 1) % vars.options.length);
              const item = vars.options[linx];
              if (String(item.name).toLowerCase().startsWith(String(e.key).toLowerCase())) {
                vars.index = linx;
                break LOOP;
              };
            };
          } };
          const item = vars.options[vars.index];
          if (item) {
            item.selected = true;
            vars.text = item.name;
            /** FIXME: 임시코드 */
            $(vars.elem.value).text(item.name);
            emit(UPDATE_MV, item.value);
            emitChange();
          };
        };
        function getClass(ocls, type, inx) {
          let ret = ocls;
          switch (type) {
          case "button": {
            if (props.variant) {
              ret = `${ret}\ btn-${props.variant}`;
            };
          } break;
          case "item": {
            if (inx === vars.index) {
              ret = `${ret}\ active`;
            };
          } break;
          };
          return ret;
        };
        const emitChange = debounce(function() { emit(ONCHANGE, props.modelValue); }, 300);
        setOptions(props.options);

        const self = cinst();
        onMounted(async function() { registFormElement(self, vars.elem.value); });
        onBeforeUnmount(async function() { });
        onUpdated(async function() { });
        const v = {
          props,
          attrs,
          vars,
          strm,
          setOptions,
          onSelect,
          onKeypress,
          getClass,
        };
        return v;
      },
    });
    app.component(name, CSelect);
  };
  {
    const name = "c-datepicker";
    const CDatepicker = defineComponent({
      template: (`
      \ <c-input
      \   v-bind="attrs"
      \   class="form-control"
      \   type="text"
      \   \:ref="vars.elem"
      \   @mousedown="onMouseDown"
      \   @click="onClick"
      \   @focus="onFocus"
      \   @blur="onBlur"
      \   />`),
      setup(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const vars = {
          elem: ref(),
          widget: ref(),
        };
        $.extend(true, $.datePicker.defaults, {
          strings: {
            months: ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"],
            days: ["일", "월", "화", "수", "목", "금", "토"]
          },
          view: {
            month: {
              firstDayOfWeek: 0,
            },
          },
          dateFormat(date) {
            return "" + date.getFullYear() + "-" + $.datePicker.defaults.pad(date.getMonth() + 1, 2) + "-" + $.datePicker.defaults.pad(date.getDate(), 2);
          },
          dateParse(string) {
            var date = new Date();
            if (string instanceof Date) {
              date = new Date(string);
            } else {
              var parts = string.match(/(\d{4})-(\d{1,2})-(\d{1,2})/);
              if ( parts && parts.length == 4 ) {
                date = new Date( parts[1], parts[2] - 1, parts[3] );
              }
            };
            return date;
          }
        });
        async function onMouseDown(e) {
          emit(ONMOUSEDOWN, e);
        };
        async function onClick(e) {
          emit(ONCLICK, e);
        };
        const onFocus = debounce(async function(e) {
          vars.widget.value = $.datePicker.api.show({ element: vars.elem.value });
          emit(ONFOCUS, e);
        }, 100);
        async function onBlur(e) {
          if (vars.widget.value) { $.datePicker.api.hide(vars.widget.value); };
          emit(ONBLUR, e);
        };
        const self = cinst();
        onMounted(async function() {
          vars.elem = vars.elem.value._.setupState.vars.elem;
          $(vars.elem.value).attr("data-select", "datepicker");
          log.debug("ELEM:", vars.elem.value);
        });
        onBeforeUnmount(async function() { });
        onUpdated(async function() { });
        return {
          attrs,
          vars,
          onMouseDown,
          onClick,
          onFocus,
          onBlur
        };
      },
    });
    app.component(name, CDatepicker);
  };
  {
    const name = "c-accordion";
    const CAccordion = defineComponent({
      template: (`
      \ <div class="accordion" \:id="vars.uid">
      \   <template v-for="(itm, slotid) in slots">
      \   <div class="accordion-item">
      \     <h3 class="accordion-header">
      \       <button
      \         type="button"
      \         data-bs-toggle="collapse"
      \         \:class="['accordion-button', vars.ivalue === slotid ? '' : 'collapsed']"
      \         \:aria-expanded="vars.ivalue === slotid ? true : false"
      \         \:data-bs-target="'#' + vars.uid + '_' + slotid"
      \         \:aria-controls="'#' + vars.uid + '_' + slotid"
      \         v-html="vars.titles[slotid]"
      \         @click="function(e) { onClick(e, slotid) }"
      \         >
      \       </button>
      \     </h3>
      \     <div
      \       \:id="vars.uid + '_' + slotid"
      \       \:class="['accordion-collapse', 'collapse', vars.ivalue === slotid ? 'show' : '']"
      \       \:data-bs-parent="'#' + vars.uid"
      \       >
      \       <div class="accordion-body">
      \         <slot
      \           \:name="slotid"
      \           v-bind="slotItem(slotid)"
      \           />
      \       </div>
      \     </div>
      \   </div>
      \   </template>
      \ </div>`),
      props: {
        modelValue: undefined,
      },
      setup(props, ctx) {
        // log.debug("CHECK:", props.modelValue);
        const { attrs, emit, expose, slots } = ctx;
        const uid = genId();
        const vars = {
          uid,
          titles: {
          },
          /** 최초값만 지정 */
          ivalue: props.modelValue
        };
        slotItem = function(key) {
          return {
            title: function(title) {
              vars.titles[key] = title;
              return "";
            }
          };
        };
        async function onClick(e, slotid) {
          LOOP: for (const k in slots) {
            if (k === slotid) {
              emit(UPDATE_MV, k);
              emit(ONCHANGE, k);
              break LOOP;
            };
          };
          emit(ONCLICK, e);
        };
        const self = cinst();
        onMounted(async function() {
          self.update();
        });
        onBeforeUnmount(async function() { });
        onUpdated(async function() { });
        return {
          props,
          attrs,
          vars,
          slots,
          slotItem,
          onClick,
        };
      },
    });
    app.component(name, CAccordion);
  };
  {
    const name = "c-pagination";
    const CPagination = defineComponent({
      template: (`
      \ <nav  aria-label="Page navigation">
      \   <ul
      \     class="pagination"
      \     >
      \     <template
      \       v-if="vars.pnums[0] > 1"
      \       >
      \       <li
      \         class="page-item"
      \         >
      \         <a
      \           class="page-link"
      \           \:href="linkHref(1)"
      \           @click="function(e) { onClick(e, 1) }"
      \           >
      \           <i class="bi bi-chevron-double-left"></i>
      \         </a>
      \       </li>
      \     </template>
      \     <template
      \       v-if="vars.current > 1"
      \       >
      \       <li
      \         class="page-item"
      \         >
      \         <a
      \           class="page-link"
      \           \:href="linkHref(vars.current - 1)"
      \           @click="function(e) { onClick(e, vars.current - 1) }"
      \           >
      \           <i class="bi bi-chevron-left"></i>
      \         </a>
      \       </li>
      \     </template>
      \
      \     <template
      \       v-for="(itm, inx) in vars.list"
      \       >
      \       <template v-if="itm.num === vars.current">
      \       <li
      \         class="page-item active"
      \         >
      \         <a
      \           class="page-link"
      \           >
      \           {{ itm.num }}
      \         </a>
      \       </li>
      \       </template>
      \       <template v-else>
      \       <li
      \         class="page-item"
      \         >
      \         <a
      \           class="page-link"
      \           \:href="linkHref(itm.num)"
      \           @click="function(e) { onClick(e, itm.num) }"
      \           >
      \           {{ itm.num }}
      \         </a>
      \       </li>
      \       </template>
      \     </template>
      \
      \     <template
      \       v-if="vars.current < vars.pnums[2]"
      \       >
      \       <li
      \         class="page-item"
      \         >
      \         <a
      \           class="page-link"
      \           \:href="linkHref(vars.current + 1)"
      \           @click="function(e) { onClick(e, vars.current + 1) }"
      \           >
      \           <i class="bi bi-chevron-right"></i>
      \         </a>
      \       </li>
      \     </template>
      \     <template
      \       v-if="vars.pnums[0] + props.pages < vars.pnums[2]"
      \       >
      \       <li
      \         class="page-item"
      \         >
      \         <a
      \           class="page-link"
      \           \:href="linkHref(vars.pnums[2])"
      \           @click="function(e) { onClick(e, vars.pnums[2]) }"
      \           >
      \           <i class="bi bi-chevron-double-right"></i>
      \         </a>
      \       </li>
      \     </template>
      \   </ul>
      \ </nav>`),
      props: {
        rows: 0,
        pages: 0,
        current: 0,
        total: 0,
        href: '',
      },
      setup(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const uid = genId();
        const vars = {
          list: [],
          current: props.current ? props.current : 1,
          pnums: [0, 0, 0],
          rnums: [0, 0],
        };
        const self = cinst();
        update(props.rows, props.pages, props.total, props.current);
        function linkHref(pagenum) {
          let ret = undefined;
          if (props.href) {
            ret = props.href.replace(/[#]page/g, pagenum);
          } else {
            ret = "#";
          };
          return ret;
        };
        function update(rows, pages, total, current) {
          let paging = new Paging(rows, pages, total);
          putAll(vars.pnums, paging.pageNumbers(current));
          putAll(vars.rnums, paging.rowNumbers(current));
          empty(vars.list);
          for (let num = vars.pnums[0]; num <= vars.pnums[1]; num++) { vars.list.push({ num }); };
        };
        watch(function() { return props.current; }, function(nv, ov) { vars.current = nv; });
        async function onClick(e, pagenum) {
          if (!props.href) {
            vars.current = pagenum;
            update(props.rows, props.pages, props.total, pagenum);
            self.update();
          };
          emit("update:current", pagenum);
          emit(ONCLICK, e, pagenum);
        };
        return {
          props,
          attrs,
          vars,
          slots,
          linkHref,
          onClick,
        };
      },
    });
    app.component(name, CPagination);
  };
  {
    const name = "c-aside";
    const CAside = defineComponent({
      template: (`
      \ <aside
      \   \:class="className()"
      \   tabindex="-1"
      \   >
      \   <slot />
      \ </aside>`),
      props: {
        modelValue: undefined,
        position: "",
        visible: false,
      },
      setup(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const uid = genId();
        const vars = {
          clsAside: "",
        };
        function className() {
          let position = "";
          switch(props.position) {
          case "right": {
            position = "offcanvas-end";
          } break;
          default:
          case "left": {
            position = "offcanvas-start";
          } };
          return strm(`offcanvas ${position}\ offcanvas-light ${vars.clsAside}`);
        };
        watch(function() { return props.visible; }, function(nv, ov) {
          if (nv !== ov) {
            if (nv) {
              vars.clsAside = "show";
            } else {
              vars.clsAside = "show hiding";
            };
          };
        });
        const self = cinst();
        onMounted(async function() { });
        onBeforeUnmount(async function() { });
        onUpdated(async function() { });
        return {
          props,
          attrs,
          vars,
          slots,
          className,
        };
      },
    });
    app.component(name, CAside);
  };
};