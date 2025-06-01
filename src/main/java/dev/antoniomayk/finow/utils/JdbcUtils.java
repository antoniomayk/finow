package dev.antoniomayk.finow.utils;

import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;

public class JdbcUtils {
  private static final String DB_HOST = "db.host";
  private static final String DB_USERNAME = "db.username";
  private static final String DB_PASSWORD = "db.password";

  private static final String DB_DRIVER = "jdbc:h2:";

  private JdbcUtils() {}

  public static Pool pool(Vertx vertx) {
    var properties = PropertiesUtils.instance().getProperties();
    var connectOptions =
        new JDBCConnectOptions()
            .setJdbcUrl(DB_DRIVER + properties.getProperty(DB_HOST))
            .setUser(properties.getProperty(DB_USERNAME))
            .setPassword(properties.getProperty(DB_PASSWORD));
    var poolOptions = new PoolOptions().setMaxSize(16);

    return JDBCPool.pool(vertx, connectOptions, poolOptions);
  }

  public static Configuration migrationConfig() {
    var properties = PropertiesUtils.instance().getProperties();
    var url = DB_DRIVER + properties.getProperty(DB_HOST);
    var user = properties.getProperty(DB_USERNAME);
    var password = properties.getProperty(DB_PASSWORD);

    return new FluentConfiguration().dataSource(url, user, password);
  }
}
