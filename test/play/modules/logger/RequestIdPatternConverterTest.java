package play.modules.logger;

import org.apache.log4j.Category;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Test;
import play.mvc.Http;

import static org.apache.log4j.Priority.WARN;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class RequestIdPatternConverterTest {

  @After
  public void tearDown() throws Exception {
    Http.Request.current.set(null);
  }

  @Test
  public void returnsThreadNameIfRequestIsMissing() throws Exception {
    Thread.currentThread().setName("Thread name");
    LoggingEvent event = new LoggingEvent("INFO", mock(Category.class), WARN, "message", null);

    assertThat(new RequestIdPatternConverter().convert(event), is("Thread name"));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void returnsRequestIdIfRequestIsPresent() throws Exception {
    Http.Request request = new Http.Request();
    request.args.put("requestId", "123");
    Http.Request.current.set(request);
    LoggingEvent event = new LoggingEvent("INFO", mock(Category.class), WARN, "message", null);

    assertThat(new RequestIdPatternConverter().convert(event), is("123"));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void returnsThreadNameIfRequestIdIsMissing() throws Exception {
    Thread.currentThread().setName("Thread name");
    Http.Request.current.set(new Http.Request());
    LoggingEvent event = new LoggingEvent("INFO", mock(Category.class), WARN, "message", null);

    assertThat(new RequestIdPatternConverter().convert(event), is("Thread name"));
  }
}