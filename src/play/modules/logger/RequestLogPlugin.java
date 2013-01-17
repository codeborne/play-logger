package play.modules.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.PlayPlugin;
import play.mvc.Http;
import play.mvc.Scope;
import play.mvc.results.Result;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class RequestLogPlugin extends PlayPlugin {
  private static String prefix = Integer.toHexString((int)(Math.random() * 0x1000));
  private static AtomicInteger counter = new AtomicInteger(1);
  private static Logger logger = LoggerFactory.getLogger("request");

  static final String EXCLUDE = "(authenticityToken|action|controller|x-http-method-override)=.*?(\t|$)";
  static final String PASSWORD = "(?i)(.*?password.*?)=.*?(\t|$)";

  @Override public void routeRequest(Http.Request request) {
    request.args.put("startTime", currentTimeMillis());
    request.args.put("requestId", prefix + "-" + counter.incrementAndGet());
  }

  @Override public void onActionInvocationResult(Result result) {
    logRequestInfo(result);
  }

  static void logRequestInfo(Result result) {
    Http.Request request = Http.Request.current();
    Scope.Session session = Scope.Session.current();
    long start = (Long)request.args.get("startTime");

    StringBuilder line = new StringBuilder()
      .append(request.action).append(' ')
      .append(request.remoteAddress).append(' ')
      .append(session.getId()).append(' ')
      .append(extractParams(request))
      .append(" -> ").append(result.getClass().getSimpleName())
      .append(' ').append(currentTimeMillis() - start).append(" ms");

    logger.info(line.toString());
  }

  static String extractParams(Http.Request request) {
    try {
      String params = request.querystring;
      if (params.startsWith("?")) params = params.substring(1);
      if ("application/x-www-form-urlencoded".equals(request.contentType))
        params += (isNotEmpty(params) ? "\t" : "") + request.params.get("body");
      return URLDecoder.decode(params.replace("&", "\t").replaceAll(EXCLUDE, "").replaceAll(PASSWORD, "$1=*$2"), "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
