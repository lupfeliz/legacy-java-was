/**
 * @File        : c-input.js
 * @Author      : 정재백
 * @Since       : 2024-10-31
 * @Description : 입력컴포넌트
 * @Site        : https://devlog.ntiple.com
 **/

function $component_input(app) {
  const name = "c-input";
  const CInput = {
    template: `
    \ <input
    \   class="form-control"
    \   :vrules=""
    \   v-bind="attrs"
    \   />`,
    name: name,
    setup: function(props) {
      const attrs = Vue.useAttrs();
      return {
        attrs: attrs,
      };
    }
  };
  app.component(name, CInput);
};