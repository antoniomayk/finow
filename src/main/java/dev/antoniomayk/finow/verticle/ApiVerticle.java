package dev.antoniomayk.finow.verticle;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code ApiVerticle} is responsible for exposing the aplication's HTTP API.
 *
 * <p>It initializes and manages an HTTP server, handling incoming web requests and serving
 * responses at port 8888. This verticle acts as the entry point for client-side intereaction with
 * the Finow application.
 */
public class ApiVerticle extends VerticleBase {
  private static final Logger log = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Future<?> start() {
    var httpServer = vertx.createHttpServer();
    var httpPort = 8888;
    return httpServer
        .requestHandler(ctx -> ctx.response().end("finow-api"))
        .listen(httpPort)
        .onSuccess(http -> log.info("HTTP server started on port " + httpPort))
        .onFailure(throwable -> log.error(throwable.getMessage()));
  }
}
