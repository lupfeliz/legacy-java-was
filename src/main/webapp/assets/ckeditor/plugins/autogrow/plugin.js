'use strict';
(function () {
  CKEDITOR.plugins.add('autogrow', {
    init: function(editor) {
      if (editor.elementMode == CKEDITOR.ELEMENT_MODE_INLINE) { return }
      editor.on('instanceReady', function() {
        if (editor.editable().isInline()) {
          editor.ui.space('contents').setStyle('height', 'auto');
        } else {
          initIframeAutogrow(editor);
        }
      });
    }
  });
  function initIframeAutogrow(editor) {
    var lastHeight,
      doc,
      markerContainer,
      scrollable,
      marker,
      configBottomSpace = editor.config.autoGrow_bottomSpace || 0,
      configMinHeight = editor.config.autoGrow_minHeight !== undefined ? editor.config.autoGrow_minHeight : 200,
      configMaxHeight = editor.config.autoGrow_maxHeight || Infinity,
      maxHeightIsUnlimited = !editor.config.autoGrow_maxHeight;

    editor.addCommand('autogrow', {
      exec: resizeEditor,
      modes: { wysiwyg: 1 },
      readOnly: 1,
      canUndo: false,
      editorFocus: false
    });

    var eventsList = { contentDom: 1, key: 1, selectionChange: 1, insertElement: 1, mode: 1 };
    for (var eventName in eventsList) {
      editor.on(eventName, function (evt) {
        if (evt.editor.mode == 'wysiwyg') {
          setTimeout(function() {
            if (isNotResizable()) {
              lastHeight = null;
              return;
            }
            resizeEditor();
            if (!maxHeightIsUnlimited) {
              resizeEditor();
            }
          }, 100)
        }
      });
    }

    editor.on('afterCommandExec', function(evt) {
      if (evt.data.name == 'maximize' && evt.editor.mode == 'wysiwyg') {
        if (evt.data.command.state == CKEDITOR.TRISTATE_ON) {
          scrollable.removeStyle('overflow-y');
        } else {
          resizeEditor();
        }
      }
    });

    editor.on('contentDom', refreshCache);
    refreshCache();
    if (editor.config.autoGrow_onStartup && editor.editable().isVisible()) {
      editor.execCommand('autogrow');
    }

    function refreshCache() {
      doc = editor.document;
      markerContainer = doc[CKEDITOR.env.ie ? 'getBody' : 'getDocumentElement']();
      scrollable = CKEDITOR.env.quirks ? doc.getBody() : doc.getDocumentElement();
      var body = CKEDITOR.env.quirks ? scrollable : scrollable.findOne('body');
      if (body) {
        body.setStyle('height', 'auto');
        body.setStyle('min-height', CKEDITOR.env.safari ? '0%' : 'auto');
      }
      marker = CKEDITOR.dom.element.createFromHtml(
        `<span style="margin:0;padding:0;border:0;clear:both;width:1px;height:1px;display:block;">
        ${(CKEDITOR.env.webkit ? '&nbsp;' : '')}
        </span>`,
        doc);
    }

    function isNotResizable() {
      var maximizeCommand = editor.getCommand('maximize');
      return (
        !editor.window || maximizeCommand && maximizeCommand.state == CKEDITOR.TRISTATE_ON
      )
    }

    function contentHeight() {
      markerContainer.append(marker);
      var height = 0;
      if (marker && doc) {
        try {
          var o = undefined;
          height = (o = marker.getDocumentPosition(doc)) ? o.y : 0;
          marker.remove();
        } catch (e) { }
        height = height + marker.$.offsetHeight
      }
      return height;
    }

    function resizeEditor() {
      if (maxHeightIsUnlimited) {
        scrollable.setStyle('overflow-y', 'heidden');
      }
      var currentHeight = editor.window.getViewPaneSize().height,
        newHeight = contentHeight()
      
      newHeight += configBottomSpace;
      newHeight = Math.max(newHeight, configMinHeight);
      newHeight = Math.min(newHeight, configMaxHeight);

      if (newHeight != currentHeight && lastHeight != newHeight) {
        newHeight = editor.fire('autoGrow', { currentHeight: currentHeight, newHeight: newHeight }).newHeight;
        editor.resize(null, newHeight, true);
        lastHeight = newHeight;
      }

      if (!maxHeightIsUnlimited) {
        if (newHeight < configMaxHeight && scrollable.$.scrollHeight > scrollable.$.clientHeight) {
          scrollable.setStyle('overflow-y', 'hidden');
        } else {
          scrollable.removeStyle('overflow-y')
        }
      }
    }
  }
})();