/**
 * @File        : entry.js
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 진입점 JS 스크립트
 * @Site        : https://devlog.ntiple.com
 **/

function initEntryScript(callback, { vars, log, cbase }) {
  const ref = Vue.ref;
  const watch = Vue.watch;
  const nextTick = Vue.nextTick;
  const M_SHOWN = "shown.bs.modal";
  const M_HIDDEN = "hidden.bs.modal";
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
    MaterialStyle: {}
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

  /** 앱 내 유일키 생성 */
  function genId() { return (new Date().getTime()) + String((appvars.uidseq = (appvars.uidseq + 1) % 1000) + 1000).substring(1, 4) };

  /** 최소(min)~최대(max)값 사이의 난수 생성, 최소값을 입력하지 않을경우 자동으로 0 으로 지정됨 */
  function getRandom(max, min) {
    if (max === undefined) { max = 0; };
    if (min === undefined) { min = 0; };
    if (max < 0) { max = max * -1; };
    const ret = min + Math.floor(Math.random() * max);
    return ret;
  };

  /** 단일문자 난수 */
  function randomChar(c, n) {
    if (c === undefined) { c = "a"; };
    if (n === undefined) { n = 26; };
    return String.fromCharCode(Number(c.charCodeAt(0)) + getRandom(n));
  };

  /** 난수로 이루어진 문자열, number / alpha / alphanum 의 3가지 타입으로 생성가능 */
  function randomStr(length, type) {
    let ret = "";
    switch (type) {
    case undefined:
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

  function putAll(_target, source, opt) {
    let target = _target;
    if (target == null || source == null || target === source ) { return target; };
    if (!opt) { opt = { root: target }; };
    for (const k in source) {
      const titem = target[k];
      const sitem = source[k];
      if (titem !== undefined && titem !== null) {
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
    let ret = undefined;
    const prm = { };
    let o;
    try {
      const d1 = String(((o = history) && (o = o.state) && (o = o.url)) ? o : "").split(/[/]/);
      const d2 = String(((o = history) && (o = o.state) && (o = o.as)) ? o : "").split(/[/]/);
      // log.debug("D:", d1, d2);
      let len = d1.length > d2.length ? d1.length : d2.length;
      for (let inx = 0; inx < len; inx++) {
        if (/[\[]([a-zA-Z0-9_-]+)[\]]/.test(d1[inx] || "")) {
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
    let ret = undefined;
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
    let ret = undefined;
    const win = window;
    // log.debug('CHECK:', tid, win.opener, win.opener[tid]);
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

  const dialogref = ref();
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
      if (vis === undefined) { vis = true };
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
      let width = option.width ? option.width : wm / 3;
      let height = option.height ? option.height : hm / 3;
      /** 화면중앙 */
      let left = option.left ? option.left : (wm - width) / 2;
      /** 상단 1/4 지점 */
      let top = option.top ? option.top : (hm - height) / 4;
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

  function numberOnly(str) {
    let ret = str;
    if (!str) { str = '' };
    ret = String(str).replace(/[^0-9]+/g, '');
    return ret;
  };

  function numToHangul(str) {
    let minus = /^[-]/.test(str);
    str = numberOnly(str);
    if (!str) { str = ''; };
    let ret = '';
    str = str.replace(/^[0]+/g, '');
    if (!str) { str = '0'; };
    let len = str.length;
    let digit = '';
    let word = '';
    let han = ['', '일', '이', '삼', '사', '오', '육', '칠', '팔', '구'];
    let pos1 = ['', '십', '백', '천'];
    let pos2 = ['', '만', '억', '조', '경'];
    function divide(str) {
      let ret = '';
      let len = str.length;
      for (let inx = 0; inx < len; inx++) {
        word = '';
        digit = str.substring(len - inx - 1, len - inx);
        if (digit != '0') {
          if (digit == '1' && inx % 4 != 0) {
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
        if (word.length > 0) { word = word + pos2[Math.floor(inx / 4)] + ' ' };
      };
      ret = '' + word + ret;
    };
    return ret;
  };

  function clone(v) {
    let ret = undefined;
    if (!v) { return ret; };
    try {
      ret = JSON.parse(JSON.stringify(v));
    } catch(e) { };
    return ret;
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
    if (sortKey !== undefined) {
      const doSort = function(slist, depth) {
        slist.sort(function(a, b) { return a[sortKey] - b[sortKey]; });
        for (const item of slist) {
          const subList = item[subListKey];
          if (subList && subList.length > 0) {
            log.trace('SUBLIST:', subList);
            doSort(subList, depth + 1);
          };
        };
      };
      doSort(ret, 0);
    };
    return ret;
  };

  watch(function() { return dialogvars.value.modal.queue.length }, function(n, o) {
    if (o == 0 && n > 0) { doModal(); };
  });
  watch(function() { return dialogvars.value.progress.queue.length }, function(n, o) {
    if (o == 0 && n > 0) { doProgress(); };
  });

  /** 공통 사용을 위해 entry.js 와 launch-script.jsp 의 항목을 맞춰 주어야 한다. */
  if (callback) { callback({
    clone,
    dialog,
    dialogvars,
    doModal,
    genId,
    getGlobalTmp,
    getOpenerTmp,
    getParameter,
    getRandom,
    getUri,
    getUrl,
    hierarchy,
    initpopup,
    log,
    numberOnly,
    numToHangul,
    putAll,
    randomChar,
    randomStr,
    setGlobalTmp,
    setOpenerTmp,
    vars,
  }); };
}