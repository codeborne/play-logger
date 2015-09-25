package play.modules.logger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExceptionsMonitoringPluginTest {
  @Test
  public void key() {
    assertEquals("java.lang.IllegalStateException: Hello", ExceptionsMonitoringPlugin.key(new IllegalStateException("Hello\nWorld")));
  }

  @Test
  public void keyWithSpecificNumbers() {
    assertEquals("java.lang.RuntimeException: Card '* - 0' not found or doesn't belong to 'CODEBFIMI' clerk access group or issued by restricted institution or disabled by VIP access configuration",
        ExceptionsMonitoringPlugin.key(new RuntimeException("Card '964301******1706 - 0' not found or doesn't belong to 'CODEBFIMI' clerk access group or issued by restricted institution or disabled by VIP access configuration")));

    assertEquals("java.lang.RuntimeException: Не обнаружен клиент в списке обслуживаемых клиентов. dbo_id=*.",
        ExceptionsMonitoringPlugin.key(new RuntimeException("Не обнаружен клиент в списке обслуживаемых клиентов. dbo_id=190398235334.")));
  }
}