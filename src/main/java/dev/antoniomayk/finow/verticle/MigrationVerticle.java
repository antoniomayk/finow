package dev.antoniomayk.finow.verticle;

import dev.antoniomayk.finow.utils.JdbcUtils;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import org.flywaydb.core.Flyway;

public class MigrationVerticle extends VerticleBase {
  @Override
  public Future<?> start() throws Exception {
    var config = JdbcUtils.migrationConfig();
    var flyway = new Flyway(config);

    return vertx.executeBlocking(() -> flyway.migrate());
  }
}
