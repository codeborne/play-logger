package play.modules.logger;

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.spi.LoggingEvent;
import play.mvc.Http;

class RequestIdPatternConverter extends PatternConverter {

  @Override protected String convert(LoggingEvent event) {
    String threadName = Thread.currentThread().getName();
    Http.Request request = Http.Request.current();
    if (request == null) return threadName;
    Object rid = request.args.get("requestId");
    return rid == null ? threadName : rid.toString();
  }
}
