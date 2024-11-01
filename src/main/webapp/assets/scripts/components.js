/**
 * @File        : c-input.js
 * @Author      : 정재백
 * @Since       : 2024-10-31
 * @Description : 입력컴포넌트
 * @Site        : https://devlog.ntiple.com
 **/

function $component_input(param) {
  const app = param.app;
  const log = param.log;
  {
    const name = "c-input";
    const CInput = {
      template: `
      \ <input
      \   class="form-control"
      \   :vrules=""
      \   @keydown="onKeyDown"
      \   v-bind="attrs"
      \   />`,
      name: name,
      setup: function(props) {
        const v = {
          attrs: Vue.useAttrs(),
          onKeyDown: async function(e) {
            // log.debug("E:", e);
            if (props.keydown) {
              props.keydown(e);
            };
          }
        };
        return v;
      }
    };
    app.component(name, CInput);
  }
};