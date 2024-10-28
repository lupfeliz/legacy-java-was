<%!
/**
 * @File        : launch-script.jsp
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 페이지 런치 스크립트
 * @Site        : https://devlog.ntiple.com
 **/
%>
<script>
{
  var createApp = Vue.createApp;
  var ref = Vue.ref;
  Vue.createApp({
    setup() {
      const message = ref("Hello vue!")
      return {
        message
      }
    }
  }).mount("#app");
}
</script>