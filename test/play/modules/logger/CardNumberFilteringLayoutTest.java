package play.modules.logger;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

import static org.junit.Assert.*;

public class CardNumberFilteringLayoutTest {
  @Test
  public void cardNumbersAreMasked() throws Exception {
    LoggingEvent event = new LoggingEvent("", Logger.getRootLogger(), Level.ALL, "Hello card 1234123412341234 and longer card 1234123412341234123, but not account DE123412341234123412341234", null);
    assertEquals("Hello card 123412******1234 and longer card 123412******4123, but not account DE123412341234123412341234\n", new CardNumberFilteringLayout().format(event));
  }
}