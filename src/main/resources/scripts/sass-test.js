try {
  const Sass = require('./sass.sync-0.11.1.min.js')
  Sass.compile(`
  div {
    border: 1px solid #f00;
    > div {
      background: #f00;
    }
  }
  `, function(r) {
    console.log(r.text);
  });
} catch (e) {
  // console.log('E:', e.message)
}