package dev.antoniomayk.finow.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class TestJdbcUtils {

  @Test
  void createPoolAndCheckSqlStatement(Vertx vertx, VertxTestContext testContext) {
    Handler<AsyncResult<RowSet<Row>>> testIfFirstColumnValueIsEqualTo1 =
        testContext.succeeding(
            rows ->
                testContext.verify(
                    () -> {
                      var row = rows.iterator().next();
                      assertEquals(1, row.getInteger(0));

                      testContext.completeNow();
                    }));
    JdbcUtils.pool(vertx).query("SELECT 1").execute().onComplete(testIfFirstColumnValueIsEqualTo1);
  }

  @Test
  void createFlywayConfiguration() {
    var config = JdbcUtils.migrationConfig();
    assertEquals("jdbc:h2:~/finow-test", config.getUrl());
    assertEquals("h2", config.getUser());
    assertEquals("finow", config.getPassword());
  }
}
