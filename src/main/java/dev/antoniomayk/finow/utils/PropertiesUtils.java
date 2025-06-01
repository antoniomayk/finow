package dev.antoniomayk.finow.utils;

import java.io.IOException;
import java.util.Properties;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A utility class for managing application properties. This class implements a singleton pattern to
 * ensure a single instance of loaded properties is available throughout the application. Properties
 * are loaded from "classpath:application.properties".
 */
public class PropertiesUtils {
  @Nullable private static PropertiesUtils instance;

  private final Properties properties;

  private PropertiesUtils() {
    properties = readProperties();
  }

  /**
   * Returns the singleton instance of {@code PropertiesUtils}. If the instance does not already
   * exist, it is created and initialized by reading properties from the "application.properties"
   * file.
   *
   * @return The singleton instance of PropertiesUtils.
   */
  @NonNull
  public static PropertiesUtils instance() {
    if (instance == null) {
      instance = new PropertiesUtils();
    }
    return instance;
  }

  /**
   * Retrieves the loaded properties. These properties are read from the "application.properties"
   * file upon the first call to {@link #instance()}.
   *
   * @return A {@link Properties} object containing the application properties.
   */
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
