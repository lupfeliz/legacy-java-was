<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/common/init.jsp" %>
메인페이지
[<ex:hello message="ABCD"/>]
[<ex:hello>
AAAA
</ex:hello>]
[<ex:hello />]
<div>{{ message }}</div>
<button class="btn btn-primary">
  OK
</button>
<div>
<div id="editor"></div>
</div>
<script>
setTimeout(function() {
  var editor = CKEDITOR.replace($(`#editor`)[0], {
    width: "auto",
    on: { pluginsLoaded: function(e) { } },
    toolbar : [
      ["Undo", "Redo", "Format", "Font", "FontSize", "Bold", "Italic", "Underline",
      "TextColor", "BGColor", "RemoveFormat", "BulletedList", "NumberedList",
      "Copy", "Cut", "Indent", "Outdent", "Link", "Table", "Source"],
    ],
    extraPlugins: "autogrow, colorbutton, font",
    height: 160,
    autoGrow_minHeight: 160,
    autoGrow_maxHeight: 400,
    // autoGrow_bottomSpace: 10,
    autoGrow_onStartup: true,
    // contentsCss: "/assets/styles/editor.css",
    extraAllowedContent: "span{*}[*],img{*}[*]",
    /** resize 가 있으면 autogrow 기능을 사용 못함 */
    removePlugins: "resize, elementspath",
    sourceAreaTabSize: 2,
    // font_names: ectx?.fontNames || "",
  });
}, 1000);
</script>