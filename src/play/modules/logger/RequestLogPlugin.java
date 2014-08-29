package play.modules.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;
import play.PlayPlugin;
import play.mvc.Http;
import play.mvc.Scope;
import play.mvc.results.Redirect;
import play.mvc.results.Result;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static java.lang.System.currentTimeMillis;

public class RequestLogPlugin extends PlayPlugin {
  private static final String REQUEST_ID_PREFIX = Integer.toHexString((int)(Math.random() * 0x1000));
  private static final AtomicInteger counter = new AtomicInteger(1);
  private static final Logger logger = LoggerFactory.getLogger("request");

  private static final String LOG_AS_PATH = Play.configuration.getProperty("request.log.pathForAction", "Web.");
  private static final Pattern EXCLUDE = Pattern.compile("(authenticityToken|action|controller|x-http-method-override)=.*?(\t|$)");
  private static final Pattern MASK = Pattern.compile("(?i)(.*?(?=" + Play.configuration.getProperty("request.log.maskParams", "password|cvv|card\\.cvv|card\\.number") + ").*?)=.*?(\t|$)");

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

    logger.info(path(request) +
        ' ' + request.remoteAddress +
        ' ' + session.getId() +
        getRequestLogCustomData(request) +
        ' ' + extractParams(request) +
        " -> " + result(result) +
        ' ' + (currentTimeMillis() - start) + " ms");
  }

  private static String result(Result result) {
    return (result instanceof Redirect) ?
        result.getClass().getSimpleName() + ((Redirect) result).url :
        result.getClass().getSimpleName();
  }

  private static void logRequestError(Throwable e) {
    Http.Request request = Http.Request.current();
    Scope.Session session = Scope.Session.current();
    long start = (Long)request.args.get("startTime");

    logger.info(path(request) +
        ' ' + request.remoteAddress +
        ' ' + session.getId() +
        getRequestLogCustomData(request) +
        ' ' + extractParams(request) +
        " -> " + e + ' ' + (currentTimeMillis() - start) + " ms");
  }

  static String getRequestLogCustomData(Http.Request request) {
    return request.args.containsKey("requestLogCustomData") ? " " + request.args.get("requestLogCustomData") : "";
  }

  private static String path(Http.Request request) {
    return (request.action.startsWith(LOG_AS_PATH) ? request.path : request.action);
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
