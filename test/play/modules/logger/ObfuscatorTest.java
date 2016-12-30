package play.modules.logger;

import org.junit.Test;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ObfuscatorTest {
  Obfuscator obfuscator = new Obfuscator();

  @Test
  public void nullSafe() {
    assertThat(obfuscator.isLikeCardNumber(null), is(false));
    assertThat(obfuscator.maskCardNumber(null), is(nullValue()));
  }

  @Test
  public void emptyString() {
    assertThat(obfuscator.isLikeCardNumber(""), is(false));
    assertThat(obfuscator.maskCardNumber(""), is(""));
  }

  @Test
  public void masksCardNumbers() {
    assertThat(obfuscator.isLikeCardNumber("4797707124015750"), is(true));
    assertThat(obfuscator.maskCardNumber("4797707124015750"), is("479770******5750"));
    
    assertThat(obfuscator.maskCardNumber("foo 4797707124015750 bar"), is("foo 479770******5750 bar"));
    assertThat(obfuscator.maskCardNumber("foo4797707124015750bar"), is("foo479770******5750bar"));
    assertThat(obfuscator.maskCardNumber("foo4797707124015750"), is("foo479770******5750"));
    assertThat(obfuscator.maskCardNumber("4797707124015750bar"), is("479770******5750bar"));
  }

  @Test
  public void doesNotMaskAccountNumbers() {
    assertThat(obfuscator.isLikeCardNumber("40702810090240700028"), is(false));
    assertThat(obfuscator.maskCardNumber("40702810090240700028"), is("40702810090240700028"));
  }

  @Test
  public void doesNotMaskPhoneNumbers() {
    assertThat(obfuscator.isLikeCardNumber("7916000000"), is(false));
    assertThat(obfuscator.maskCardNumber("7916000000"), is("7916000000"));
  }

  @Test
  public void masksMultipleCardNumbersInText() {
    assertThat(obfuscator.maskCardNumber("4797707124015750 tere foo4797707124015750bar"), 
        is("479770******5750 tere foo479770******5750bar"));
  }
}