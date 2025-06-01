package dev.antoniomayk.finow.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Represents a task with its unique identifier.
 *
 * @param taskId The unique identifier for the task.
 * @param name The name of the task.
 * @param details Optional detailed description of the task.
 * @param details Optional timestamp indicating when the task was completed.
 * @param completedAt The unique identifier for the task.
 * @param createdAt Optional timestamp indicating when the task was created.
 */
public record Task(
    @JsonProperty(value = "TASK_ID") UUID taskId,
    @JsonProperty(value = "TASK_NAME") String name,
    @Nullable @JsonProperty(value = "TASK_DETAILS") String details,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
        @JsonProperty(value = "COMPLETED_AT")
        @Nullable LocalDateTime completedAt,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
        @JsonProperty(value = "CREATED_AT")
        @Nullable LocalDateTime createdAt) {}
