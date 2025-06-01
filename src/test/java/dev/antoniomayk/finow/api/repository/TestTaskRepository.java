package dev.antoniomayk.finow.api.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.antoniomayk.finow.api.model.Task;
import dev.antoniomayk.finow.utils.JdbcUtils;
import dev.antoniomayk.finow.verticle.MigrationVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class TestTaskRepository {
  record Tuple<T, U>(T _0, U _1) {}

  record Tuple3<T, U, V>(T _0, U _1, V _2) {}

  private final TaskRepository taskRepository = new TaskRepository();

  static {
    DatabindCodec.mapper().registerModule(new JavaTimeModule());
  }

  @BeforeAll
  static void deployMigrationVerticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MigrationVerticle()).onComplete(testContext.succeedingThenComplete());
  }

  @Test
  void insertAndCheckReturn(Vertx vertx, VertxTestContext testContext) {
    Function<SqlConnection, Future<Tuple<Transaction, UUID>>> insertTask =
        (conn) ->
            conn.begin()
                .flatMap(
                    tx ->
                        taskRepository
                            .insert(conn, new Task(null, "Do it", null, null, null))
                            .map(task_id -> new Tuple<>(conn.transaction(), task_id)));

    Handler<AsyncResult<Tuple<Transaction, UUID>>> assertUuidNotNull =
        testContext.succeeding(
            tuple ->
                testContext.verify(
                    () -> {
                      assertNotNull(tuple._1);

                      var tx = tuple._0;
                      tx.rollback().onComplete(unused -> testContext.completeNow());
                    }));

    JdbcUtils.pool(vertx).withConnection(insertTask).onComplete(assertUuidNotNull);
  }

  @Test
  void selectAllAndCheckReturn(Vertx vertx, VertxTestContext testContext) {
    var expectedTaskList =
        Arrays.asList(
            new Task(null, "Do it 1", null, null, null),
            new Task(null, "Do it 2", null, null, null));

    Function<SqlConnection, Future<Tuple<Transaction, List<Task>>>> insertTasks =
        (conn) ->
            conn.begin()
                .flatMap(
                    tx -> taskRepository.insert(conn, expectedTaskList.getFirst()).map(task -> tx))
                .compose(
                    tx ->
                        taskRepository
                            .insert(conn, expectedTaskList.getLast())
                            .map(e -> new Tuple<>(conn, tx)),
                    Future::failedFuture)
                .flatMap(
                    tuple ->
                        taskRepository
                            .selectAll(tuple._0)
                            .map(taskList -> new Tuple<>(tuple._1, taskList)));

    Handler<AsyncResult<Tuple<Transaction, List<Task>>>> assertAllTaskNames =
        testContext.succeeding(
            tuple ->
                testContext.verify(
                    () -> {
                      var tx = tuple._0;
                      var taskList = tuple._1;

                      assertArrayEquals(
                          expectedTaskList.stream().map(Task::name).toArray(),
                          taskList.stream().map(Task::name).toArray());

                      tx.rollback().onComplete(unused -> testContext.completeNow());
                    }));

    JdbcUtils.pool(vertx).withConnection(insertTasks).onComplete(assertAllTaskNames);
  }

  @Test
  void selectByIdAndCheckReturn(Vertx vertx, VertxTestContext testContext) {
    var expectedTaskName = "Do it";

    Function<SqlConnection, Future<Tuple3<Transaction, UUID, Task>>> insertTasks =
        (conn) ->
            conn.begin()
                .flatMap(
                    tx ->
                        taskRepository
                            .insert(conn, new Task(null, expectedTaskName, null, null, null))
                            .map(task -> new Tuple<>(tx, task)))
                .flatMap(
                    tuple ->
                        taskRepository
                            .selectById(conn, tuple._1)
                            .map(Optional::get)
                            .map(task -> new Tuple3<>(tuple._0, tuple._1, task)));

    Handler<AsyncResult<Tuple3<Transaction, UUID, Task>>> assertTaskNameAndTaskId =
        testContext.succeeding(
            tuple ->
                testContext.verify(
                    () -> {
                      var expectedTaskId = tuple._1;
                      var actualTask = tuple._2;

                      assertEquals(expectedTaskName, actualTask.name());
                      assertEquals(expectedTaskId, actualTask.taskId());

                      var tx = tuple._0;
                      tx.rollback().onComplete(unused -> testContext.completeNow());
                    }));

    JdbcUtils.pool(vertx).withConnection(insertTasks).onComplete(assertTaskNameAndTaskId);
  }

  @Test
  void updateAndCheckReturn(Vertx vertx, VertxTestContext testContext) {
    Function<SqlConnection, Future<Tuple3<Transaction, Task, Task>>> insertTasks =
        (conn) ->
            conn.begin()
                .flatMap(
                    tx ->
                        taskRepository
                            .insert(conn, new Task(null, "Do it", null, null, null))
                            .map(task -> new Tuple<>(tx, task)))
                .flatMap(
                    tuple ->
                        taskRepository
                            .selectById(conn, tuple._1)
                            .map(Optional::get)
                            .map(
                                task ->
                                    new Tuple<>(
                                        task,
                                        new Task(
                                            task.taskId(),
                                            "Do It 2.0",
                                            task.details(),
                                            LocalDateTime.now(),
                                            task.createdAt())))
                            .map(tasks -> new Tuple3<>(tuple._0, tasks._0, tasks._1)))
                .flatMap(tuple -> taskRepository.update(conn, tuple._2).map(affectedRows -> tuple))
                .flatMap(
                    tuple ->
                        taskRepository
                            .selectById(conn, tuple._2.taskId())
                            .map(Optional::get)
                            .map(task -> new Tuple3<>(tuple._0, tuple._1, task)))
                .onFailure(
                    t -> {
                      t.printStackTrace();
                      Future.failedFuture(t);
                    });

    Handler<AsyncResult<Tuple3<Transaction, Task, Task>>> assertThatTaskAreNotEqual =
        testContext.succeeding(
            tuple ->
                testContext.verify(
                    () -> {
                      assertNotEquals(tuple._1, tuple._2);

                      var tx = tuple._0;
                      tx.rollback().onComplete(unused -> testContext.completeNow());
                    }));

    JdbcUtils.pool(vertx).withConnection(insertTasks).onComplete(assertThatTaskAreNotEqual);
  }

  @Test
  void deleteAndCheckReturn(Vertx vertx, VertxTestContext testContext) {
    Function<SqlConnection, Future<Tuple<Transaction, Optional<Task>>>> insertTasks =
        (conn) ->
            conn.begin()
                .flatMap(
                    tx ->
                        taskRepository
                            .insert(conn, new Task(null, "Do it", null, null, null))
                            .map(task -> new Tuple<>(tx, task)))
                .flatMap(
                    tuple ->
                        taskRepository
                            .deleteById(conn, tuple._1)
                            .map(affectedRows -> new Tuple<>(tuple._0, tuple._1)))
                .flatMap(
                    tuple ->
                        taskRepository
                            .selectById(conn, tuple._1)
                            .map(task -> new Tuple<>(tuple._0, task)));

    Handler<AsyncResult<Tuple<Transaction, Optional<Task>>>> assertThatUuidIsDeleted =
        testContext.succeeding(
            tuple ->
                testContext.verify(
                    () -> {
                      assertTrue(tuple._1.isEmpty());

                      var tx = tuple._0;
                      tx.rollback().onComplete(unused -> testContext.completeNow());
                    }));

    JdbcUtils.pool(vertx).withConnection(insertTasks).onComplete(assertThatUuidIsDeleted);
  }
}
