package play.modules.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.PlayPlugin;
import play.mvc.Http;
import play.mvc.Scope;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class RequestLogPlugin extends PlayPlugin {
  private static String prefix = Integer.toHexString((int)(Math.random() * 0x1000));
  private static AtomicInteger counter = new AtomicInteger(1);
  private static Logger logger = LoggerFactory.getLogger("request");

  static final String EXCLUDE = "(authenticityToken|action|controller|x-http-method-override)=.*?(\t|$)";
  static final String PASSWORD = "password=.*?(\t|$)";

  @Override
  public void beforeActionInvocation(Method actionMethod) {
    Http.Request.current().args.put("startTime", currentTimeMillis());
    Http.Request.current().args.put("requestId", prefix + "-" + counter.incrementAndGet());
  }

  @Override public void afterActionInvocation() {
    Http.Request request = Http.Request.current();
    Scope.Session session = Scope.Session.current();
    long start = (Long)request.args.get("startTime");

    StringBuilder line = new StringBuilder();
    line.append(request.action).append(' ');
    line.append(request.remoteAddress).append(' ');
    line.append(session.getId()).append(' ');
    line.append(extractParams(request));
    line.append(' ').append(currentTimeMillis() - start).append(" ms");

    logger.info(line.toString());
  }

  @Override public void onInvocationException(Throwable e) {
    afterActionInvocation();
  }

  static String extractParams(Http.Request request) {
    try {
      String params = request.querystring;
      if (params.startsWith("?")) params = params.substring(1);
      if ("application/x-www-form-urlencoded".equals(request.contentType))
        params += (isNotEmpty(params) ? "\t" : "") + request.params.get("body");
      return URLDecoder.decode(params.replace("&", "\t").replaceAll(EXCLUDE, "").replaceAll(PASSWORD, "password=*$1"), "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
