package play.modules.logger;

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;
import play.mvc.Http;

public class ExtendedPatternLayout extends org.apache.log4j.PatternLayout {
  public ExtendedPatternLayout() {
  }

  public ExtendedPatternLayout(String pattern) {
    super(pattern);
  }

  @Override
  protected PatternParser createPatternParser(String pattern) {
    return new PatternParser(pattern) {
      protected void finalizeConverter(char c) {
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

  static class HeapSizePatternConverter extends PatternConverter {
    protected String convert(LoggingEvent event) {
      Runtime runtime = Runtime.getRuntime();
      long used = runtime.totalMemory() - runtime.freeMemory();
      return (used / 1024 / 1024 + 1) + "MB";
    }
  }

  static class RequestIdPatternConverter extends PatternConverter {
    protected String convert(LoggingEvent event) {
      Http.Request request = Http.Request.current();
      if (request == null) return "job";
      Object rid = request.args.get("requestId");
      return rid == null ? "unknown" : rid.toString();
    }
  }
}
