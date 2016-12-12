package play.modules.logger;

import java.util.regex.Pattern;

class Utils {
  private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("((?:\\b|\\D)[0-9]{6})([0-9]{6,9})([0-9]{4}(?:\\b|\\D))");

  public static String maskCardNumber(String text) {
    return CARD_NUMBER_PATTERN.matcher(text).replaceAll("$1******$3");
  }
}
