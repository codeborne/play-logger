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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;

public class RequestLogPlugin extends PlayPlugin {
  private static final String REQUEST_ID_PREFIX = Integer.toHexString((int)(Math.random() * 0x1000));
  private static final AtomicInteger counter = new AtomicInteger(1);
  private static final Logger logger = LoggerFactory.getLogger("request");

  @SuppressWarnings("NonFinalStaticVariableUsedInClassInitialization")
  private static final String LOG_AS_PATH = Play.configuration.getProperty("request.log.pathForAction", "Web.");

  @Override public void onConfigurationRead() {
    initMaskedParams();
  }

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
    
    String executionTime = "";
    if (request != null && request.args != null) {
      Long start = (Long) request.args.get("startTime");
      if (start != null) executionTime = " " + (currentTimeMillis() - start) + " ms";
    }

    logger.info(path(request) +
        ' ' + request.remoteAddress +
        ' ' + session.getId() +
        getRequestLogCustomData(request) +
        ' ' + extractParams(request) +
        " -> " + result(result) +
        executionTime);
  }

  private static String result(Result result) {
    return (result instanceof Redirect) ?
        result.getClass().getSimpleName() + ' ' + ((Redirect) result).url :
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
    return request.action != null && request.action.startsWith(LOG_AS_PATH) ? request.path : request.action;
  }

  private static final Set<String> SKIPPED_PARAMS = new HashSet<>(asList("authenticityToken", "action", "controller", "x-http-method-override", "body", "action", "controller"));

  private static final Set<String> MASKED_PARAMS = new HashSet<>();

  private static void initMaskedParams() {
    String maskedParamsString = Play.configuration.getProperty("request.log.maskParams", "password|cvv|card.cvv|card.number");
    for (String param : maskedParamsString.split("\\|")) MASKED_PARAMS.add(param.toLowerCase().trim());
  }

  static String extractParams(Http.Request request) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String[]> param : request.params.all().entrySet()) {
      String name = param.getKey();
      if (SKIPPED_PARAMS.contains(name)) continue;
      sb.append('\t').append(name).append('=');
      String value;
      if (mustMask(name)) value = "*";
      else {
        if (param.getValue().length == 1)
          value = param.getValue()[0];
        else
          value = Arrays.toString(param.getValue());
      }
      sb.append(value);
    }
    return sb.toString().trim();
  }

  private static boolean mustMask(String name) {
    for (String maskedParam : MASKED_PARAMS) {
      if (name.toLowerCase().contains(maskedParam)) return true;
    }
    return false;
  }
}
