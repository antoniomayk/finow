package dev.antoniomayk.finow.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record Task(
    @JsonProperty(value = "TASK_ID") UUID taskId,
    @JsonProperty(value = "NAME") String name,
    @JsonProperty(value = "DETAILS") String details,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    @JsonProperty(value = "COMPLETED_AT")
    LocalDateTime completedAt,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    @JsonProperty(value = "CREATED_AT")
    LocalDateTime createdAt) {

  public Task(UUID taskId, Task task) {
    this(taskId, task.name, task.details, task.completedAt, task.createdAt);
  }
}
