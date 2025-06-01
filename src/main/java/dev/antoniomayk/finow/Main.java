package dev.antoniomayk.finow;

import dev.antoniomayk.finow.verticle.MainVerticle;
import io.vertx.core.Vertx;

/**
 * Main entry point for Vert.x application. This class initializes and deploys the primary Verticle.
 */
public class Main {
  /**
   * Deploy the {@code MainVerticle} to start the application. Exits the application with an error
   * code if deployment fails
   *
   * @param args Command line arguments (not used in this application).
   */
  public static void main(String[] args) {
    var vertx = Vertx.vertx();
    vertx.deployVerticle(MainVerticle.class.getName()).onFailure(throwable -> System.exit(-1));
  }
}
