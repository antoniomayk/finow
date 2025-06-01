package dev.antoniomayk.finow.utils;

public class StringUtils {
  private StringUtils() {}

  public static String makeSingleLine(String str) {
    return str.replaceAll("\\s+", " ").trim();
  }
}
