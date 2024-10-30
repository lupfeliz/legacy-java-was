/**
 * @File        : entry.js
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 진입점 JS 스크립트
 * @Site        : https://devlog.ntiple.com
 **/

function initEntryScript(callback) {
  const context = { };
  /** 필요한 라이브러리들을 추가한다. */
  context.log = {
    trace: function() { },
    debug: window.console.log,
    info: window.console.log,
    warn: window.console.warn,
    error: window.console.warn
  };

  context.dialog = {

  };
  if (callback) { callback(context); }
}