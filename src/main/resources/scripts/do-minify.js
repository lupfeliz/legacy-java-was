/**
  Minify javascript code using UglifyJS2
  @see https://github.com/mishoo/UglifyJS2
  @param {String} code - The javascript code to minify
*/
var BufferedReader=Java.type("java.io.BufferedReader");
var InputStreamReader=Java.type("java.io.InputStreamReader");
var FileInputStream=Java.type("java.io.FileInputStream");
var FileWriter=Java.type("java.io.FileWriter");
var StringBuilder=Java.type("java.lang.StringBuilder");
var System=Java.type("java.lang.System");
var out = System.out;

function minify (sourcePath, targetPath, charset) {
  var reader = null;
  var writer = null;
  try {
    var code = new StringBuilder();

    reader =  new BufferedReader(new InputStreamReader(new FileInputStream(sourcePath), charset));
    for (var rn; (rn=reader.readLine()) != null; ) {
      code.append(rn).append("\r\n");
    }
    var ast = UglifyJS.parse(code.toString());
    code.setLength(0);
    ast.figure_out_scope();
    compressor = UglifyJS.Compressor();
    ast = ast.transform(compressor);
    writer = new FileWriter(targetPath);
    writer.append(ast.print_to_string());
  } catch(e) {
    out.print(e.stack)
    out.print(e.lineNumber)
    out.print(e.columnNumber)
    out.print(e.fileName)
  } finally {
    try { reader.close(); } catch (ignore) { }
    try { writer.close(); } catch (ignore) { }
  }
}

function minifyCode (code) {
  try {
    var ast = UglifyJS.parse(code.toString());
    ast.figure_out_scope();
    compressor = UglifyJS.Compressor();
    ast = ast.transform(compressor);
    code = ast.print_to_string();
    return code;
  } catch (e) {
    out.print(e.stack)
    out.print(e.lineNumber)
    out.print(e.columnNumber)
    out.print(e.fileName)
  }
}
