package play.modules.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;
import play.PlayPlugin;
import play.mvc.Http;
import play.mvc.Scope;
import play.mvc.results.Result;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static java.lang.System.currentTimeMillis;

public class RequestLogPlugin extends PlayPlugin {
  static final String REQUEST_ID_PREFIX = Integer.toHexString((int)(Math.random() * 0x1000));
  static final AtomicInteger counter = new AtomicInteger(1);
  static final Logger logger = LoggerFactory.getLogger("request");

  static final String LOG_AS_PATH = Play.configuration.getProperty("request.log.pathForAction", "Web.");
  static final Pattern EXCLUDE = Pattern.compile("(authenticityToken|action|controller|x-http-method-override)=.*?(\t|$)");
  static final Pattern MASK = Pattern.compile("(?i)(.*?(?=" + Play.configuration.getProperty("request.log.maskParams", "password|cvv|card\\.number") + ").*?)=.*?(\t|$)");

  @Override public void routeRequest(Http.Request request) {
    request.args.put("startTime", currentTimeMillis());
    request.args.put("requestId", REQUEST_ID_PREFIX + "-" + counter.incrementAndGet());
  }

  @Override public void onActionInvocationResult(Result result) {
    logRequestInfo(result);
  }

  @Override public void onInvocationException(Throwable e) {
    logRequestError(e);
  }

  static void logRequestInfo(Result result) {
    Http.Request request = Http.Request.current();
    Scope.Session session = Scope.Session.current();
    long start = (Long)request.args.get("startTime");

    StringBuilder line = new StringBuilder()
      .append(request.action.startsWith(LOG_AS_PATH) ? request.path : request.action).append(' ')
      .append(request.remoteAddress).append(' ')
      .append(session.getId())
      .append(' ').append(extractParams(request))
      .append(" -> ").append(result.getClass().getSimpleName())
      .append(' ').append(currentTimeMillis() - start).append(" ms");

    logger.info(line.toString());
  }

  static void logRequestError(Throwable e) {
    Http.Request request = Http.Request.current();
    Scope.Session session = Scope.Session.current();
    long start = (Long)request.args.get("startTime");

    StringBuilder line = new StringBuilder()
      .append(request.action.startsWith(LOG_AS_PATH) ? request.path : request.action).append(' ')
      .append(request.remoteAddress).append(' ')
      .append(session.getId())
      .append(' ').append(extractParams(request))
      .append(" -> ").append(e)
      .append(' ').append(currentTimeMillis() - start).append(" ms");

    logger.info(line.toString());
  }

  static String extractParams(Http.Request request) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String[]> param : request.params.all().entrySet()) {
      if (param.getKey().equals("body") || param.getKey().equals("action") || param.getKey().equals("controller")) continue;
      sb.append('\t').append(param.getKey()).append('=');
      if (param.getValue().length == 1) sb.append(param.getValue()[0]);
      else sb.append(Arrays.toString(param.getValue()));
    }

    String params = EXCLUDE.matcher(sb).replaceAll("");
    params = MASK.matcher(params).replaceAll("$1=*$2");
    return params.trim();
  }
}
