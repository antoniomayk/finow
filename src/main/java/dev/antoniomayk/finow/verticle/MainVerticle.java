package dev.antoniomayk.finow.verticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.VerticleBase;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends VerticleBase {
  private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Future<?> start() throws Exception {
    var bootTime = System.currentTimeMillis();

    return deployMigrationVerticle(vertx)
        .flatMap(unused -> deployApiVerticle(vertx))
        .onSuccess(
            deploymentId ->
                LOG.info(
                    "finow start successfully in "
                        + (System.currentTimeMillis() - bootTime)
                        + " ms."))
        .onFailure(throwable -> LOG.error(throwable.getMessage(), throwable));
  }

  private Future<Void> deployMigrationVerticle(Vertx vertx) {
    var options =
        new DeploymentOptions()
            .setThreadingModel(ThreadingModel.WORKER)
            .setWorkerPoolName("migration-worker-pool")
            .setInstances(1)
            .setWorkerPoolSize(1);

    return vertx.deployVerticle(MigrationVerticle.class, options).flatMap(vertx::undeploy);
  }

  private Future<String> deployApiVerticle(Vertx vertx) {
    return vertx.deployVerticle(ApiVerticle.class.getName());
  }
}
