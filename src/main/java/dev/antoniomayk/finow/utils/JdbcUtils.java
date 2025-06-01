package dev.antoniomayk.finow.utils;

import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.configuration.FluentConfiguration;

/**
 * A utility class for creating database connection pools and Flyway migration configurations. This
 * class uses properties loaded via {@link PropertiesUtils} to configure database connections.
 */
public class JdbcUtils {
  private static final String DB_HOST = "db.host";
  private static final String DB_USERNAME = "db.username";
  private static final String DB_PASSWORD = "db.password";

  private static final String DB_DRIVER = "jdbc:h2:";

  private JdbcUtils() {}

  /**
   * Creates and returns a JDBC connection pool using Vert.x's JDBC client. The connection details
   * (host, username, password) are retrieved from the application's properties. The pool is
   * configured with a maximum size of 16 connections.
   *
   * @param vertx The Vert.x instance to be used for creating the JDBC pool.
   * @return A {@link Pool} instance configured for JDBC database access.
   */
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

  /**
   * Creates and returns a Flyway database migration configuration. The database URL, username, and
   * password are sourced from the application's properties.
   *
   * @return A {@link Configuration} instance set up for database migrations.
   */
  public static Configuration migrationConfig() {
    var properties = PropertiesUtils.instance().getProperties();
    var url = DB_DRIVER + properties.getProperty(DB_HOST);
    var user = properties.getProperty(DB_USERNAME);
    var password = properties.getProperty(DB_PASSWORD);

    return new FluentConfiguration().dataSource(url, user, password);
  }
}
