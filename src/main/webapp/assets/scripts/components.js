/**
 * @File        : c-input.js
 * @Author      : 정재백
 * @Since       : 2024-10-31
 * @Description : 입력컴포넌트
 * @Site        : https://devlog.ntiple.com
 **/

function registerComponent($SCRIPTPRM) {
  const ref = Vue.ref;
  const useAttrs = Vue.useAttrs;
  const defineProps = Vue.defineProps;
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
  const app = $SCRIPTPRM.app;
  const { debounce, throttle } = lodash;
  {
    const name = "c-button";
    const CButton = {
      template: `
      \ <button
      \   class="btn"
      \   @click="onClick"
      \   >
      \   <slot />
      \ </button>`,
      name,
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
    };
    app.component(name, CButton);
  };
  {
    const name = "c-input";
    const CInput = {
      template: `
      \ <input
      \   class="form-control"
      \   :ref="vars.elem"
      \   :vrules=""
      \   :formatter=""
      \   :rtformatter=""
      \   @keydown="onKeydown"
      \   @keyup="onKeyup"
      \   @focus="onFocus"
      \   @blur="onBlur"
      \   v-bind="attrs"
      \   />`,
      name,
      setup: function(props, ctx) {
        const { attrs, emit, expose, slots } = ctx;
        const vars = {
          itype: attrs.type,
          avail: true,
          material: false,
          elem: ref(),
          valid: {
            error: false,
            isValidated: false,
            isValid: undefined,
            message: undefined,
          },
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
          if (attrs.formatter) { el.value = attrs.formatter(el.value); };
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
            let st = Number(el.selectionStart || 0);
            let ed = Number(el.selectionEnd || 0);
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
                const minv = attrs.minvalue ? Number(attrs.minvalue) : undefined;
                const maxv = attrs.maxvalue ? Number(attrs.maxvalue) : undefined;
                v = Number(numberOnly(el.value ? el.value : 0)) - 1;
                if (minv !== undefined && v < minv) { v = minv; };
                if (maxv !== undefined && v > maxv) { v = maxv; };
                if (attrs.rtformatter) {
                  el.value = attrs.rtformatter(v);
                } else {
                  el.value = v;
                };
                el.selectionStart = st;
                el.selectionEnd = ed;
                cancelEvent(e);
                vars.avail = true;
                return;
              } break;
              case KEYCODE_TABLE.PC.ArrowDown: {
                let d = String(el.value).substring((st - 1) ? (st - 1) : 0, st);
                if (/[0-9]/.test(d)) {
                  d = String(Number(d) + 1);
                  log.trace("CHECK:", d);
                };
                const minv = attrs.minvalue ? Number(attrs.minvalue) : undefined;
                const maxv = attrs.maxvalue ? Number(attrs.maxvalue) : undefined;
                v = Number(numberOnly(el.value ? el.value : 0)) + 1;
                if (minv !== undefined && v < minv) { v = minv; };
                if (maxv !== undefined && v > maxv) { v = maxv; };
                if (attrs.rtformatter) {
                  el.value = attrs.rtformatter(v);
                } else {
                  el.value = v;
                };
                el.selectionStart = st;
                el.selectionEnd = ed;
                cancelEvent(e);
                vars.avail = true;
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
                  ([KEYCODE_TABLE.PC.KeyA, KEYCODE_TABLE.PC.KeyC, KEYCODE_TABLE.PC.KeyV, KEYCODE_TABLE.PC.KeyR].indexOf(kcode) !== -1) &&
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
                v1 = (attrs && attrs.rtformatter) ? attrs.rtformatter(vprev) : vprev;
                v2 = (attrs && attrs.rtformatter) ? attrs.rtformatter(el.value) : el.value;
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
                      v2 = (attrs && attrs.rtformatter) ? attrs.rtformatter(v2) : v2;
                      l2 --;
                      st --;
                      ed --;
                    // // } else if (ed < l2 && kcode === KEYCODE_TABLE.PC.Delete) {
                    } else if (kcode === KEYCODE_TABLE.PC.Delete) {
                      v2 = `${v2.substring(0, st)}${v2.substring(st + 2)}`;
                      v2 = (attrs && attrs.rtformatter) ? attrs.rtformatter(v2) : v2;
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
                  v = (attrs && attrs.rtformatter) ? attrs.rtformatter(el.value) : v;
                  if (attrs.maxlength && v.length > attrs.maxlength) {
                    v = (attrs && attrs.rtformatter) ? attrs.rtformatter(vprev) : vprev;
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
          attrs,
          onBlur,
          onFocus,
          onKeydown,
          onKeyup,
          vars,
        };
        return v;
      }
    };
    app.component(name, CInput);
  };
};