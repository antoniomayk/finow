package dev.antoniomayk.finow.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * Represents a new task with a name and optional details.
 *
 * @param name The name of the task.
 * @param details Optional details about the task.
 */
public record NewTask(
    @JsonProperty(value = "TASK_NAME") String name,
    @Nullable @JsonProperty(value = "TASK_DETAILS") String details) {}
