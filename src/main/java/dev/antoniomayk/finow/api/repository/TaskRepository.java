package dev.antoniomayk.finow.api.repository;

import static dev.antoniomayk.finow.utils.StringUtils.makeSingleLine;

import dev.antoniomayk.finow.api.model.NewTask;
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

/**
 * {@code TaskRepository} provides data access operations for {@link Task} model. It encapsulates
 * SQL queries for common CRUD (Create, Read, Update, Delete) operations on a 'tasks' table, using a
 * reactive SQL client.
 *
 * <p>This repository is designed to work with a {@code SqlConnection} and return {@code Future}
 * objects, indicating asynchronous operation execution.
 */
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
              (task_id, task_name, task_details)
            VALUES
              (#{TASK_ID}, #{TASK_NAME}, #{TASK_DETAILS})
          """);

  private static final String SQL_UPDATE =
      makeSingleLine(
          """
            UPDATE
              tasks
            SET
              task_name = #{TASK_NAME},
              task_details = #{TASK_DETAILS},
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

  /**
   * Retrieves a single task by its unique ID.
   *
   * @param connection The SQL connection to use for the query.
   * @param taskId The unique identifier of the task to retrieve.
   * @return A {@code Future} containing an {@code Optional<Task>}. The {@code Optional} will be
   *     present if a task with the given ID is found, otherwise, it will be empty.
   */
  public static Future<Optional<Task>> selectById(SqlConnection connection, UUID taskId) {
    return (Future<Optional<Task>>)
        SqlTemplate.forQuery(connection, SQL_SELECT_BY_ID)
            .mapTo(Task.class)
            .execute(Collections.singletonMap("TASK_ID", taskId))
            .map(rowSet -> StreamSupport.stream(rowSet.spliterator(), false).findFirst());
  }

  /**
   * Retrieves all tasks from the database.
   *
   * @param connection The SQL connection to use for the query.
   * @return A {@code Future} containing a {@code List<Task>} of all tasks found in the database.
   *     The list will be empty if no tasks are found.
   */
  public static Future<List<Task>> selectAll(SqlConnection connection) {
    return SqlTemplate.forQuery(connection, SQL_SELECT_ALL)
        .mapTo(Task.class)
        .execute(Collections.emptyMap())
        .map(rowSet -> StreamSupport.stream(rowSet.spliterator(), false).toList());
  }

  /**
   * Inserts a new task into the database. A new unique ID is always generated for the task before
   * insertion.
   *
   * @param connection The SQL connection to use for the insert operation.
   * @param task The {@link Task} object containing the name and details for the new task. Its
   *     {@code taskId} field will be ignored and a new one generated.
   * @return A {@code Future} containing the {@link UUID} of the newly inserted task.
   */
  public static Future<UUID> insert(SqlConnection connection, NewTask task) {
    final var taskId = UUID.randomUUID();

    return SqlTemplate.forUpdate(connection, SQL_INSERT)
        .mapFrom(Task.class)
        .execute(new Task(taskId, task.name(), task.details(), null, null))
        .map(sqlResult -> taskId);
  }

  /**
   * Updates an existing task in the database. The task is identified by its ID.
   *
   * @param connection The SQL connection to use for the update operation.
   * @param task The {@link Task} object containing the updated information. The {@code taskId}
   *     field of this object is used to locate the task to update.
   * @return A {@code Future} that completes with {@code Void} when the update operation is
   *     finished.
   */
  public static Future<Void> update(SqlConnection connection, Task task) {
    return SqlTemplate.forUpdate(connection, SQL_UPDATE)
        .mapFrom(Task.class)
        .execute(task)
        .map(SqlResult::value);
  }

  /**
   * Deletes a task from the database by its unique ID.
   *
   * @param connection The SQL connection to use for the delete operation.
   * @param taskId The unique identifier of the task to delete.
   * @return A {@code Future} that completes with {@code Void} when the delete operation is
   *     finished.
   */
  public static Future<Void> deleteById(SqlConnection connection, UUID taskId) {
    return SqlTemplate.forUpdate(connection, SQL_DELETE_BY_ID)
        .execute(Collections.singletonMap("TASK_ID", taskId))
        .map(SqlResult::value);
  }
}
