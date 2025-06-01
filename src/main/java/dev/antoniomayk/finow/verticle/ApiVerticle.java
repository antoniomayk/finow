package dev.antoniomayk.finow.verticle;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiVerticle extends VerticleBase {
  private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Future<?> start() {
    var httpServer = vertx.createHttpServer();
    var httpPort = 8888;
    return httpServer
        .requestHandler(ctx -> ctx.response().end("finow-api"))
        .listen(httpPort)
        .onSuccess(http -> LOG.info("HTTP server started on port " + httpPort))
        .onFailure(throwable -> LOG.error(throwable.getMessage()));
  }
}
