package dev.antoniomayk.finow.utils;

import java.io.IOException;
import java.util.Properties;

public class PropertiesUtils {
  private static PropertiesUtils instance;

  private final Properties properties;

  private PropertiesUtils() {
    properties = readProperties();
  }

  public static PropertiesUtils instance() {
    if (instance == null) {
      instance = new PropertiesUtils();
    }
    return instance;
  }

  public Properties getProperties() {
    return properties;
  }

  private Properties readProperties() {
    Properties properties = new Properties();
    try (var inStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {
      properties.load(inStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return properties;
  }
}
