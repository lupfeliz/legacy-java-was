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