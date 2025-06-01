package dev.antoniomayk.finow.utils;

/**
 * A utility class for common string manipulation operations. This class provides static methods to
 * perform various transformations on strings.
 */
public class StringUtils {
  private StringUtils() {}

  /**
   * Converts a multi-line string into a single-line string. This method replaces any sequence of
   * whitespace characters (including newlines, tabs, and spaces) with a single space, and then
   * trims any leading or trailing whitespace from the resulting string.
   *
   * @param multiLineString The input string, potentially with multiple lines.
   * @return A single-line string representation.
   */
  public static String makeSingleLine(String multiLineString) {
    return multiLineString.replaceAll("\\s+", " ").trim();
  }
}
