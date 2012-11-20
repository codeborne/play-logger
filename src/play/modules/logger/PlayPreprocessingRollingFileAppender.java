package play.modules.logger;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;
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
    if ("play".equals(event.getLoggerName()) && event.getThrowableInformation() != null && Http.Request.current() != null) {
      Throwable throwable = event.getThrowableInformation().getThrowable();
      if (throwable instanceof ActionNotFoundException) {
        logRequestInfo(new NotFound(""));
        return; // do not log "not found" in general log
      }
      else if (throwable instanceof JavaExecutionException) {
        logRequestInfo(new Error(""));
      }
    }
    super.append(event);
  }
}
