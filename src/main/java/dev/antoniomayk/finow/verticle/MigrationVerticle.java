package dev.antoniomayk.finow.verticle;

import dev.antoniomayk.finow.utils.JdbcUtils;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes Flyway database migration on startup.
 *
 * <p>It obtains the migration configuration from {@code JdbcUtils#migrationConfig()} and runs
 * {@code Flyway#migrate()} on a worker thread to prevent blocking the event loop.
 */
public class MigrationVerticle extends VerticleBase {
  private static final Logger log = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public Future<?> start() throws Exception {
    var config = JdbcUtils.migrationConfig();
    var flyway = new Flyway(config);

    return vertx.executeBlocking(
        () -> {
          log.info("Starting Flyway migration service at " + config.getUrl() + ".");
          return flyway.migrate();
        });
  }
}
