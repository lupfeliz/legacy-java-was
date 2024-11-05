/**
 * @File        : entry.js
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 진입점 JS 스크립트
 * @Site        : https://devlog.ntiple.com
 **/

function initEntryScript(callback, { vars, log, cbase }) {
  const lodash = _;
  const U = undefined;
  const N = null;
  const ref = Vue.ref;
  const watch = Vue.watch;
  const nextTick = Vue.nextTick;
  const M_SHOWN = "shown.bs.modal";
  const M_HIDDEN = "hidden.bs.modal";
  const DATE_FORMAT_YM = "YYYY-MM";
  const DATE_FORMAT_YMD = "YYYY-MM-DD";
  const DATE_FORMAT_NORM = "YYYY-MM-DD HH:mm:ss";
  const DATE_FORMAT_CODE = "YYYYMMDDHHmmss";
  const PTN_CRYPTOKEY_HDR = /([-]{5}(BEGIN|END) (RSA ){0,1}(PRIVATE|PUBLIC) KEY[-]{5})/gm;
  const PTN_NL = /[\r\n]/gm;

  /** 필요한 라이브러리들을 추가한다. */
  const LOG = {
    trace: function() { },
    debug: window.console.log,
    info: window.console.log,
    warn: window.console.warn,
    error: window.console.warn
  };
  for (const k in LOG) { log[k] = LOG[k]; };

  /** 공통프로퍼티 */
  const appvars = {
    astate: 0,
    gstate: 0,
    tstate: {},
    uidseq: 0,
    router: {},
    config: {
      app: { profile: "", basePath: "" },
      api: [{ base: "", alter: "", server: "", timeout: 0 }],
      auth: { expiry: 0 },
      security: {
        key: { rsa: { public: "", private: "" } }
      }
    },
    global: { },
    MaterialStyle: {},
    instance: { },
  };

  /** 레이어팝업 제어 */
  const dialogvars = ref({
    modal: {
      element: {},
      instance: {},
      current: {},
      queue: [],
      click: function(cmd) {
        const modal = dialogvars.value.modal;
        switch (modal.current.type) {
        case "alert": {
          if (modal.current.resolve) { modal.current.resolve(true); };
        } break;
        case "confirm": {
          if (modal.current.resolve) { modal.current.resolve(cmd === 1 ? true : false); };
        } break; };
        modal.current = { };
        modal.instance.hide();
      }
    },
    progress: {
      element: {},
      instance: {},
      current: {},
      queue: [],
      handlevis: function(e) {
        const current = dialogvars.value.progress.current;
        if (current && current.resolve) {
          current.resolve();
          current.state = e.type;
          dialogvars.value.progress.current = {};
        };
        doProgress();
      }
    },
    winpopups: {
      closeListener: function() { },
      list: { }
    }
  });

  const _KEYCODE_TABLE = {
    /**
     * 특수문자 키보드   [PC, ANDROID, IOS, CHAR]
     * Android 에서 CODE 는 물리키보드인 경우에서만 표시됨
     * 대다수의 경우 Android 에서는 229 (가상키눌림) 리턴
     * IOS 는 물리키보드와 동일
     * 키코드 참고 : https://www.toptal.com/developers/keycode/table
     **/
    Semicolon           : [ 59, 229, 186,  ";"],
    Equal               : [ 61, 229, 186,  "="],
    Comma               : [188, 229, 188,  ","],
    Minus               : [189, 229, 189,  "-"],
    Period              : [190, 229, 190,  "."],
    Slash               : [191, 229, 191,  "/"],
    Backquote           : [192, 229, 192,  "`"],
    BracketLeft         : [219, 229, 219,  "["],
    Backslash           : [220, 229, 220, "\\"],
    BracketRight        : [221, 229, 221,  "]"],
    Quote               : [222, 229, 222, "\'"],
    /** 동작키보드 (화살표, 엔터키 등) */
    Esc                 : [ 27,  27,  27,    U],
    Enter               : [ 13,  13,  13,    U],
    /** IOS 에서는 "Delete" 가 아닌 "Undefined" */
    Delete              : [ 46,  46,  46,    U],
    /** IOS 에서는 "Insert" 이벤트 자체가 없음 */
    Insert              : [ 45,  45,   U,    U],
    Tab                 : [  9,   9,   9,    U],
    Backspace           : [  8,   8,   8,    U],
    Space               : [ 32, 229,  32,  " "],
    ArrowLeft           : [ 37,  37,  37,    U],
    ArrowRight          : [ 39,  39,  39,    U],
    ArrowUp             : [ 38,  38,  38,    U],
    ArrowDown           : [ 40,  40,  40,    U],
    Home                : [ 36,  36,  36,    U],
    End                 : [ 35,  35,  35,    U],
    PageUp              : [ 33,  33,  33,    U],
    PageDown            : [ 34,  34,  34,    U],
    /** 메타키보드 */
    ShiftLeft           : [ 16,  16,  16,    U],
    ShiftRight          : [ 16,  16,  16,    U],
    ControlLeft         : [ 17,  17,  17,    U],
    ControlRight        : [ 17,  17,  17,    U],
    AltLeft             : [ 18,  18,  18,    U],
    AltRight            : [ 18,  18,  18,    U],
    MetaLeft            : [ 91,  91,  91,    U],
    MetaRight           : [ 91,  91,  91,    U],
    /** 숫자키 */
    Digit0              : [ 48, 229,  48,  "0"],
    Digit1              : [ 49, 229,  49,  "1"],
    Digit2              : [ 50, 229,  50,  "2"],
    Digit3              : [ 51, 229,  51,  "3"],
    Digit4              : [ 52, 229,  52,  "4"],
    Digit5              : [ 53, 229,  53,  "5"],
    Digit6              : [ 54, 229,  54,  "6"],
    Digit7              : [ 55, 229,  55,  "7"],
    Digit8              : [ 56, 229,  56,  "8"],
    Digit9              : [ 57, 229,  57,  "9"],
    /** 키패드 */
    Numpad0             : [ 96, 229,  96,  "0"],
    Numpad1             : [ 97, 229,  97,  "1"],
    Numpad2             : [ 98, 229,  98,  "2"],
    Numpad3             : [ 99, 229,  99,  "3"],
    Numpad4             : [100, 229, 100,  "4"],
    Numpad5             : [101, 229, 101,  "5"],
    Numpad6             : [102, 229, 102,  "6"],
    Numpad7             : [103, 229, 103,  "7"],
    Numpad8             : [104, 229, 104,  "8"],
    Numpad9             : [105, 229, 105,  "9"],
    KeyA                : [ 65, 229,  65,  "a"],
    KeyB                : [ 66, 229,  66,  "b"],
    KeyC                : [ 67, 229,  67,  "c"],
    KeyD                : [ 68, 229,  68,  "d"],
    KeyE                : [ 69, 229,  69,  "e"],
    KeyF                : [ 70, 229,  70,  "f"],
    KeyG                : [ 71, 229,  71,  "g"],
    KeyH                : [ 72, 229,  72,  "h"],
    KeyI                : [ 73, 229,  73,  "i"],
    KeyJ                : [ 74, 229,  74,  "j"],
    KeyK                : [ 75, 229,  75,  "k"],
    KeyL                : [ 76, 229,  76,  "l"],
    KeyM                : [ 77, 229,  77,  "m"],
    KeyN                : [ 78, 229,  78,  "n"],
    KeyO                : [ 79, 229,  79,  "o"],
    KeyP                : [ 80, 229,  80,  "p"],
    KeyQ                : [ 81, 229,  81,  "q"],
    KeyR                : [ 82, 229,  82,  "r"],
    KeyS                : [ 83, 229,  83,  "s"],
    KeyT                : [ 84, 229,  84,  "t"],
    KeyU                : [ 85, 229,  85,  "u"],
    KeyV                : [ 86, 229,  86,  "v"],
    KeyW                : [ 87, 229,  87,  "w"],
    KeyX                : [ 88, 229,  88,  "x"],
    KeyY                : [ 89, 229,  89,  "y"],
    KeyZ                : [ 90, 229,  90,  "z"],
    /** Android 전용 가상키눌림 */
    Virtual             : [229, 229, 229,    U],
  };

  const KEYCODE_TABLE = { PC: {}, ANDROID: {}, IOS: {}, CHAR: {} };

  /** 키코드 -> 코드명 역색인 */
  const KEYCODE_REV_TABLE = { PC: {}, ANDROID: {}, IOS: {}, CHAR: {}, };

  for (const k in _KEYCODE_TABLE) {
    put(KEYCODE_TABLE.PC, k, (_KEYCODE_TABLE)[k][0]);
    put(KEYCODE_TABLE.ANDROID, k, (_KEYCODE_TABLE)[k][1]);
    put(KEYCODE_TABLE.IOS, k, (_KEYCODE_TABLE)[k][2]);
    put(KEYCODE_TABLE.CHAR, k, (_KEYCODE_TABLE)[k][3]);
  };

  LOOP1: for (const k in KEYCODE_TABLE) {
    for (const k2 in (KEYCODE_TABLE)[k]) {
      const v = (KEYCODE_TABLE)[k][k2];
      if (k == "ANDROID" && v == 229) { continue LOOP1; };
      if (v !== U) { put((KEYCODE_REV_TABLE)[k], v, k2); };
    };
  };

  /** 앱 내 유일키 생성 */
  function genId() { return (new Date().getTime()) + String((appvars.uidseq = (appvars.uidseq + 1) % 1000) + 1000).substring(1, 4) };

  /** [출처] [javascript] securerandom|작성자 alucard99 */
  function secureRandom(wordCount) {
    var randomWords;
    if (window.crypto && window.crypto.getRandomValues) {
      /** 크롬 등에서 지원 */
      randomWords = new Int32Array(wordCount);
      window.crypto.getRandomValues(randomWords);
    } else if (window.msCrypto && window.msCrypto.getRandomValues) {
      /** Internet Explorer 11에서 지원 */
      randomWords = new Int32Array(wordCount);
      window.msCrypto.getRandomValues(randomWords);
    } else {
      return Math.random(); 
    };
    var result = randomWords[0] * Math.pow(2, -32);
    result = Math.abs(result);
    return result;
  };

  /** 최소(min)~최대(max)값 사이의 난수 생성, 최소값을 입력하지 않을경우 자동으로 0 으로 지정됨 */
  function getRandom(max, min) {
    if (max === U) { max = 0; };
    if (min === U) { min = 0; };
    if (max < 0) { max = max * -1; };
    const ret = min + Math.floor(Math.random() * max);
    return ret;
  };

  /** 단일문자 난수 */
  function randomChar(c, n) {
    if (c === U) { c = "a"; };
    if (n === U) { n = 26; };
    return String.fromCharCode(Number(c.charCodeAt(0)) + getRandom(n));
  };

  /** 난수로 이루어진 문자열, number / alpha / alphanum 의 3가지 타입으로 생성가능 */
  function randomStr(length, type) {
    let ret = "";
    switch (type) {
    case U:
    /** 숫자   */
    case "number": {
      for (let inx = 0; inx < length; inx++) { ret += String(getRandom(10)); }
    } break;
    /** 문자   */
    case "alpha": {
      for (let inx = 0; inx < length; inx++) {
        switch(getRandom(2)) {
        case 0: /** 소문자 */ { ret += randomChar("a", 26); } break;
        case 1: /** 대문자 */ { ret += randomChar("A", 26); } break;
        }
      }
    } break;
    /** 영숫자 */
    case "alphanum": {
      for (let inx = 0; inx < length; inx++) {
        switch(getRandom(3)) {
        case 0: /** 숫자   */ { ret += String(getRandom(10)); } break;
        case 1: /** 소문자 */ { ret += randomChar("a", 26); } break;
        case 2: /** 대문자 */ { ret += randomChar("A", 26); } break;
        }
      }
    } break; };
    return ret;
  };

  function parseDate(v, f) {
    let ret = undefined;
    if (v instanceof Date) {
      return v;
    } else if (typeof(v) == "string") {
      /** TODO: 기호는 무작정 치환하지 않고 자릿수 커팅을 먼저 시도하도록 */
      v = v.replace(/[^0-9]+/g, "");
      if (!f) { f = DATE_FORMAT_CODE; };
    } else if (typeof(v) == "number") {
      v = new Date(v);
    };
    if (f === undefined) {
      ret = moment(v).toDate();
    } else {
      ret = moment(v, f ? f: DATE_FORMAT_CODE).toDate();
    };
    return ret;
  };

  function dateStrFormat(v, of, nf) {
    if (!v) { return v; };
    return moment(parseDate(v), nf ? nf : DATE_FORMAT_CODE).format(of);
  };

  function formatDate(v, f) {
    const date = parseDate(v);
    if (date) { return moment(v).format(f ? f : DATE_FORMAT_NORM) };
    return ""
  };

  function makeDate(y, m, d, h, i, s) {
    let ret = new Date(0);
    let t;
    for (let inx = 0; inx < 2; inx ++) {
      if ((t = Number(y)) >= 1) { ret.setFullYear(t); };
      if ((t = Number(m)) >= 1) { ret.setMonth(t - 1); };
      if ((t = Number(d)) >= 1) { ret.setDate(t); };
      if ((t = Number(h)) >= 0) { ret.setHours(t); };
      if ((t = Number(i)) >= 0) { ret.setMinutes(t); };
      if ((t = Number(s)) >= 0) { ret.setSeconds(t); };
    };
    // log.trace(`FORMAT-DATE:${y || ""}-${m || ""}-${d || ""} / ${h || ""}:${i || ""}:${s || ""}`, ret);
    return ret;
  };

  function putAll(_target, source, opt) {
    let target = _target;
    if (target == null || source == null || target === source ) { return target; };
    if (!opt) { opt = { root: target }; };
    for (const k in source) {
      const titem = target[k];
      const sitem = source[k];
      if (titem !== U && titem !== null) {
        if (typeof (titem) === "string") {
          target[k] = source[k]
        } else if (((opt ? opt : {}).deep) && titem instanceof Array && sitem instanceof Array) {
          putAll(titem, sitem, opt)
        } else if (((opt ? opt : {}).deep) && typeof(titem) === "object" && typeof(sitem) === "object") {
          putAll(titem, sitem, opt)
        } else {
          /** 타입이 다르다면 무조건 치환. */
          target[k] = source[k];
        }
      } else {
        target[k] = source[k];
      }
    };
    return target;
  };
  function getParameter(key) {
    let ret = U;
    const prm = { };
    let o;
    try {
      const d1 = String(((o = history) && (o = o.state) && (o = o.url)) ? o : "").split(/[/]/);
      const d2 = String(((o = history) && (o = o.state) && (o = o.as)) ? o : "").split(/[/]/);
      // log.debug("D:", d1, d2);
      let len = d1.length > d2.length ? d1.length : d2.length;
      for (let inx = 0; inx < len; inx++) {
        if (/[\[]([a-zA-Z0-9_-]+)[\]]/.test(d1[inx] ? d1[inx] : "")) {
          prm[d1[inx].substring(1, d1[inx].length - 1)] = d2[inx];
        };
      };
    } catch (e) {
      log.debug("E:", e);
    };
    if ((o = history) && (o = o.state) && (o = o.options)) {
      for (const k of Object.keys(o)) { prm[k] = o[k]; };
    };
    if ((o = location.search) && (o = new URLSearchParams(o))) {
      for (const k of o.keys()) { prm[k] = o.get(k); };
    };
    if (Object.keys(prm).length > 0) { log.debug("PRM:", prm, history); };
    ret = key ? prm[key] : prm;
    return ret;
  };
  function getUrl() { return location.href; };
  function getUri() {
    let ret = "/";
    let o;
    if (appvars.astate) {
      ret = String(((o = history) && (o = o.state) && (o = o.url)) ? o :  "/").replace(/[?].*$/g, "");
    };
    return ret;
  };
  function setGlobalTmp(value) {
    const tid = randomStr(10, "alphanum");
    window[tid] = function() { return value; };
    return tid;
  };
  function getGlobalTmp(tid) {
    let ret = U;
    const win = window;
    if (win[tid]) {
      ret = win[tid];
      if (ret) { ret = ret(); };
      // if (ret) { ret = ret.value; };
      delete win[tid];
    };
    return ret;
  };
  function setOpenerTmp(value) {
    const tid = randomStr(10, "alphanum");
    const win = window;
    if (win && win.opener) {
      win.opener[tid] = function() { return value; };
    };
    return tid;
  };
  function getOpenerTmp(tid) {
    let ret = U;
    const win = window;
    // log.debug("CHECK:", tid, win.opener, win.opener[tid]);
    if (win.opener && win.opener[tid]) {
      ret = win.opener[tid];
      if (ret) { ret = ret(); };
      // if (ret) { ret = ret.value; };
      if (ret.$$POPUPCTX$$) {
        ret.$$POPUPCTX$$.close = function() {
          window.close();
        };
        delete ret.$$POPUPCTX$$;
      };
      delete win.opener[tid];
    } else {
      /** 오픈주체가 없으므로 창을 닫는다 */
      window.close();
    };
    return ret;
  };

  const dialog = {
    alert: function(msg) { return new Promise(function(resolve) {
      dialogvars.value.modal.queue.push({
        type: "alert",
        msg: msg,
        resolve
      });
    }); },
    confirm: function(msg) { return new Promise(function(resolve) {
      dialogvars.value.modal.queue.push({
        type: "confirm",
        msg: msg,
        resolve
      });
    }); },
    progress: function(vis, timeout) { return new Promise(function(resolve) {
      if (vis === U) { vis = true };
      dialogvars.value.progress.queue.push({
        vis: vis,
        timeout: timeout,
        resolve
      });
    }); },
    winpopup: function(url, data, option) {
      const winpopups = dialogvars.value.winpopups;
      /** 이전에 열린 팝업들을 제거한다 */
      for (let tid in winpopups.list) {
        const pctx = winpopups.list[tid];
        if (pctx.close) {
          pctx.close();
          delete winpopups.list[tid];
        };
      };
      if (!data) { data = { }; };
      data.OPENER_LOG = log;
      const tid = setGlobalTmp(data);
      const wm = Number(window.screen.availWidth);
      const hm = Number(window.screen.availHeight);
      if (!option) { option = {}; };
      let target = option.target ? option.target : "_blank";
      let width = Math.round(option.width ? option.width : wm / 3);
      let height = Math.round(option.height ? option.height : hm / 3);
      /** 화면중앙 */
      let left = Math.round(option.left ? option.left : (wm - width) / 2);
      /** 상단 1/4 지점 */
      let top = Math.round(option.top ? option.top : (hm - height) / 4);
      let menubar = option.menubar ? option.menubar : "no";
      let scrollbars = option.scrollbars ? option.scrollbars : "yes";
      let status = option.status ? option.status : "no";
      let location = option.location ? option.location : "no";
      let resizable = option.resizable ? option.resizable : "yes";
      if (data) {
        winpopups.list[tid] = { };
        data.$$POPUPCTX$$ = winpopups.list[tid];
      };
      let addr = url;
      if (/[?]/.test(addr)) { addr = addr + "&tid=" + tid; } else { addr = addr + "?tid=" + tid; };
      if (/^about:/.test(url)) {
        addr = url;
      } else if (/^http/.test(url)) {
        addr = url;
      } else if (/\//.test(url)) {
        addr = cbase + addr;
      };
      const popopts = "popup=true,width=" + width + ",height=" + height + ",left=" + left + ",top=" + top + ",menubar=" + menubar + "," +
        "scrollbars=" + scrollbars + ",status=" + status + ",location=" + location + ",resizable=" + resizable + "";
      log.debug("POPUP-OPTS:", addr, target, popopts);
      const hnd = window.open(addr, target, popopts);
      // hnd.moveTo(left, top);
      // log.debug("HND:", hnd);
      if (!winpopups.closeListener) {
        /** window가 리프레시되기 전에 열려있는 모든 창을 닫는다 */
        winpopups.closeListener = async function() {
          log.trace("CLOSE....");
          for (let tid in winpopups.list) {
            const pctx = winpopups.list[tid];
            if (pctx.close) {
              pctx.close();
              delete winpopups.list[tid];
            }
          }
        };
        window.addEventListener("beforeunload", winpopups.closeListener);
      };
      return hnd;
    }
  };

  function doModal() {
    nextTick(function() {
      const modal = dialogvars.value.modal;
      if (!modal.instance.show) { return; };
      if (modal.queue.length > 0) {
        const item = modal.queue.splice(0, 1)[0];
        modal.current = item;
        modal.instance.show();
      };
    });
  };

  function doProgress() {
    nextTick(function() {
      const progress = dialogvars.value.progress;
      if (!progress.instance.show) { return; };
      if (progress.current.resolve) { return; };
      if (progress.queue.length > 0) {
        const item = progress.queue.splice(0, 1)[0];
        if (
          (progress.current.state == M_SHOWN && item.vis) ||
          (progress.current.state == M_HIDDEN && !item.vis)) {
          nextTick(doProgress);
          return;
        };
        progress.current = item;
        if (item.vis) {
          if (!isNaN(Number(item.timeout))) {
            setTimeout(function() {
              dialog.progress(false);
            }, Number(item.timeout));
          };
          progress.instance.show();
        } else {
          progress.instance.hide();
        };
      };
    })
  };

  function initpopup() {
    if (opener) { log.debug = opener.console.log; };
    if (window.opener) {
      // const v = reactive(getOpenerTmp(getParameter("tid")));
      const v = getOpenerTmp(getParameter("tid"));
      const l = v.OPENER_LOG;
      delete v.OPENER_LOG;
      putAll(log, l);
      putAll(vars, v);
      return v;
    };
    return {};
  };

  function numberOnly(str, c) {
    let ret = str;
    let s = 1;
    if (!str) { str = ""; };
    /** 부호체크 */
    if (c) { if (str[0] == "-") { s = -1; }; };
    ret = String(str).replace(/[^0-9]+/g, "");
    return String(Number(ret) * s);
  };

  function numToHangul(str) {
    let minus = /^[-]/.test(str);
    str = numberOnly(str);
    if (!str) { str = ""; };
    let ret = "";
    str = str.replace(/^[0]+/g, "");
    if (!str) { str = "0"; };
    let len = str.length;
    let digit = "";
    let word = "";
    let han = ["", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구"];
    let pos1 = ["", "십", "백", "천"];
    let pos2 = ["", "만", "억", "조", "경"];
    function divide(str) {
      let ret = "";
      let len = str.length;
      for (let inx = 0; inx < len; inx++) {
        word = "";
        digit = str.substring(len - inx - 1, len - inx);
        if (digit != "0") {
          if (digit == "1" && inx % 4 != 0) {
            word = (pos1[inx % 4]);
          } else {
            word = han[Number(digit)] + (pos1[inx % 4]);
          }
        };
        ret = word + ret;
      };
      return ret;
    };
    for (let inx = 0; inx < len; inx += 4) {
      let frag = str.substring(len - inx - 4, len - inx);
      word = divide(frag);
      if (inx % 4 == 0) {
        if (word.length > 0) { word = word + pos2[Math.floor(inx / 4)] + " " };
      };
      ret = "" + word + ret;
    };
    return ret;
  };

  function clone(v) {
    let ret = U;
    if (!v) { return ret; };
    try {
      ret = JSON.parse(JSON.stringify(v));
    } catch(e) { };
    return ret;
  };

  function nitem(a, i, v) {
    if (a && (i !== U && i!== null) && i >= 0 && a[i]) {
      if (v) { a[i] = v; };
      return a[i];
    };
    return v;
  };

  function val(o, k, v) {
    let r = o;
    if (o && (k !== U && k !== null) && o[k]) {
      if (v) { o[k] = v; };
      r = o[k];
    };
    return r;
  };

  function nval(o, def) {
    if (!def) { def = U; };
    switch (o) {
    case null:
    case U: o = def; break;
    default: break;
    };
    return o;
  };

  function trim(value) {
    if (!value) { return value; };
    for (const k in value) {
      const v = value[k];
      if (!v) { continue; };
      if (typeof v === "string") {
        value[k] = String(v).trim();
      } else {
        value[k] = trim(v);
      };
    };
    return value;
  };

  function num(v, d) {
    let ret = d;
    if (v && !isNaN(v = Number(v))) { ret = v; };
    return ret;
  };

  function lpad(v, len, pad) {
    if (pad.length > len) {
      // log.debug("오류 : 채우고자 하는 문자열이 요청 길이보다 큽니다")
      return v;
    };
    v = String(v);
    pad = String(pad);
    while (v.length < len) { v = pad + v; };
    v = v.length >= len ? v.substring(0, len) : v;
    return v;
  };

  function rpad(v, len, pad) {
    if (pad.length > len) {
      // console.log("오류 : 채우고자 하는 문자열이 요청 길이보다 큽니다")
      return v + "";
    };
    v = String(v);
    pad = String(pad);
    while (v.length < len) { v += pad; };
    v = v.length >= len ? v.substring(0, len) : v;
    return v;
  };

  /** 다수개 배열을 새로운 배열로 합쳐 반환 */
  function mergeAll(...params) {
    let ret = U;
    for (const item of params) {
      if (item instanceof Array) {
        if (ret === null || ret === U) { ret = [ ]; };
        pushAll(ret, item);
      } else {
        if (ret === null || ret === U) { ret = { }; };
        putAll(ret, item);
      }
    };
    return ret;
  };

  /** 두개 객체을 합쳐 반환 */
  function mergeObj(v1, v2) {
    let ret = {};
    if (v1) { putAll(ret, v1); };
    if (v2) { putAll(ret, v2); };
    return ret;
  };

  function equals(target, source) {
    let ret = false;
    if (target instanceof Array) {
      if (!(source instanceof Array)) { return false; };
      if (target.length != source.length) { return false; };
      ret = true;
      for (let inx = 0; inx < target.length; inx++) {
        if (target[inx] !== source[inx]) { return false; };
      };
    } else if (typeof target === "object") {
      if (Object.keys(target).length != Object.keys(source).length) { return false; };
      ret = true;
      for (const k in target) {
        if (target[k] !== source[k]) { return false };
      };
    } else {
      ret = target == source;
    };
    return ret
  };

  function equalsIgnoreCase(v1, v2) {
    let ret = false;
    if (typeof v1 != "string") { return ret; };
    if (typeof v2 != "string") { return ret; };
    if (!v1 && !v2) { return true; };
    if (!v1 || !v2) { return ret; };
    v1 = v1.toLowerCase();
    v2 = v2.toLowerCase();
    ret = v1 == v2;
    return ret;
  };

  function min(array) {
    let ret = U;
    try {
      for (const v of array) {
        if (ret === null || ret === U) { ret = v; continue; };
        if (v < ret) { ret = v; };
      };
    } catch (ignore) { };
    return ret;
  };

  function max(array) {
    let ret = U;
    try {
      for (const v of array) {
        if (ret === null || ret === U) { ret = v; continue; };
        if (v > ret) { ret = v; };
      }
    } catch (ignore) { };
    return ret;
  };

  function near(search, list, minv, maxv) {
    let ret = U;
    let dif = Number.MAX_VALUE;
    if (!list || !list.length) { return ret; };
    const v1 = Number(search);
    if (isNaN(v1)) { return ret; };
    for (let inx = 0; inx < list.length; inx++) {
      const v2 = Number(list[inx]);
      if (isNaN(v2)) { continue; };
      if (minv !== U && !isNaN(minv) && v2 < minv) { continue; };
      if (maxv !== U && !isNaN(maxv) && v2 > maxv) { continue; };
      const cif = Math.abs(v1 - v2);
      if (cif < dif) {
        dif = cif;
        ret = list[inx];
      };
    };
    return ret;
  };

  function find(search, list) {
    let ret = -1;
    if (!list || !list.length) { return ret; };
    FIND_LOOP: for (let inx = 0; inx < list.length; inx++) {
      let item = list[inx];
      if (search instanceof Function) {
        try {
          if (search(item)) {
            ret = inx;
            break FIND_LOOP;
          }
        } catch (e) { };
      } else if (search instanceof Array) {
        for (let match of search) {
          if (item === match) {
            ret = inx;
            break FIND_LOOP;
          };
        };
      } else {
        if (item === search) {
          ret = inx;
          break FIND_LOOP;
        };
      };
    };
    return ret;
  };

  function sort(arr, ...keys) {
    if (!arr) { return arr; };
    if (!keys) { return arr; };
    arr.sort((a, b) => {
      for (const itm of keys) {
        if (typeof itm === "string") {
          const key = itm;
          if (a[key] == b[key]) {
            continue;
          } else if (a[key] > b[key]) {
            return 1;
          } else if (a[key] < b[key]) {
            return -1;
          };
        } else {
          const key = itm.key;
          const sig = itm.odr === "descending" ? -1 : 1;
          if (a[key] == b[key]) {
            continue;
          } else if (a[key] > b[key]) {
            return 1 * sig;
          } else if (a[key] < b[key]) {
            return -1 * sig;
          };
        };
      };
      return 0;
    });
    return arr;
  };

  function swap(arr, inx1, inx2) {
    if (!arr) { return 0; };
    let v = arr[inx1];
    arr[inx1] = arr[inx2];
    arr[inx2] = v;
  };

  function hierarchy(list, idKey, parentKey, subListKey, sortKey, extmap) {
    const ret = [ ];
    const map = extmap ? extmap : { };
    if (!list) { return ret; };
    const olist = clone(list);
    /** 1차 LOOP 맵생성 */
    for (const item of olist) {
      const cid = item[idKey];
      map[cid] = item;
    };
    /** 2차 LOOP 부모찾아 배열하기 */
    for (const item of olist) {
      const pid = item[parentKey];
      if (map[pid]) {
        const parent = map[pid];
        if (!parent[subListKey]) {
          parent[subListKey] = [ ];
        };
        parent[subListKey].push(item);
      } else {
        /** 부모노드가 없다면 루트아이템으로 인식. */
        ret.push(item);
      };
    };
    /** 정렬. */
    if (sortKey !== U) {
      const doSort = function(slist, depth) {
        slist.sort(function(a, b) { return a[sortKey] - b[sortKey]; });
        for (const item of slist) {
          const subList = item[subListKey];
          if (subList && subList.length > 0) {
            log.trace("SUBLIST:", subList);
            doSort(subList, depth + 1);
          };
        };
      };
      doSort(ret, 0);
    };
    return ret;
  };

  function numeric(str) {
    str = String(str ? str : "").trim();
    let minus = /^[-]/.test(str);
    let dpoint = "";
    // str = str.replace(/^[0.]+/g, "");
    str = str.replace(/[^0-9.]/g, "");
    if (!str) { str = ""; };
    /** 앞자리 0 제거 */
    if (str.length > 1) { str = str.replace(/^[0]+([1-9]+)/g, "$1"); };
    /** 소숫점 떼기 */
    {
      let d = str.split(/\./);
      if (d.length > 1) { dpoint = d[1]; };
      str = d[0];
    };
    /** 공백이라면 0 으로 치환 */
    if (str.length == 0) { str = "0"; };
    let ret = "";
    let len = str.length;
    let digit = "";
    for (let inx = 0; inx < len; inx++) {
      digit = str.substring(len - inx - 1, len - inx);
      if (inx > 0 && inx % 3 == 0) {
        ret = digit + "," + ret;
      } else {
        ret = digit + ret;
      };
    };
    if (minus) { ret = `-${ret}`; };
    if (dpoint) { ret = `${ret}.${dpoint}`; };
    return ret;
  };

  function replaceLink(v) {
    /** 문장 중 링크가 발견되면 a태그 덧씌움 */
    let ret = String(v);
    /** 하이퍼텍스트 링크 */
    ret = v.replace(/(http[s]{0,1}\:\/\/[a-zA-Z0-9._\/\|?=\&-]+)/g,
      `<a target="_blank" href="$1">$1</a>`);
    /** 이메일 링크 */
    ret = v.replace(/((([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))\@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,})))/g,
      `<a target="_blank" href="mailto:$1">$1</a>`);
    return ret;
  };

  function getPattern(t, v) {
    let ret = U;
    switch(t) {
    case "number":
      ret = /^([\+\-]{0,1})[0-9]+$/;
      break;
    case "numeric":
      ret = /^([\+\-]{0,1})[0-9]+(\,[0-9]{3})*(\.[0-9]+){0,1}$/;
      break;
    case "alpha":
      ret = /^[a-zA-Z]+$/;
      break;
    case "alphaspc":
      ret = /^[\sa-zA-Z]+$/;
      break;
    case "alphanum":
      ret = /^[a-zA-Z0-9]+$/;
      break;
    case "alphanumspc":
      ret = /^[\sa-zA-Z0-9]+$/;
      break;
    case "alphastart":
      ret = /^[a-zA-Z].*$/;
      break;
    case "ascii":
      ret = /^[\x00-\x7F]+$/;
      break;
    case "date":
      ret = /^([0-9]{4}[-]{0,1}[0-9]{2}[-]{0,1}[0-9]{2})([ ]{0,1}[0-9]{2}[:]{0,1}[0-9]{2}[:]{0,1}[0-9]{2}(.[0-9]{1,3}){0,1}){0,1}$/;
      break;
    case "email":
      ret = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))\@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
      break;
    case "password":
      /** Minimum eight characters, at least one letter and one number: */
      // ret = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/;
      /** Minimum eight characters, at least one letter, one number and one special character: */
      // ret = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
      /** Minimum eight characters, at least one uppercase letter, one lowercase letter and one number: */
      // ret = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/;
      /** Minimum eight characters, at least one uppercase letter, one lowercase letter, one number and one special character: */
      // ret = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
      ret = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{4,}$/;
      break;
    };
    if (v !== null && v !== U) { return ret.test(String(v).trim()); };
    return ret;
  };

  /**
   * 유니코드 조합공식
   * (초성) * 588 + (중성) * 28 + (종성) + 44302
   */
  const BASE_CODE = 44032;
  const BLANK = "　";

  /** 3벌(초,중,종성) 테이블 */
  const TB_CJJ = [
    /** 19개 초성 자음 */
    ["ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"],
    /** 21개 중성 모음 */
    ["ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"],
    /** 16개 종성 자음 (27칸) */
    ["　", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ", "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ", "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ", "ㅌ", "ㅍ", "ㅎ"],
  ];

  /** 사용하는 조사만 간추림. */
  const LST_JOSA = [
    ["을", "를"],
    ["은", "는"],
    ["이", "가"],
    ["의", "의"],
    ["에", "에"],
  ];

  /** 암호화 기본키 저장소 */
  const cryptovars = {
    NIL_ARR: CryptoJS.enc.Hex.parse("00"),
    aes: {
      defbit: 256,
      defkey: undefined,
      opt: {
        iv: undefined,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
      }
    },
    rsa: {
      JSEncrypt: JSEncrypt,
      BigInteger: BigInteger,
      SecureRandom: SecureRandom,
      parseBigInt: parseBigInt,
      b64tohex: b64tohex,
      hex2b64: hex2b64,
      cryptor: undefined,
      defkey: ""
    },
    tobig: function(v) {
      let ret = v;
      if (v && typeof v === "number") {
        ret = cryptovars.rsa.parseBigInt(v.toString(16), 16);
      };
      return ret;
    },
    pkcsunpad: function(bint, len) {
      var buf = bint.toByteArray();
      var inx = 0;
      while (buf[inx] != 0) {
        if (++inx >= buf.length) { return null; };
      };
      var ret = "";
      while (++inx < buf.length) {
        var c = buf[inx] & 255;
        if (c < 128) {
          ret += String.fromCharCode(c);
        } else if ((c > 191) && (c < 224)) {
          ret += String.fromCharCode(((c & 31) << 6) | (buf[inx + 1] & 63));
          ++inx;
        } else {
          ret += String.fromCharCode(((c & 15) << 12) | ((buf[inx + 1] & 63) << 6) | (buf[inx + 2] & 63));
          inx += 2;
        };
      };
      return ret;
    },
    pkcspad: function(msg, pos) {
      /** TODO: fix for utf-8 */
      if (pos < msg.length + 11) { return (log.error("Message too long for RSA")); };
      let buf = [];
      let inx = msg.length - 1;
      while (inx >= 0 && pos > 0) {
        let ch = msg.charCodeAt(inx--);
        /** encode using utf-8 */
        if (ch < 128) {
          buf[--pos] = ch;
        } else if ((ch > 127) && (ch < 2048)) {
          buf[--pos] = (ch & 63) | 128;
          buf[--pos] = (ch >> 6) | 192;
        } else {
          buf[--pos] = (ch & 63) | 128;
          buf[--pos] = ((ch >> 6) & 63) | 128;
          buf[--pos] = (ch >> 12) | 224;
        };
      };
      buf[--pos] = 0;
      let rng = new cryptovars.rsa.SecureRandom();
      let x = [];
      /** random non-zero pad */
      while (pos > 2) {
        x[0] = 0;
        /** PUBLIC-KEY ENCRYPTION 인 경우 rng 적용, PRIVATE 인 경우 255 */
        // while (x[0] == 0) { rng.nextBytes(x) }
        x[0] = 255;
        buf[--pos] = x[0];
      };
      /** PUBLIC-KEY ENCRYPTION 인 경우 2,  PRIVATE 인 경우 1 */
      // buf[--pos] = 2
      buf[--pos] = 1;
      buf[--pos] = 0;
      return new cryptovars.rsa.BigInteger(buf);
    }
  };

  const crypto = {
    /** AES 모듈 */
    aes: {
      init: async function(key) {
        if (key) { cryptovars.aes.defkey = crypto.aes.key(key); };
      },
      decrypt: function(msg, key) {
        let hkey = key ? crypto.aes.key(key) : cryptovars.aes.defkey;
        return CryptoJS.AES.decrypt(msg, hkey, cryptovars.aes.opt).toString(CryptoJS.enc.Utf8);
      },
      encrypt: function(msg, key) {
        let hkey = key ? crypto.aes.key(key) : cryptovars.aes.defkey;
        return CryptoJS.AES.encrypt(msg, hkey, cryptovars.aes.opt).toString();
      },
      key: function(key, bit = cryptovars.aes.defbit) {
        let ret = undefined;
        if (key) {
          if (typeof key === "string" || typeof key === "number") {
            key = String(key);
            const b64len = Math.round(bit * 3 / 2 / 8);
            if (key.length > (b64len)) { key = String(key).substring(0, b64len); };
            if (key.length < (b64len)) { key = String(key).padEnd(b64len, "\0"); };
            if (ret === undefined) { try { ret = crypto.b64dec(key); } catch (e) { log.debug("E:", e); }; };
            if (ret === undefined) { try { ret = crypto.hexdec(key); } catch (e) { log.debug("E:", e); }; };
          } else {
            if (key.__proto__ === CryptoJS.lib.WordArray) { ret = key; };
          };
        };
        return ret;
      },
      setDefaultKey(key, bit = cryptovars.aes.defbit) {
        if (bit && bit !== cryptovars.aes.defbit) { cryptovars.aes.defbit = bit; };
        cryptovars.aes.defkey = crypto.aes.key(key, bit);
      }
    },
    /** RSA 모듈 / JSEncrypt 에서는 private key 를 사용해야만 암/복호화가 모두 지원된다 */
    rsa: {
      init: async function(keyval, keytype) {
        if (!cryptovars.rsa.JSEncrypt) {
          const cryptor = cryptovars.rsa.cryptor = new cryptovars.rsa.JSEncrypt();
          switch (keytype) {
          case "privateKey": case undefined: { cryptor.setPrivateKey(keyval); } break;
          case "publicKey": { cryptor.setPublicKey(keyval); } break;
          };
        };
      },
      keygen: function(bit = 1024) {
        const crypt = new JSEncrypt({ default_key_size: bit });
        crypt.getKey();
        const privatekey = String(crypt.getPrivateKey())
          .replace(PTN_CRYPTOKEY_HDR, "")
          .replace(PTN_NL, "")
          .trim();
        const pubkey = String(crypt.getPublicKey())
          .replace(PTN_CRYPTOKEY_HDR, "")
          .replace(PTN_NL, "")
          .trim();
        // const privatekey = String(crypt.getPrivateKey())
        //   .trim();
        // const pubkey = String(crypt.getPublicKey())
        //   .trim();
        return [privatekey, pubkey];
      },
      decrypt: function(msg, key) {
        let cryptor = cryptovars.rsa.cryptor;
        if (key) {
          cryptor = new cryptovars.rsa.JSEncrypt();
          cryptor.setKey(key);
        };
        const kobj = cryptor.getKey();
        if (kobj.d) {
          // log.trace("PRV-DEC", msg)
          return cryptor.decrypt(msg)
        } else {
          // log.trace("PUB-DEC", msg)
          let ret = undefined;
          const c = cryptovars.rsa.parseBigInt(cryptovars.rsa.b64tohex(msg), 16);
          const e = cryptovars.tobig(kobj.e);
          ret = c.modPow(e, kobj.n);
          // log.trace("N:", tohex(kobj?.n))
          // log.trace("E:", tohex(kobj?.e))
          // log.trace("DECRYPT:", tohex(ret))
          ret = cryptovars.pkcsunpad(ret, (kobj.n.bitLength() + 7) >> 3);
          return ret;
        }
      },
      encrypt: function(msg, key) {
        let cryptor = cryptovars.rsa.cryptor;
        if (key) {
          cryptor = new cryptovars.rsa.JSEncrypt();
          cryptor.setKey(key);
        };
        const kobj = cryptor.getKey();
        if (!kobj.d) {
          return cryptor.encrypt(msg);
        } else {
          // log.trace("PRV-ENC", msg);
          let ret = undefined;
          let maxLength = (kobj.n.bitLength() + 7) >> 3;
          let c = cryptovars.pkcspad(msg, maxLength);
          ret = c.modPow(cryptovars.tobig(kobj.d), kobj.n);
          // log.trace("PADDING:", tohex(c));
          // log.trace("N:", tohex(kobj?.n));
          // log.trace("D:", tohex(kobj?.d));
          // log.trace("ENCRYPT:", tohex(ret));
          ret = ret.toString(16);
          let length = ret.length;
          /** fix zero before result */
          for (var inx = 0; inx < maxLength * 2 - length; inx++) { ret = "0" + ret; };
          ret = cryptovars.rsa.hex2b64(ret);
          return ret;
        }
      }
    },
    b64dec: function(key) {
      let ret = cryptovars.NIL_ARR;
      try { ret = CryptoJS.enc.Base64.parse(key); } catch (e) { log.debug("E:", e); };
      return ret;
    },
    hexdec: function(key) {
      let ret = cryptovars.NIL_ARR;
      try { ret = CryptoJS.enc.Hex.parse(key);k } catch (e) { log.debug("E:", e); };
      return ret;
    },
  };

  class Hangul {
    /** 1글자의 초, 중, 종성을 분리한다. */
    extract(ch) {
      const ret = ["", "", ""];
      if (ch && ch.length == 1) {
        const code = String(ch).charCodeAt(0);
        let mod = code - BASE_CODE;
        /** 초성분리 */
        for (let inx = TB_CJJ[0].length - 1; inx >= 0; inx--) {
          if (mod >= (inx * 588)) {
            mod = mod - inx * 588;
            ret[0] = TB_CJJ[0][inx];
            break;
          };
        };
        log.trace("MOD-1:", mod, ret);
        /** 중성분리 */
        for (let inx = TB_CJJ[1].length - 1; inx >= 0; inx--) {
          if (mod >= (inx * 28)) {
            mod = mod - inx * 28;
            ret[1] = TB_CJJ[1][inx];
            break;
          };
        };
        log.trace("MOD-2:", mod, ret);
        /** 종성분리 (사실 종성 자체는 이 시점에서 바로 mod 값임, 로직 정리를 위해 아래 코드 기재) */
        for (let inx = TB_CJJ[2].length - 1; inx >= 0; inx--) {
          if (TB_CJJ[2][inx] === BLANK) { continue; };
          if (mod >= inx) {
            mod = mod - inx;
            ret[2] = TB_CJJ[2][inx];
            break;
          };
        };
        log.trace("MOD-3:", mod, ret);
        /** mod 는 여기서 0 이어야 한다. */
      };
      return ret;
    };

    /**
     * 조사판단.
     * xx (을/를) 입력해주세요 -> 이름을 입력해주세요 / 번호를 입력해주세요
     * detectJosa("이름", "을") => "이름을"
     * detectJosa("번호", "을") => "번호를"
     * detectJosa("이름", "은") => "이름을"
     * detectJosa("번호", "은") => "번호는"
     * detectJosa("이름", "이") => "이름이"
     * detectJosa("번호", "이") => "번호가"
     */
    detectJosa(str, josa, wrap) {
      if (str) { str = str.trim(); };
      let ret = [str, josa];
      if (!str || str.length <= 1 || !josa) { return ""; };
      let josaSet = [ ];
      LOOP1: for (const set of LST_JOSA) {
        for (const str of set) {
          if (str == josa) {
            josaSet = set;
            break LOOP1;
          };
        };
      };
      /** 찾는 조사SET 이 없으면 바로 종료 */
      if (josaSet.length == 0) { return `${ret[0]}${ret[1]}`; };
      let lch = str.charAt(str.length - 1);
      const ext = hangul.extract(lch);
      if (ext[2]) {
        /** 받침이 있는 경우 */
        ret[1] = josaSet[0];
      } else {
        /** 받침이 없는 경우 */
        switch (lch.toLowerCase()) {
          case "l": case "m": case "n": case "r":
          case "1": case "3": case "6": case "7":
          case "8": case "0":
            ret[1] = josaSet[0];
            break;
          default:
            ret[1] = josaSet[1];
            break;
        };
      };
      if (wrap) {
        if (wrap.length > 1) {
          return `${wrap[0]}${ret[0]}${wrap[1]}${ret[1]}`;
        } else {
          return `${wrap}${ret[0]}${wrap}${ret[1]}`;
        };
      } else {
        return `${ret[0]}${ret[1]}`;
      };
    };

    /**
     * 이름{term -> replace} 을{josa} 입력해주세요{str}
     */
    replaceWithJosa (term, replace, josa, str) {
      let ret = str;
      ret = str.replace(term, hangul.detectJosa(replace, josa));
      return ret;
    };
  };

  const hangul = new Hangul();

  function px2rem(v, el) {
    v = Number(String(v).replace(/[^0-9^.]+/g, ""));
    if (isNaN(v)) { v = 0; };
    if (!el) { el = document.documentElement; };
    return v / parseFloat(getComputedStyle(el).fontSize);
  };
  function rem2px(v, el) {
    v = Number(String(v).replace(/[^0-9^.]+/g, ""));
    if (isNaN(v)) { v = 0; };
    if (!el) { el = document.documentElement; };
    return v * parseFloat(getComputedStyle(el).fontSize);
  };
  function getText(element) { return element ? $(element).text() : ""; };
  function getFrom(v, k) { return v ? v[k] : U ; };
  function put(target, key, value) { if (target && key) { target[key] = value; }; };
  function strm(v) { return String(v ? v : "").replace(/[ \t]+/g, " ").trim(); };

  /** target 에서 exclude 나열된 것들을 제외한 모든 요소를 복제한 객체 생성 */
  function copyExclude(target, excludes = []) {
    let ret = { };
    const keys = Object.keys(target);
    for (const key of keys) {
      if (excludes.indexOf(key) !== -1) { continue; };
      ret[key] = (target)[key];
    };
    return ret;
  };
  /** 목표객체(target) 의 key 값을 가지는 내용만 복사. */
  function copyExists(target, source, keys) {
    if (targets === U || target === null) { return; };
    if (source === U || source === null) { return; };
    if (keys !== U && keys !== null) {
      for (const k of keys) {
        target[k] = source[k];
      };
    } else {
      for (const k in target) {
        target[k] = source[k];
      };
    };
    return target;
  };

  function isEvent(e) { return !!(e && e.preventDefault && e.stopPropagation); };
  function cancelEvent (e) {
    if (e && e.preventDefault && e.stopPropagation) {
      e.preventDefault();
      e.stopPropagation();
    };
  };

  function until(check, opt) {
    if (opt === null || opt === undefined) { opt = { }; };
    const ctx = {
      __max_check: opt.maxcheck ? opt.maxcheck : 100,
      __interval: opt.interval ? opt.interval : 100
    };
    return new Promise<any>(function(resolve, _reject) {
      function fnexec() {
        /** 조건을 만족시키면 */
        if (check()) {
            resolve(true);
        } else if (ctx.__max_check > 0) {
          ctx.__max_check--;
          setTimeout(fnexec, ctx.__interval);
        } else {
          resolve(false);
        };
      };
      fnexec();
    });
  };

  /** SLEEP (ms) */
  async function sleep(time) {
    return new Promise(function(resolve, _reject) {
      log.trace("SLEEP", time);
      setTimeout(function() {
        log.trace("SLEEP DONE!");
        resolve(null);
      }, time);
    });
  };

  class Paging {
    constructor(rowCount, pageCount, rowTotal) {
      rowCount = Number(rowCount);
      pageCount = Number(pageCount);
      rowTotal = Number(rowTotal);
      if (isNaN(rowCount)) { rowCount = ROWS_DEF; };
      if (isNaN(pageCount)) { pageCount = PAGES_DEF; };
      if (isNaN(rowTotal)) { rowTotal = 0; };
      this.rowCount = rowCount;
      this.pageCount = pageCount;
      this.rowTotal = rowTotal;
    };

    rowNumbers(pn) {
      pn = Number(pn);
      if (isNaN(pn)) { pn = 1; };
      if (pn < 1) { pn = 1; };
      let rns = (pn - 1) * this.rowCount + 1;
      let rne = rns + this.rowCount;
      if (rne > this.rowTotal) { rne = this.rowTotal; };
      return [rns, rne];
    };

    pageNumbers(pn) {
      pn = Number(pn);
      if (isNaN(pn)) { pn = 1; };
      if (pn < 1) { pn = 1; };
      let mod = 0;
      const pnt = Math.ceil(this.rowTotal / this.rowCount);
      // if (pn > this.pages) { mod = (pn - 1) %  this.pages }
      mod = (pn - 1) %  this.pageCount;
      let pns = pn - mod;
      let pne = pns + this.pageCount - 1;
      if (pne > pnt) { pne = pnt; };
      return [pns, pne, pnt];
    };
  };

  function update() {
    if (appvars.instance._) {
      log.debug("UPDATE:", appvars.instance._);
      appvars.instance._.update();
    }
  };

  /** 사전 정의된 validation 함수들 */
  function validations() {
    const { detectJosa } = hangul;
    function josa(name, tail, wrap) {
      const ret = detectJosa(name, tail, wrap);
      return ret;
    };
    return {
      "auto": {
        validate(v, p) {
          log.trace("V-AUTO:", v, p, (v !== undefined && v !== "" && v !== false));
          return true;
        }
      },
      "test": {
        validate(v, p) { return true; },
        message(v, p) { }
      },
      "required": function(v, p, c) {
        const value = v.value;
        let invalid = !(value !== undefined && value !== null && value !== "" && value !== false);
        log.trace("V-REQUIRED:", v.name, value, p, invalid);
        if (invalid) {
          if (v.type == "checkbox") {
            let name = josa(v.name, "에");
            return String(`#(name) 반드시 체크해 주세요`)
              .replace(/\#\(name\)/g, name);
          } else if (v.type == "select" || v.type == "combobox") {
            let name = josa(v.name, "은");
            return String(`#(name) 반드시 선택해 주세요`)
              .replace(/\#\(name\)/g, name);
          } else {
            let name = josa(v.name, "은");
            return String(`#(name) 반드시 입력해 주세요`)
              .replace(/\#\(name\)/g, name);
          }
        };
        return true;
      },
      "nospc": function(v, p) {
        if (/ /g.test(v.value)) {
          let name = josa(v.name, "은");
          return String(`#(name) 공백을 입력할수 없어요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "number": function(v, p) {
        if (!getPattern("number", v.value)) {
          let name = josa(v.name, "은");
          return String(`#(name) 숫자만 입력해 주세요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "numeric": function(v, p) {
        if (!getPattern("numeric", v.value)) {
          let name = josa(v.name, "은");
          return String(`#(name) 숫자만 입력해 주세요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "alpha": function(v, p) {
        if (!getPattern("alpha", v.value)) {
          let name = josa(v.name, "은");
          return String(`#(name) 영문으로만 입력해 주세요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "alphaspc": function(v, p) {
        if (!getPattern("alphaspc", v.value)) {
          let name = josa(v.name, "은");
          return String(`#(name) 영문으로만 입력해 주세요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "alphastart": function(v, p) {
        if (!(getPattern(C.ALPHASTART, v.value))) {
          let name = josa(v.name, "의");
          return String(`#(name) 첫글자는 반드시 영문으로 입력해 주세요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "alphanum": function(v, p) {
        if (!(getPattern("alphanum", v.value))) {
          let name = josa(v.name, "은");
          return String(`#(name) 영문 또는 숫자로만 입력해 주세요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "alphanumspc": function(v, p) {
        if (!(getPattern("alphanumspc", v.value))) {
          let name = josa(v.name, "은");
          return String(`#(name) 영문 또는 숫자로만 입력해 주세요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "ascii": function(v, p) {
        if (!getPattern("ascii", v.value)) {
          let name = josa(v.name, "은");
          return String(`#(name) 영문, 숫자 또는 기호만 입력해 주세요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "hangul": function(v, p) {
        if (!getPattern("hangul", v.value)) {
          let name = josa(v.name, "은");
          return String(`#(name) 한글만 입력해 주세요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "date": function(v, p) {
        let valid = true;
        if (valid && !getPattern("date", v.value)) { valid = false; };
        if (valid) {
          const d = String(v.value).split("-");
          const f = formatDate(makeDate(d[0], d[1], d[2]), DATE_FORMAT_YMD);
          log.trace("CHECK-DATE:", v.value, valid, d, f);
          if (v.value != f) { valid = false; };
        };
        if (!valid) {
          let name = josa(v.value, "은");
          return String(`#(name) 올바른 날자 형식이 아니예요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "date-ym": function(v, p) {
        let valid = true;
        if (valid && !/^([0-9]{4}-[0-9]{1,2})$/.test(v.value)) { valid = false; };
        if (valid) {
          const d = String(v.value).split("-");
          const f = formatDate(makeDate(d[0], d[1]), DATE_FORMAT_YM);
          const check = `${lpad(d[0], 4, "0")}-${lpad(d[1], 2, "0")}`;
          if (check != f) { valid = false; };
        };
        if (!valid) {
          let name = josa(v.value, "은");
          return String(`#(name) 올바른 날자 형식이 아니예요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "email": function(v, p) {
        let t;
        if (!getPattern("email", v.value)) {
          let name = josa(v.value, "은");
          return String(`#(name) 올바른 이메일 형식이 아니예요`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "password": function(v, p) {
        let t;
        if (!getPattern("password", v.value)) {
          let name = josa(v.name, "은");
          return String(`#(name) 4자리 이상, 영문자, 숫자, 기호를 반드시 섞어서 입력해 주세요.`)
            .replace(/\#\(name\)/g, name);
        };
        return true;
      },
      "content-len": function(v, p) {
        let t;
        const vmin = num(nval(p, 0), 0);
        const vmax = num(nval(p, 1), 0);
        const div = document.createElement("div");
        div.innerHTML = v.value;
        const clen = String(div.innerText).trim().length;
        if (vmin > 0 && clen < vmin) {
          let name = josa(v.name, "의");
          return String(`#(name) 길이는 최소 #(min) 글자 입니다.`)
            .replace(/\#\(name\)/g, name)
            .replace(/\#\(min\)/g, String(numeric(vmin)));
        };
        if (vmax > 0 && clen > vmax) {
          let name = josa(v.name, "의");
          return String(`#(name) 길이는 최대 #(max) 글자 입니다.`)
            .replace(/\#\(name\)/g, name)
            .replace(/\#\(min\)/g, String(numeric(vmin)));
        };
        return true;
      },
      "len": function(v, p, c) {
        let t;
        const vmin = num(nitem(p, 0), 0);
        const vmax = num(nitem(p, 1), 0);
        if (vmin > 0 && v.value && String(v.value ? v.value : "").trim().length < vmin) {
          let name = josa(v.name, "의");
          return String(`#(name) 길이는 최소 #(min) 글자 입니다.`)
            .replace(/\#\(name\)/g, name)
            .replace(/\#\(min\)/g, String(numeric(vmin)));
        };
        if (vmax > 0 && v.value && String(v.value).length > vmax) {
          let name = josa(v.name, "의");
          return String(`#(name) 길이는 최대 #(max) 글자 입니다.`)
            .replace(/\#\(name\)/g, name)
            .replace(/\#\(max\)/g, String(numeric(vmax)));
        };
        return true;
      },
      "minv": function(v, p, c) {
        let t;
        const vmin = num(nitem(p, 0), 0);
        /** FIXME: 부호체크(+-) 가능하도록 수정할것 */
        if (Number(numberOnly(v.value ? v.value : 0)) < vmin) {
          let name = josa(v.name, "은");
          return String(`#(name) #(min) 이상의 값을 입력해 주세요`)
            .replace(/\#\(name\)/g, name)
            .replace(/\#\(min\)/g, String(numeric(vmin)));
        };
        return true;
      },
      "maxv": function(v, p, c) {
        let t;
        const vmax = num(nitem(p, 0), 0);
        /** FIXME: 부호체크(+-) 가능하도록 수정할것 */
        if (Number(numberOnly(v.value ? v.value : 0)) > vmax) {
          let name = josa(v.name, "은");
          return String(`#(name) #(max) 이하의 값을 입력해 주세요`)
            .replace(/\#\(name\)/g, name)
            .replace(/\#\(max\)/g, String(numeric(vmax)));
        };
        return true;
      },
      "atleast": function(v, p, c) {
        log.trace("ATLEAST:", v, p, c);
        const count = p[0];
        let found = 0;
        let vv = p ? p[1] :  undefined;
        if (v.value && v.value instanceof Array) {
          LOOP: for (const itm of v.value) {
            if ((vv && itm == vv) || (!vv && itm)) {
              found++;
              if (found >= count) { break LOOP; };
            };
            continue LOOP;
          };
        };
        if (found < count) {
          if (v.type == "checkbox") {
            let name = josa(v.name, "에");
            return String(`#(name) 반드시 #(count)개 이상 체크해 주세요`)
              .replace(/\#\(name\)/g, name)
              .replace(/\#\(count\)/g, count);
          } else if (v.type == "select") {
            let name = josa(v.name, "은");
            return String(`#(name) 반드시 #(count)개 이상 선택해 주세요`)
              .replace(/\#\(name\)/g, name)
              .replace(/\#\(count\)/g, count);
          } else {
            let name = josa(v.name, "은");
            return String(`#(name) 반드시 #(count)개 이상 입력해 주세요`)
              .replace(/\#\(name\)/g, name)
              .replace(/\#\(count\)/g, count);
          }
        } else {
          return true;
        };
      },
      "atmost": function(v, p, c) {
        log.trace("ATMOST:", v, p, c);
        const count = p[0];
        let found = 0;
        let vv = p ? p[1] :  undefined;
        if (v.value && v.value instanceof Array) {
          LOOP: for (const itm of v.value) {
            if ((vv && itm == vv) || (!vv && itm)) { found++; };
            continue LOOP;
          };
        };
        if (found > count) {
          if (v.type == "checkbox") {
            let name = josa(v.name, "은");
            return String(`#(name) #(count)개 이상 체크할 수 없어요`)
              .replace(/\#\(name\)/g, name)
              .replace(/\#\(count\)/g, count);
          } else if (v.type == "select") {
            let name = josa(v.name, "은");
            return String(`#(name) #(count)개 이상 선택할 수 없어요`)
              .replace(/\#\(name\)/g, name)
              .replace(/\#\(count\)/g, count);
          } else {
            let name = josa(v.name, "은");
            return String(`#(name) #(count)개 이상 입력할 수 없어요`)
              .replace(/\#\(name\)/g, name)
              .replace(/\#\(count\)/g, count);
          }
        } else {
          return true;
        };
      },
    };
  };

  function registFormElement(self, elem) {
    const { props, attrs } = (self && self.setupState) ? self.setupState : {};
    let o;
    if ((self.setupState) && (o = self.root) && (o = o.refs) && (o = o[props.form]) && (o.registFormElement)) {
      o.registFormElement(self.setupState, elem);
    };
  };

  async function formSubmit($form, opt) {
    if (!$form) { return; };
    if (!$form instanceof jQuery) { $form = $($form); };
    try {
      dialog.progress(true);
      const $elist = $form.find("input,textarea,select");
      const $vform = $(document.createElement("form"));
      $(["action"]).each(
        function(i, k) {
          let v = $form.attr(k);
          if (v) { $vform.attr(k, v); };
        }); 
      for(let inx = 0; inx < $elist.length; inx++) {
        const $elem = $($elist[inx]);
        const tagName = String($elem.prop("tagName")).toLowerCase();
        const type = $elem.prop("type");
        const name = $elem.attr("name");
        const value = $elem.attr("value");
        const group = $elem.attr("data-group");
        const index = $elem.attr("data-index");
        const checked = $elem.attr("checked");
        log.trace("FORM-ELEMENT:", inx, name, tagName, type, value, checked, group, index);
        let $velem = $(document.createElement("input"))
          .attr("type", "hidden");
        if (tagName === "input") {
          switch (type) {
          case "checkbox": case "radio": {
            if (group !== undefined && index !== undefined) {
              $velem.attr("name", name).attr("value", value);
            } else if (checked) {
              $velem.attr("name", name).attr("value", value);
            } else {
              $velem = undefined;
            };
          } break;
          case "text": case "hidden": default: {
            $velem.attr("name", name).attr("value", value);
          } break;
          };
        };
        if ($velem) { $vform.append($velem); }
      };
      $vform
        .attr("method", "post")
        .attr("enctype", "application/x-www-form-urlencoded");
      $(document.body).append($vform);
      log.debug("FORM:", $vform[0].outerHTML);
      $vform.submit();
      $vform.remove();
    } catch (e) {
      log.debug("E:", e);
    } finally {
      dialog.progress(false);
    };
  };

  async function formToJson($form, opt) {
    let ret = {};
    if (!$form) { return; };
    if (!$form instanceof jQuery) { $form = $($form); };
    try {
      dialog.progress(true);
      const $elist = $form.find("input,textarea,select");
      for(let inx = 0; inx < $elist.length; inx++) {
        const $elem = $($elist[inx]);
        const tagName = String($elem.prop("tagName")).toLowerCase();
        const type = $elem.prop("type");
        const name = $elem.attr("name");
        const value = $elem.attr("value");
        const group = $elem.attr("data-group");
        const index = $elem.attr("data-index");
        const checked = $elem.attr("checked");
        log.trace("FORM-ELEMENT:", inx, name, tagName, type, value, checked, group, index);
        if (tagName === "input") {
          switch (type) {
          case "checkbox": case "radio": {
            if (group !== undefined && index !== undefined) {
              if (!ret[name]) { ret[name] = []; };
              ret[name][index] = value;
            } else if (checked) {
              ret[name] = value;
            } else {
            };
          } break;
          case "text": case "hidden": default: {
            ret[name] = value;
          } break;
          };
        };
      };
      log.debug("RET:", ret);
    } catch (e) {
      log.debug("E:", e);
    } finally {
      dialog.progress(false);
    };
    return ret;
  };

  async function validateForm(form, opt) {
    let ret = false;
    if (!form) {
      log.debug("폼 객체가 올바르지 않아요");
      return ret;
    };
    if (form.validateForm) {
      /** NO-OP */
    } else if (form instanceof jQuery && form[0] && form[0].validateForm) {
      form = form[0];
    };
    if (form && form.validateForm) {
      ret = await form.validateForm(opt, validations);
    };
    return ret;
  };
  watch(function() { return dialogvars.value.modal.queue.length }, function(n, o) {
    if (o == 0 && n > 0) { doModal(); };
  });
  watch(function() { return dialogvars.value.progress.queue.length }, function(n, o) {
    if (o == 0 && n > 0) { doProgress(); };
  });

  const MOUNT_HOOK_PROCS = [];
  const UNMOUNT_HOOK_PROCS = [];

  /** [ 레이어팝업 관련 스크립트 */
  MOUNT_HOOK_PROCS.push(async function(app) {
    // log.debug("MOUNT:", app._.refs);
    appvars.instance = app;
    const modalref = app.$refs["dialogvars.modal.ref"];
    const progressref = app.$refs["dialogvars.progress.ref"];
    dialogvars.value.modal.instance = new bootstrap.Modal(modalref, {});
    dialogvars.value.progress.instance = new bootstrap.Modal(progressref, {});
    progressref.addEventListener(M_SHOWN, dialogvars.value.progress.handlevis);
    progressref.addEventListener(M_HIDDEN, dialogvars.value.progress.handlevis);
    modalref.addEventListener(M_HIDDEN, doModal);
  });
  UNMOUNT_HOOK_PROCS.push(async function(app) {
    const modalref = app.$refs["dialogvars.modal.ref"];
    const progressref = app.$refs["dialogvars.progress.ref"];
    progressref.removeEventListener(M_SHOWN, dialogvars.value.progress.handlevis);
    progressref.removeEventListener(M_HIDDEN, dialogvars.value.progress.handlevis);
    modalref.removeEventListener(M_HIDDEN, doModal);
  });
  /** ] 레이어팝업 관련 스크립트 */

  function BIND_VALUES({ props, context }) {
    return { vars, dialogvars, props, context };
  };

  /** 공통 사용을 위해 entry.js 와 launch-script.jsp 의 항목을 맞춰 주어야 한다. */
  if (callback) { callback({
  vars: vars.value,
  BIND_VALUES,
  KEYCODE_REV_TABLE,
  KEYCODE_TABLE,
  MOUNT_HOOK_PROCS,
  UNMOUNT_HOOK_PROCS,
  cancelEvent,
  clone,
  copyExclude,
  copyExists,
  crypto,
  dateStrFormat,
  dialog,
  dialogvars,
  doModal,
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
  }); };
}