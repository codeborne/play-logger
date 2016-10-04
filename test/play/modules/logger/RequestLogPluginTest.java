package play.modules.logger;

import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.data.parsing.UrlEncodedParser;
import play.mvc.Http;
import play.mvc.results.Redirect;
import play.mvc.results.RenderTemplate;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestLogPluginTest {
  @SuppressWarnings("deprecation")
  Http.Request request = new Http.Request();

  @Before
  public void setUp() {
    if (Play.configuration == null) Play.configuration = new Properties();
    Play.configuration.setProperty("request.log.maskParams", "password|cvv|card.cvv|card.number");
    new RequestLogPlugin().onConfigurationRead();
    Http.Request.current.set(request);
  }

  @Test
  public void passwordIsMasked() {
    setQueryString("username=anton&password=123&password2=456&newPassword=678&password=789&oldPassword=1693&age=12");
    String maskedParams = RequestLogPlugin.extractParams(request);

    assertTrue(maskedParams.contains("username=anton"));
    assertTrue(maskedParams.contains("age=12"));
    assertTrue(maskedParams.contains("password2=*"));
    assertTrue(maskedParams.contains("newPassword=*"));
    assertTrue(maskedParams.contains("oldPassword=*"));
    assertTrue(maskedParams.contains("password=*"));
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

    assertTrue(maskedParams.contains("card.validityYear=2015"));
    assertTrue(maskedParams.contains("card.validityMonth=07"));
    assertTrue(maskedParams.contains("card.holderName=Some Body"));
    assertTrue(maskedParams.contains("card.cvv=*"));
    assertTrue(maskedParams.contains("cvv=*"));
    assertTrue(maskedParams.contains("card.number=*"));
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

    assertTrue(maskedParams.contains("id=123"));
    assertTrue(maskedParams.contains("password=*"));
    assertTrue(maskedParams.contains("x=y"));
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

  @Test
  public void setsCurrentThreadName_by_actionName() {
    request.action = "Payments.history";
    Thread.currentThread().setName("play-thread-666");

    new RequestLogPlugin().beforeActionInvocation(null);
    assertEquals("play-thread-666 Payments.history", Thread.currentThread().getName());
  }

  @Test
  public void ignoresPreviouslySetThreadName_if_itWasNotResetForWhateverReason() {
    request.action = "Payments.history";
    Thread.currentThread().setName("play-thread-1 Bank.statement");

    new RequestLogPlugin().beforeActionInvocation(null);
    assertEquals("play-thread-1 Payments.history", Thread.currentThread().getName());
  }

  @Test
  public void resetsCurrentThreadName_after_actionInvocation() {
    Thread.currentThread().setName("play-thread-007 Payments.history");
    new RequestLogPlugin().afterActionInvocation();
    assertEquals("play-thread-007", Thread.currentThread().getName());
  }

  @Test
  public void noResultMeansRenderingError() throws Exception {
    assertEquals("RenderError", RequestLogPlugin.result(null));
  }

  @Test
  public void logsRedirectUrl() throws Exception {
    Redirect result = new Redirect("/foo");
    assertEquals("Redirect /foo", RequestLogPlugin.result(result));
  }

  @Test
  public void logsTemplateRenderingTime() throws Exception {
    RenderTemplate result = mock(RenderTemplate.class);
    when(result.getRenderTime()).thenReturn(101L);
    assertEquals("RenderTemplate 101 ms", RequestLogPlugin.result(result));
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

