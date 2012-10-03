package play.modules.logger;

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;
import play.mvc.Http;

public class ExtendedPatternLayout extends org.apache.log4j.PatternLayout {

  private final static PatternConverter HEAP_SIZE_PATTERN_CONVERTER = new HeapSizePatternConverter();
  private final static PatternConverter REQUEST_ID_PATTERN_CONVERTER = new RequestIdPatternConverter();

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
          addConverter(HEAP_SIZE_PATTERN_CONVERTER);
        }
        else if (c == 'R') {
          addConverter(REQUEST_ID_PATTERN_CONVERTER);
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
      if (request == null) return "";
      Object rid = request.args.get("requestId");
      return rid == null ? "" : rid.toString();
    }
  }
}
