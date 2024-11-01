/**
 * @File        : c-input.js
 * @Author      : 정재백
 * @Since       : 2024-10-31
 * @Description : 입력컴포넌트
 * @Site        : https://devlog.ntiple.com
 **/

function $component_input(app) {
  const CInput = {
    template: `<input
    \ class="form-control"
    \ :vrules=""
    \ data-test="OK"
    \ />`,
    name: "c-input",
    props: {
    },
    data:function() {
      return {};
    },
    mounted: async function() {
    }
  };
  app.component("c-input", CInput);
};