CKEDITOR.plugins.add( 'preview', {
  requires: 'dialog',
  icons: 'preview',
  // hidpi: true,
  init: function (editor) {
    editor.ui.addButton && editor.ui.addButton('Preview', {
      label: '미리보기',
      command: 'preview',
      toolbar: 'tools,1'
    });
  }
});
