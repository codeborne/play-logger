package play.modules.logger;

import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.data.parsing.UrlEncodedParser;
import play.mvc.Http;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

public class RequestLogPluginTest {
  @SuppressWarnings("deprecation")
  Http.Request request = new Http.Request();

  @Before
  public void setUp() {
    if (Play.configuration == null) Play.configuration = new Properties();
    Play.configuration.setProperty("request.log.maskParams", "password|cvv|card\\.cvv|card\\.number");
    Http.Request.current.set(request);
  }

  @Test
  public void passwordIsMasked() {
    setQueryString("username=anton&password=123&password2=456&newPassword=678&password=789&oldPassword=1693&age=12");
    String maskedParams = RequestLogPlugin.extractParams(request);

    assertThat(maskedParams, containsString("username=anton"));
    assertThat(maskedParams, containsString("age=12"));
    assertThat(maskedParams, containsString("password2=*"));
    assertThat(maskedParams, containsString("newPassword=*"));
    assertThat(maskedParams, containsString("oldPassword=*"));
    assertThat(maskedParams, containsString("password=*"));
    assertFalse(maskedParams.contains("123"));
    assertFalse(maskedParams.contains("456"));
    assertFalse(maskedParams.contains("67"));
    assertFalse(maskedParams.contains("789"));
    assertFalse(maskedParams.contains("1693"));
  }

  @Test
  public void cvvIsMasked() {
    setQueryString("card.holderName=Some+Body&card.number=6789690444552800&" +
        "card.validityMonth=07&card.validityYear=2015&card.cvv=907&cvv=600");
    String maskedParams = RequestLogPlugin.extractParams(request);

    assertThat(maskedParams, containsString("card.validityYear=2015"));
    assertThat(maskedParams, containsString("card.validityMonth=07"));
    assertThat(maskedParams, containsString("card.holderName=Some Body"));
    assertThat(maskedParams, containsString("card.cvv=*"));
    assertThat(maskedParams, containsString("cvv=*"));
    assertThat(maskedParams, containsString("card.number=*"));
    assertFalse(maskedParams.contains("6789690444552800"));
    assertFalse(maskedParams.contains("907"));
    assertFalse(maskedParams.contains("600"));
  }

  @Test
  public void cardNumberIsMasked() {
    setQueryString("card.number=6789 6904 4455 2800");
    assertEquals("card.number=*", RequestLogPlugin.extractParams(request));
  }

  @Test
  public void postParametersAreIncluded() {
    setQueryString("id=123");
    request.contentType = "application/x-www-form-urlencoded";
    request.body = new ByteArrayInputStream("password=456&x=y".getBytes());
    String maskedParams = RequestLogPlugin.extractParams(request);

    assertThat(maskedParams, containsString("id=123"));
    assertThat(maskedParams, containsString("password=*"));
    assertThat(maskedParams, containsString("x=y"));
    assertFalse(maskedParams.contains("456"));
  }

  @Test
  public void skipsPlaySpecificParameters() {
    setQueryString("authenticityToken=skip&action=skip&controller=skip&abc=value");
    assertEquals("abc=value", RequestLogPlugin.extractParams(request));
  }

  @Test
  public void paramsAreDecoded() {
    setQueryString("hello=A+B+%43");
    assertEquals("hello=A B C", RequestLogPlugin.extractParams(request));
  }

  @Test
  public void customLogData() {
    assertEquals("", RequestLogPlugin.getRequestLogCustomData(request));

    request.args = new HashMap<>();
    request.args.put("requestLogCustomData", "xtra");

    assertEquals(" xtra", RequestLogPlugin.getRequestLogCustomData(request));
  }

  private void setQueryString(String params) {
    try {
      request.params.data.putAll(UrlEncodedParser.parseQueryString(new ByteArrayInputStream(params.getBytes("UTF-8"))));
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}

