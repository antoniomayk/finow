package dev.antoniomayk.finow.api.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.antoniomayk.finow.api.model.NewTask;
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
class TestTaskRepository {
  record Tuple2<T, U>(T i0, U i1) {}

  record Tuple3<T, U, V>(T i0, U i1, V i2) {}

  static {
    DatabindCodec.mapper().registerModule(new JavaTimeModule());
  }

  @BeforeAll
  static void deployMigrationVerticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MigrationVerticle()).onComplete(testContext.succeedingThenComplete());
  }

  @Test
  void insertAndCheckReturn(Vertx vertx, VertxTestContext testContext) {
    Function<SqlConnection, Future<Tuple2<Transaction, UUID>>> insertTask =
        (conn) ->
            conn.begin()
                .flatMap(
                    tx ->
                        TaskRepository.insert(conn, new NewTask("Do it", null))
                            .map(taskId -> new Tuple2<>(conn.transaction(), taskId)));

    Handler<AsyncResult<Tuple2<Transaction, UUID>>> assertUuidNotNull =
        testContext.succeeding(
            tuple ->
                testContext.verify(
                    () -> {
                      assertNotNull(tuple.i1);

                      var tx = tuple.i0;
                      tx.rollback().onComplete(unused -> testContext.completeNow());
                    }));

    JdbcUtils.pool(vertx).withConnection(insertTask).onComplete(assertUuidNotNull);
  }

  @Test
  void selectAllAndCheckReturn(Vertx vertx, VertxTestContext testContext) {
    var expectedTaskList =
        Arrays.asList(new NewTask("Do it 1", null), new NewTask("Do it 2", null));

    Function<SqlConnection, Future<Tuple2<Transaction, List<Task>>>> insertTasks =
        (conn) ->
            conn.begin()
                .flatMap(
                    tx -> TaskRepository.insert(conn, expectedTaskList.getFirst()).map(task -> tx))
                .compose(
                    tx ->
                        TaskRepository.insert(conn, expectedTaskList.getLast())
                            .map(e -> new Tuple2<>(conn, tx)),
                    Future::failedFuture)
                .flatMap(
                    tuple ->
                        TaskRepository.selectAll(tuple.i0)
                            .map(taskList -> new Tuple2<>(tuple.i1, taskList)));

    Handler<AsyncResult<Tuple2<Transaction, List<Task>>>> assertAllTaskNames =
        testContext.succeeding(
            tuple ->
                testContext.verify(
                    () -> {
                      var tx = tuple.i0;
                      var taskList = tuple.i1;

                      assertArrayEquals(
                          expectedTaskList.stream().map(NewTask::name).toArray(),
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
                        TaskRepository.insert(conn, new NewTask(expectedTaskName, null))
                            .map(task -> new Tuple2<>(tx, task)))
                .flatMap(
                    tuple ->
                        TaskRepository.selectById(conn, tuple.i1)
                            .map(Optional::get)
                            .map(task -> new Tuple3<>(tuple.i0, tuple.i1, task)));

    Handler<AsyncResult<Tuple3<Transaction, UUID, Task>>> assertTaskNameAndTaskId =
        testContext.succeeding(
            tuple ->
                testContext.verify(
                    () -> {
                      var expectedTaskId = tuple.i1;
                      var actualTask = tuple.i2;

                      assertEquals(expectedTaskName, actualTask.name());
                      assertEquals(expectedTaskId, actualTask.taskId());

                      var tx = tuple.i0;
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
                        TaskRepository.insert(conn, new NewTask("Do it", null))
                            .map(task -> new Tuple2<>(tx, task)))
                .flatMap(
                    tuple ->
                        TaskRepository.selectById(conn, tuple.i1)
                            .map(Optional::get)
                            .map(
                                task ->
                                    new Tuple2<>(
                                        task,
                                        new Task(
                                            task.taskId(),
                                            "Do it 2.0",
                                            task.details(),
                                            LocalDateTime.now(),
                                            task.createdAt())))
                            .map(tasks -> new Tuple3<>(tuple.i0, tasks.i0, tasks.i1)))
                .flatMap(tuple -> TaskRepository.update(conn, tuple.i2).map(affectedRows -> tuple))
                .flatMap(
                    tuple ->
                        TaskRepository.selectById(conn, tuple.i2.taskId())
                            .map(Optional::get)
                            .map(task -> new Tuple3<>(tuple.i0, tuple.i1, task)))
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
                      assertNotEquals(tuple.i1, tuple.i2);

                      var tx = tuple.i0;
                      tx.rollback().onComplete(unused -> testContext.completeNow());
                    }));

    JdbcUtils.pool(vertx).withConnection(insertTasks).onComplete(assertThatTaskAreNotEqual);
  }

  @Test
  void deleteAndCheckReturn(Vertx vertx, VertxTestContext testContext) {
    Function<SqlConnection, Future<Tuple2<Transaction, Optional<Task>>>> insertTasks =
        (conn) ->
            conn.begin()
                .flatMap(
                    tx ->
                        TaskRepository.insert(conn, new NewTask("Do it", null))
                            .map(task -> new Tuple2<>(tx, task)))
                .flatMap(
                    tuple ->
                        TaskRepository.deleteById(conn, tuple.i1)
                            .map(affectedRows -> new Tuple2<>(tuple.i0, tuple.i1)))
                .flatMap(
                    tuple ->
                        TaskRepository.selectById(conn, tuple.i1)
                            .map(task -> new Tuple2<>(tuple.i0, task)));

    Handler<AsyncResult<Tuple2<Transaction, Optional<Task>>>> assertThatUuidIsDeleted =
        testContext.succeeding(
            tuple ->
                testContext.verify(
                    () -> {
                      assertTrue(tuple.i1.isEmpty());

                      var tx = tuple.i0;
                      tx.rollback().onComplete(unused -> testContext.completeNow());
                    }));

    JdbcUtils.pool(vertx).withConnection(insertTasks).onComplete(assertThatUuidIsDeleted);
  }
}
