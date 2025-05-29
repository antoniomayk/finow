package dev.antoniomayk.finow;

import dev.antoniomayk.finow.verticle.MainVerticle;
import io.vertx.core.Vertx;

public class Main {
  public static void main(String[] args) {
    var vertx = Vertx.vertx();
    vertx.deployVerticle(MainVerticle.class.getName()).onFailure(throwable -> System.exit(-1));
  }
}
