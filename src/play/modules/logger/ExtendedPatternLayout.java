package play.modules.logger;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;
import play.mvc.Http;

public class ExtendedPatternLayout extends PatternLayout {
  public ExtendedPatternLayout() {
  }

  public ExtendedPatternLayout(String pattern) {
    super(pattern);
  }

  @Override
  protected PatternParser createPatternParser(String pattern) {
    return new PatternParser(pattern) {
      @Override protected void finalizeConverter(char c) {
        if (c == 'h') {
          addConverter(new HeapSizePatternConverter());
        }
        else if (c == 'R') {
          addConverter(new RequestIdPatternConverter());
        }
        else {
          super.finalizeConverter(c);
        }
      }
    };
  }

  private static class HeapSizePatternConverter extends PatternConverter {
    @Override protected String convert(LoggingEvent event) {
      Runtime runtime = Runtime.getRuntime();
      long used = runtime.totalMemory() - runtime.freeMemory();
      return (used / 1024 / 1024 + 1) + "MB";
    }
  }

  private static class RequestIdPatternConverter extends PatternConverter {
    @Override protected String convert(LoggingEvent event) {
      String threadName = Thread.currentThread().getName();
      Http.Request request = Http.Request.current();
      if (request == null) return threadName;
      Object rid = request.args.get("requestId");
      return rid == null ? threadName : rid.toString();
    }
  }
}
