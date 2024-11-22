/**
 * @File        : SimpleTest.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 간단한 테스트 케이스들
 * @Site        : https://devlog.ntiple.com
 * 
 * sh gradlew cleanTest test -Dproject.build.test=MANUAL -Dspring.profiles.active=local -i --no-watch-fs --tests "com.ntiple.SimpleTest.testSimple"
 **/
package com.ntiple;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.CryptoUtil.RSA.decrypt;
import static com.ntiple.commons.CryptoUtil.RSA.encrypt;
import static com.ntiple.commons.HttpUtil.httpWorker;
import static com.ntiple.commons.IOUtils.file;
import static com.ntiple.commons.IOUtils.istream;
import static com.ntiple.commons.IOUtils.readAsString;
import static com.ntiple.commons.IOUtils.reader;
import static com.ntiple.commons.IOUtils.safeclose;
import static com.ntiple.commons.IOUtils.writer;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.security.Key;
import java.util.Base64;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.junit.jupiter.api.Test;

import com.ntiple.TestUtil.TestLevel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleTest {

  @Test public void testSimple() throws Exception {
    log.info("OK");
    assertTrue(true);
  }

  @Test public void testPattern() throws Exception {
    if (!TestUtil.isEnabled("testPattern", TestLevel.MANUAL)) { return; }
    {
      Pattern PTN_ST_SCRIPT = Pattern.compile("^[<]script[^>]*>");
      Pattern PTN_ED_SCRIPT = Pattern.compile("[<]/script[^>]*>$");
      String str = "<script> TEST </script>";
      str = str.trim();
      str = PTN_ST_SCRIPT.matcher(str).replaceAll("");
      str = PTN_ED_SCRIPT.matcher(str).replaceAll("");
      log.debug("TEST:{}", str);
    }
    {
      Pattern PTN_NL = Pattern.compile("[\\r\\n][\\\\]", Pattern.MULTILINE);
      String str = "function $component_input(app){const CInput={template:`<input\r" +
        "\\ class=\"form-control\"\r" +
        "\\:vrules=\"\r" +
        "\\ data-test=\"OK\"\r" +
        "\\/>`,name:\"c-input\",props:{},data:function(){return{};},mounted:async function(){}};app.component(\"c-input\",CInput);};\r";
      log.debug("STR:{}", str);
      log.debug("--------------------------------------------------------------------------------");
      log.debug("STR:{}", PTN_NL.matcher(str).replaceAll(""));
    }
    assertTrue(true);
  }

  @Test
  public void testMinifyJS() throws Exception {
    if (!TestUtil.isEnabled("testMinifyJS", TestLevel.MANUAL)) { return; }
    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    ScriptContext context = new SimpleScriptContext();
    context.getBindings(ScriptContext.GLOBAL_SCOPE);
    context.setBindings(engine.createBindings(), ScriptContext.GLOBAL_SCOPE);
    Bindings bindings = context.getBindings(ScriptContext.GLOBAL_SCOPE); 
    bindings.put("console", System.console());
    engine.setContext(context);
    BufferedReader reader = null;
    Writer writer = null;
    File scrFile = null;
    File srcFile = null;
    try {
      Invocable invocable = (Invocable) engine;
      scrFile = TestUtil.getResource(Application.class, "/scripts/uglify.min.js");
      reader = reader(istream(scrFile), UTF8);
      engine.eval(reader);
      safeclose(reader);
      scrFile = TestUtil.getResource(Application.class, "/scripts/do-minify.js");
      reader = reader(istream(scrFile), UTF8);
      engine.eval(reader);
      safeclose(reader);
      srcFile = file("/home/coder/documents/tmp/test.js");
      String content = readAsString(srcFile);
      content = content.replaceAll("`", "｀");
      Object obj = invocable.invokeFunction("minifyCode", content);
      if (obj != null) {
        content = String.valueOf(obj).replaceAll("｀", "`");
      }
      log.debug("RESULT:{}", content);
    } finally {
      safeclose(reader);
      safeclose(writer);
    }
  }

  @Test
  public void testMinifier() throws Exception {
    if (!TestUtil.isEnabled("testMinifier", TestLevel.MANUAL)) { return; }
    net.logicsquad.minifier.js.JSMinifier min = null;
    Reader input = null;
    Writer output = null;
    try {
      input = reader(file("/home/coder/documents/tmp/minify-7196260321170045552.js"), UTF8);
      output = writer(file("/home/coder/documents/tmp/test.js"), UTF8);
      min = new net.logicsquad.minifier.js.JSMinifier(input);
      min.minify(output);
    } finally {
      safeclose(input);
      safeclose(output);
    }
  }
  
  @Test
  public void testRSA() throws Exception {
    if (!TestUtil.isEnabled("testRSA", TestLevel.MANUAL)) { return; }
    String prvk = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIklL0trECmxNh3gBScQAGa5hqILaLXyxFg4VAh6MktDTdZbbvpHeMq4owGF0i1RWgTsudOjcT1Su6Jp+2+bQGEGdjow37qOHPuErQXtDXWxAJmnn8RBTpO/w96DZatzg9fU2Ib0fSGDlXTHaRbvSInCpbEwM5h7efnuKjA11VBXAgMBAAECgYAXeaf4zuC7YjwTLQ90ukZ3TvZ+sllAG8gEGdA4i0Iko+ak9I2whZ9lg+lTD2cEntI72ZGNaoKtroWzrVR+rCJ+uLbSVB8n0JAkrtd1eg/dbxIQNFkaFGpwkC0AtQSpgsLly7HjVQ5MrIAlP63ZiK9JdTBdXyajsLJX+R7Dyll6MQJBAKmPt/ZY0rKj21KirA8T4afW2qMVpMyIRTvbvaW7BU69pxyBwJEY3okwNCE4SK94GebVaXR2B7vANCI64NizBw0CQQDPDw9IgT9nLlZk+PRfYcm729qHotm6Uc8GrY0Iz8nkxrmszIz+/XBTsjV4na1gJ6dNMJHdg7gbR9ZsEm7I9FvzAkBsk7krKGmTNtXErqIa7ZI8FZrff4aN6lzbHbTtITse1tbhrDyRLSmjE5juBMqWggOkCtiCWOpO0Z8QpD9CxDEpAkABYXNTo3D9yiRPVg2jGS7ULtodL2vOPz9nJv8awO/ys5SHX3HNPXljRXvvyvVd/8Ww0RMX7AntPKRkYhcVBfQbAkAfups6liYVJLHON6vcVQTh0G9EaSZWDyFdxn+QVNA1BTgyqyA76VUywkiDviDbjWK1gv3UtiF19aQBlDFsvzcq";
    String pubk = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCJJS9LaxApsTYd4AUnEABmuYaiC2i18sRYOFQIejJLQ03WW276R3jKuKMBhdItUVoE7LnTo3E9Uruiaftvm0BhBnY6MN+6jhz7hK0F7Q11sQCZp5/EQU6Tv8Peg2Wrc4PX1NiG9H0hg5V0x2kW70iJwqWxMDOYe3n57iowNdVQVwIDAQAB";
    Key key = null;

    String msg = "";
    String enc = "";
    String dec = "";
    {
      prvk = "MIICWwIBAAKBgQCA78d1ktnZ50Gf3T1Tk9tjrUkMHg/gEBoasS+R4AOwZMfu0WaDS4IIJVPfeBzFJbwpkNS3B0FA080ttWQGyPY4giHryJiKFHz0RdtCew32dIc/udOwlBJD2xoQGwzPPChLCparLkAV8OlcHZva/kgGdKABwum8la0zOWXUnPAU1QIDAQABAoGAFwKz5he/KVRMMeuZ9kB89t0GHFOBIcu93OWiR7Zi8igKRmS4ltXy7uE6hrc46zZAzmo6jC+PRbKG+5FTuKJEzq9/D58TJY3s1Ftf5qnStyLv+hzLpcfKF6KPnoLa59ZQoShHktVTKsk9wR3c3ul+2RMLJymHnlEY5Z7+ZTnJBEECQQDPfGGMfIEzIPkCdXmfvvmgz8ESyMFeqBQPK9cWQ5aFO38R1Ke6p+S5a15+p2E055Q0d56+xT0OwoxVKiF5iMQjAkEAnxWf/QMRnKq6S7WWNttmHXRR5Zp2UAh99zJ9QSpYNNBEP07nJx5Rg6ZjRr9QrFvWXi7ROnN5CFWq69mMyXB2pwJAar1PJcnLYbU9xSEQP7ksjKk0Z2h16i9HmoJwNVjx73qrJU4kN6c1yJnO1BNhs6jLGq7LMNMhVR2KuilhbTeJxwJAOJyfdJBVAiWXaj3SmO72peCxDD4tgEmlWgSzoi8JeLHst4LCq58Ubv8VMSX/9XYxEQ8kEeLp3VdvHcMrYLwO3QJARQX/u+YY/fOVm7vLpAEv0De8wl9gltG0/Erf3zYdqTrDHTX+3cwSwIY6JwGR6tsvi0hYCAy/uAVXSUsYptLHrA==";
      pubk = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCA78d1ktnZ50Gf3T1Tk9tjrUkMHg/gEBoasS+R4AOwZMfu0WaDS4IIJVPfeBzFJbwpkNS3B0FA080ttWQGyPY4giHryJiKFHz0RdtCew32dIc/udOwlBJD2xoQGwzPPChLCparLkAV8OlcHZva/kgGdKABwum8la0zOWXUnPAU1QIDAQAB";
      AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);
      PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(algId, ASN1Sequence.getInstance(Base64.getDecoder().decode(prvk)));
      byte[] pkcs8Encoded = privateKeyInfo.getEncoded();
      java.security.spec.KeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(pkcs8Encoded);
      key = java.security.KeyFactory.getInstance("RSA").generatePrivate(spec);
      // Key key = java.security.KeyFactory.getInstance("RSA", new BouncyCastleProvider()).generatePrivate(spec);
      msg = "테스트";
      enc = encrypt(key, msg);
      // enc = encrypt(0, prvk, msg);
      log.debug("ENCRYPT-PRIV:{} -> {}", msg, enc);
    }
    {
      prvk = "MIICWwIBAAKBgQCA78d1ktnZ50Gf3T1Tk9tjrUkMHg/gEBoasS+R4AOwZMfu0WaDS4IIJVPfeBzFJbwpkNS3B0FA080ttWQGyPY4giHryJiKFHz0RdtCew32dIc/udOwlBJD2xoQGwzPPChLCparLkAV8OlcHZva/kgGdKABwum8la0zOWXUnPAU1QIDAQABAoGAFwKz5he/KVRMMeuZ9kB89t0GHFOBIcu93OWiR7Zi8igKRmS4ltXy7uE6hrc46zZAzmo6jC+PRbKG+5FTuKJEzq9/D58TJY3s1Ftf5qnStyLv+hzLpcfKF6KPnoLa59ZQoShHktVTKsk9wR3c3ul+2RMLJymHnlEY5Z7+ZTnJBEECQQDPfGGMfIEzIPkCdXmfvvmgz8ESyMFeqBQPK9cWQ5aFO38R1Ke6p+S5a15+p2E055Q0d56+xT0OwoxVKiF5iMQjAkEAnxWf/QMRnKq6S7WWNttmHXRR5Zp2UAh99zJ9QSpYNNBEP07nJx5Rg6ZjRr9QrFvWXi7ROnN5CFWq69mMyXB2pwJAar1PJcnLYbU9xSEQP7ksjKk0Z2h16i9HmoJwNVjx73qrJU4kN6c1yJnO1BNhs6jLGq7LMNMhVR2KuilhbTeJxwJAOJyfdJBVAiWXaj3SmO72peCxDD4tgEmlWgSzoi8JeLHst4LCq58Ubv8VMSX/9XYxEQ8kEeLp3VdvHcMrYLwO3QJARQX/u+YY/fOVm7vLpAEv0De8wl9gltG0/Erf3zYdqTrDHTX+3cwSwIY6JwGR6tsvi0hYCAy/uAVXSUsYptLHrA==";
      pubk = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCA78d1ktnZ50Gf3T1Tk9tjrUkMHg/gEBoasS+R4AOwZMfu0WaDS4IIJVPfeBzFJbwpkNS3B0FA080ttWQGyPY4giHryJiKFHz0RdtCew32dIc/udOwlBJD2xoQGwzPPChLCparLkAV8OlcHZva/kgGdKABwum8la0zOWXUnPAU1QIDAQAB";
      msg = "테스트";
      enc = encrypt(1, pubk, msg);
      log.debug("ENCRYPT-PUB:{} -> {}", msg, enc);
    }
    {
      prvk = "MIICWwIBAAKBgQCA78d1ktnZ50Gf3T1Tk9tjrUkMHg/gEBoasS+R4AOwZMfu0WaDS4IIJVPfeBzFJbwpkNS3B0FA080ttWQGyPY4giHryJiKFHz0RdtCew32dIc/udOwlBJD2xoQGwzPPChLCparLkAV8OlcHZva/kgGdKABwum8la0zOWXUnPAU1QIDAQABAoGAFwKz5he/KVRMMeuZ9kB89t0GHFOBIcu93OWiR7Zi8igKRmS4ltXy7uE6hrc46zZAzmo6jC+PRbKG+5FTuKJEzq9/D58TJY3s1Ftf5qnStyLv+hzLpcfKF6KPnoLa59ZQoShHktVTKsk9wR3c3ul+2RMLJymHnlEY5Z7+ZTnJBEECQQDPfGGMfIEzIPkCdXmfvvmgz8ESyMFeqBQPK9cWQ5aFO38R1Ke6p+S5a15+p2E055Q0d56+xT0OwoxVKiF5iMQjAkEAnxWf/QMRnKq6S7WWNttmHXRR5Zp2UAh99zJ9QSpYNNBEP07nJx5Rg6ZjRr9QrFvWXi7ROnN5CFWq69mMyXB2pwJAar1PJcnLYbU9xSEQP7ksjKk0Z2h16i9HmoJwNVjx73qrJU4kN6c1yJnO1BNhs6jLGq7LMNMhVR2KuilhbTeJxwJAOJyfdJBVAiWXaj3SmO72peCxDD4tgEmlWgSzoi8JeLHst4LCq58Ubv8VMSX/9XYxEQ8kEeLp3VdvHcMrYLwO3QJARQX/u+YY/fOVm7vLpAEv0De8wl9gltG0/Erf3zYdqTrDHTX+3cwSwIY6JwGR6tsvi0hYCAy/uAVXSUsYptLHrA==";
      pubk = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCA78d1ktnZ50Gf3T1Tk9tjrUkMHg/gEBoasS+R4AOwZMfu0WaDS4IIJVPfeBzFJbwpkNS3B0FA080ttWQGyPY4giHryJiKFHz0RdtCew32dIc/udOwlBJD2xoQGwzPPChLCparLkAV8OlcHZva/kgGdKABwum8la0zOWXUnPAU1QIDAQAB";
      // enc = "XVzxrII2fUqDO81Ybo/3D6DujEAs/nMrk6/Sz2FzZRNueXU+Xa1iJr/1XxPbcjMV9m904rbdEvokatNFoI0au3DGLe3EdN4dv73SURmUEWz/6J0EfrC1NemyLrWEqDjpVxtcshd/Er8uInKftmVklgYgeL9VU8mXCdOjcsou/Dg=";
      // enc = "TUh7lunBC5IwMBcN51jNg7oanOz3OZvx9EBuEgE7tGyIEwmDRyVEt4BJDU8TuCxZn3sFC7n0GqfqXXvlXg0wRlbf+22Vc8f1P0AId6OguZafjCeFMeJR1QKCUtm5jAhs4RCR+lkNhqvgExrf51OP9SIYMVZDctMr+hk8XZbW/bQ=";
      enc = "QJWGqeD5Bxnyijbf6JzTyYN9oJES3MYugJFJXzGid13XFCxId1A70/vdnsb8I0xewBjp4m9sMbXPzRf4hXXppiTrwF87iaoZsKbPlhebh21j5roxeXO+hklb1tkOppXYHDRogFJogCYqzqXp8pV1tVK1YVuA1wmk48IXd83c7dw=";
      dec = decrypt(1, pubk, enc);
      log.debug("DECRYPT-PUB:{} -> {}", enc, dec);
    }
    {
      prvk = "MIICWwIBAAKBgQCA78d1ktnZ50Gf3T1Tk9tjrUkMHg/gEBoasS+R4AOwZMfu0WaDS4IIJVPfeBzFJbwpkNS3B0FA080ttWQGyPY4giHryJiKFHz0RdtCew32dIc/udOwlBJD2xoQGwzPPChLCparLkAV8OlcHZva/kgGdKABwum8la0zOWXUnPAU1QIDAQABAoGAFwKz5he/KVRMMeuZ9kB89t0GHFOBIcu93OWiR7Zi8igKRmS4ltXy7uE6hrc46zZAzmo6jC+PRbKG+5FTuKJEzq9/D58TJY3s1Ftf5qnStyLv+hzLpcfKF6KPnoLa59ZQoShHktVTKsk9wR3c3ul+2RMLJymHnlEY5Z7+ZTnJBEECQQDPfGGMfIEzIPkCdXmfvvmgz8ESyMFeqBQPK9cWQ5aFO38R1Ke6p+S5a15+p2E055Q0d56+xT0OwoxVKiF5iMQjAkEAnxWf/QMRnKq6S7WWNttmHXRR5Zp2UAh99zJ9QSpYNNBEP07nJx5Rg6ZjRr9QrFvWXi7ROnN5CFWq69mMyXB2pwJAar1PJcnLYbU9xSEQP7ksjKk0Z2h16i9HmoJwNVjx73qrJU4kN6c1yJnO1BNhs6jLGq7LMNMhVR2KuilhbTeJxwJAOJyfdJBVAiWXaj3SmO72peCxDD4tgEmlWgSzoi8JeLHst4LCq58Ubv8VMSX/9XYxEQ8kEeLp3VdvHcMrYLwO3QJARQX/u+YY/fOVm7vLpAEv0De8wl9gltG0/Erf3zYdqTrDHTX+3cwSwIY6JwGR6tsvi0hYCAy/uAVXSUsYptLHrA==";
      pubk = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCA78d1ktnZ50Gf3T1Tk9tjrUkMHg/gEBoasS+R4AOwZMfu0WaDS4IIJVPfeBzFJbwpkNS3B0FA080ttWQGyPY4giHryJiKFHz0RdtCew32dIc/udOwlBJD2xoQGwzPPChLCparLkAV8OlcHZva/kgGdKABwum8la0zOWXUnPAU1QIDAQAB";
      enc = "QLrgPF33wbbPp0MBwD7Uiyb0dcXqAAWt+9UUGlLf4o5/hey7EEyPBovkJp/6/m7WvaHX3z4USRaRqk9aPerBs6/MwqjFr6xkqj8Z4LcW/1tVMn9xM7vK4mpNf3O1n2o5+992oT8R/eLwvjSzjY1k7k/KpjrDrrUtVuyNb6mzb2U=";
      AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);
      PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(algId, ASN1Sequence.getInstance(Base64.getDecoder().decode(prvk)));
      byte[] pkcs8Encoded = privateKeyInfo.getEncoded();
      java.security.spec.KeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(pkcs8Encoded);
      key = java.security.KeyFactory.getInstance("RSA").generatePrivate(spec);
      dec = decrypt(key, enc);
      log.debug("DECRYPT-PUB:{} -> {}", enc, dec);
    }
  }

  @Test
  public void testHttpClient() throws Exception {
    if (!TestUtil.isEnabled("testHttpClient", TestLevel.MANUAL)) { return; }
    StringBuilder sb = new StringBuilder();
    httpWorker()
      .url("https://gitlab.ntiple.com")
      .method(p -> p.GET())
      .agent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0")
      .work((stat, stream, hdr, ctx) -> {
        try {
          sb.append(readAsString(stream));
        } catch (Exception ignore) { }
        return null;
      });
      log.debug("RESULT:{}", sb);
  }
}