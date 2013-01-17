package play.modules.logger;

import org.junit.Test;
import play.mvc.Http;

import static org.junit.Assert.assertEquals;

public class RequestLogPluginTest {
  @SuppressWarnings("deprecation")
  Http.Request request = new Http.Request();

  @Test
  public void passwordIsMasked() throws Exception {
    request.querystring = "?username=anton&password=123&password2=456&newPassword=678&password=789&oldPassword=1693&age=12";
    assertEquals("username=anton\tpassword=*\tpassword2=*\tnewPassword=*\tpassword=*\toldPassword=*\tage=12", RequestLogPlugin.extractParams(request));
  }

  @Test
  public void postParametersAreIncluded() throws Exception {
    request.querystring = "?id=123";
    request.contentType = "application/x-www-form-urlencoded";
    request.params.data.put("body", new String[]{"password=123&x=y"});
    assertEquals("id=123\tpassword=*\tx=y", RequestLogPlugin.extractParams(request));
  }

  @Test
  public void skipsPlaySpecificParameters() throws Exception {
    request.querystring = "?authenticityToken=skip&action=skip&controller=skip&abc=value";
    assertEquals("abc=value", RequestLogPlugin.extractParams(request));
  }

  @Test
  public void paramsAreDecoded() throws Exception {
    request.querystring = "?hello=A+B+%43";
    assertEquals("hello=A B C", RequestLogPlugin.extractParams(request));
  }
}

