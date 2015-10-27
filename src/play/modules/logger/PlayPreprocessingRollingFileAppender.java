package play.modules.logger;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import play.exceptions.ActionNotFoundException;
import play.exceptions.JavaExecutionException;
import play.mvc.Http;
import play.mvc.results.Error;
import play.mvc.results.NotFound;

import static play.modules.logger.RequestLogPlugin.logRequestInfo;

/**
 * Special file appender for general log, which redirects play NotFound and Error messages to request log.
 */
public class PlayPreprocessingRollingFileAppender extends DailyRollingFileAppender {
  @Override public void append(LoggingEvent event) {
    ThrowableInformation ti = event.getThrowableInformation();
    if (ti != null) {
      Throwable throwable = ti.getThrowable();

      ExceptionsMonitoringPlugin.register(event.getLoggerName(), throwable);

      if ("play".equals(event.getLoggerName()) && Http.Request.current() != null)
        if (appendToRequestLog(throwable))
          return;
    }
    super.append(event);
  }

  private boolean appendToRequestLog(Throwable throwable) {
    if (throwable instanceof ActionNotFoundException) {
      logRequestInfo(new NotFound("")); // do not log "not found" in general log
      return true;
    }
    else if (throwable instanceof JavaExecutionException) {
      logRequestInfo(new Error(""));
    }
    return false;
  }
}
