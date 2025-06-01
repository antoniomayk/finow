package dev.antoniomayk.finow.verticle;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.antoniomayk.finow.utils.JdbcUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.impl.ConnectionImpl;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.internal.SqlConnectionInternal;
import java.sql.Connection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class TestMigrationVerticle {
  private static final String TABLE_NAME = "TASKS";

  @BeforeEach
  void deployVerticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MigrationVerticle()).onComplete(testContext.succeedingThenComplete());
  }

  @Test
  void executeMigrationAndValidateSchema(Vertx vertx, VertxTestContext testContext) {
    Handler<AsyncResult<Connection>> assertIfTableExists =
        testContext.succeeding(
            conn ->
                testContext.verify(
                    () -> {
                      Assertions.assertDoesNotThrow(
                          () -> {
                            var metadata = conn.getMetaData();
                            var exists =
                                metadata
                                    .getTables(null, null, TABLE_NAME, new String[] {"TABLE"})
                                    .next();

                            assertTrue(exists);

                            testContext.completeNow();
                          });
                    }));

    JdbcUtils.pool(vertx)
        .withConnection(
            conn -> {
              var sqlConnectionInternal = ((SqlConnectionInternal) conn).unwrap();
              if (!(sqlConnectionInternal instanceof ConnectionImpl)) {
                sqlConnectionInternal = sqlConnectionInternal.unwrap();
              } else {
                return Future.failedFuture(
                    new ClassCastException("Connection is not instanceof ConnectionImpl"));
              }

              return Future.succeededFuture(
                  ((ConnectionImpl) sqlConnectionInternal).getJDBCConnection());
            })
        .onComplete(assertIfTableExists);
  }
}
