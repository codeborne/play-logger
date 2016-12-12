package play.modules.logger;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import static play.modules.logger.Utils.maskCardNumber;

public class CardNumberFilteringLayout extends ExtendedPatternLayout {

  @Override
  public String format(LoggingEvent event) {
    if (event.getMessage() instanceof String) {
      String message = event.getRenderedMessage();
      String maskedMessage = maskCardNumber(message);

      if (!message.equals(maskedMessage)) {
        ThrowableInformation throwableInformation = event.getThrowableInformation();
        Throwable throwable = throwableInformation != null ? throwableInformation.getThrowable() : null;
        LoggingEvent maskedEvent = new LoggingEvent(event.fqnOfCategoryClass,
            Logger.getLogger(event.getLoggerName()), event.timeStamp,
            event.getLevel(), maskedMessage, throwable);

        return superFormat(maskedEvent);
      }
    }
    return superFormat(event);
  }

  String superFormat(LoggingEvent maskedEvent) {
    return super.format(maskedEvent);
  }
}