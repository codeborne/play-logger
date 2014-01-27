package play.modules.logger;

import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.data.parsing.UrlEncodedParser;
import play.mvc.Http;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class RequestLogPluginTest {
  @SuppressWarnings("deprecation")
  Http.Request request = new Http.Request();

  @Before
  public void setUp() throws Exception {
    Play.configuration = new Properties();
    Play.configuration.setProperty("request.log.maskParams", "password|cvv|card\\.cvv|card\\.number");
    Http.Request.current.set(request);
  }

  @Test
  public void passwordIsMasked() throws Exception {
    setQueryString("username=anton&password=123&password2=456&newPassword=678&password=789&oldPassword=1693&age=12");
    assertEquals("username=anton\tpassword2=*\tnewPassword=*\tage=12\toldPassword=*\tpassword=*", RequestLogPlugin.extractParams(request));
  }

  @Test
  public void cvvIsMasked() throws Exception {
    setQueryString("card.holderName=Some+Body&card.number=6789690444552800&card.validityMonth=07&card.validityYear=2015&card.cvv=907&cvv=600");
    assertEquals("card.validityYear=2015\tcard.cvv=*\tcvv=*\tcard.number=*\tcard.validityMonth=07\tcard.holderName=Some Body", RequestLogPlugin.extractParams(request));
  }

  @Test
  public void cardNumberIsMasked() throws Exception {
    setQueryString("card.number=6789 6904 4455 2800");
    assertEquals("card.number=*", RequestLogPlugin.extractParams(request));
  }

  @Test
  public void postParametersAreIncluded() throws Exception {
    setQueryString("id=123");
    request.contentType = "application/x-www-form-urlencoded";
    request.body = new ByteArrayInputStream("password=123&x=y".getBytes());
    assertEquals("id=123\tpassword=*\tx=y", RequestLogPlugin.extractParams(request));
  }

  @Test
  public void skipsPlaySpecificParameters() throws Exception {
    setQueryString("authenticityToken=skip&action=skip&controller=skip&abc=value");
    assertEquals("abc=value", RequestLogPlugin.extractParams(request));
  }

  @Test
  public void paramsAreDecoded() throws Exception {
    setQueryString("hello=A+B+%43");
    assertEquals("hello=A B C", RequestLogPlugin.extractParams(request));
  }

  private void setQueryString(String params) throws UnsupportedEncodingException {
    request.params.data.putAll(UrlEncodedParser.parseQueryString(new ByteArrayInputStream(params.getBytes("UTF-8"))));
  }
}

