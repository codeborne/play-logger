package play.modules.logger;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class UtilsTest {

  @Test
  public void masksCardNumbers() throws Exception {
    assertThat(Utils.maskCardNumber(""), is(""));
    assertThat(Utils.maskCardNumber("4797707124015750"), is("479770******5750"));
    assertThat(Utils.maskCardNumber("foo 4797707124015750 bar"), is("foo 479770******5750 bar"));
    assertThat(Utils.maskCardNumber("foo4797707124015750bar"), is("foo479770******5750bar"));
    assertThat(Utils.maskCardNumber("foo4797707124015750"), is("foo479770******5750"));
    assertThat(Utils.maskCardNumber("4797707124015750bar"), is("479770******5750bar"));
  }

  @Test
  public void doesNotMaskAccountNumbers() throws Exception {
    assertThat(Utils.maskCardNumber("40702810090240700028"), is("40702810090240700028"));
  }

  @Test
  public void doesNotMaskPhoneNumbers() throws Exception {
    assertThat(Utils.maskCardNumber("7916000000"), is("7916000000"));
  }
}