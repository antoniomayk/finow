package dev.antoniomayk.finow.api.repository;

import static dev.antoniomayk.finow.utils.StringUtils.makeSingleLine;

import dev.antoniomayk.finow.api.model.Task;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.templates.SqlTemplate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

public class TaskRepository {
  private static final String SQL_SELECT_ALL =
      makeSingleLine(
          """
            SELECT
              *
            FROM
              tasks
          """);
  private static final String SQL_SELECT_BY_ID =
      makeSingleLine(
          """
            SELECT
              *
            FROM
              tasks
            WHERE
              task_id = #{TASK_ID}
          """);
  private static final String SQL_INSERT =
      makeSingleLine(
          """
            INSERT INTO tasks
              (task_id, name, details)
            VALUES
              (#{TASK_ID}, #{NAME}, #{DETAILS})
          """);
  private static final String SQL_UPDATE =
      makeSingleLine(
          """
            UPDATE
              tasks
            SET
              name = #{NAME},
              details = #{DETAILS},
              completed_at = #{COMPLETED_AT}
            WHERE task_id = #{TASK_ID}
          """);
  private static final String SQL_DELETE_BY_ID =
      makeSingleLine(
          """
            DELETE FROM
              tasks
            WHERE
              task_id = #{TASK_ID}
          """);

  public TaskRepository() {}

  public Future<Optional<Task>> selectById(SqlConnection connection, UUID taskId) {
    return SqlTemplate.forQuery(connection, SQL_SELECT_BY_ID)
        .mapTo(Task.class)
        .execute(Collections.singletonMap("TASK_ID", taskId))
        .map(rowSet -> StreamSupport.stream(rowSet.spliterator(), false).findFirst());
  }

  public Future<List<Task>> selectAll(SqlConnection connection) {
    return SqlTemplate.forQuery(connection, SQL_SELECT_ALL)
        .mapTo(Task.class)
        .execute(Collections.emptyMap())
        .map(rowSet -> StreamSupport.stream(rowSet.spliterator(), false).toList());
  }

  public Future<UUID> insert(SqlConnection connection, Task task) {
    final var taskId = UUID.randomUUID();

    return SqlTemplate.forUpdate(connection, SQL_INSERT)
        .mapFrom(Task.class)
        .execute(new Task(taskId, task))
        .map(sqlResult -> taskId);
  }

  public Future<Void> update(SqlConnection connection, Task task) {
    return SqlTemplate.forUpdate(connection, SQL_UPDATE)
        .mapFrom(Task.class)
        .execute(task)
        .map(SqlResult::value);
  }

  public Future<Void> deleteById(SqlConnection connection, UUID taskId) {
    return SqlTemplate.forUpdate(connection, SQL_DELETE_BY_ID)
        .execute(Collections.singletonMap("TASK_ID", taskId))
        .map(SqlResult::value);
  }
}
