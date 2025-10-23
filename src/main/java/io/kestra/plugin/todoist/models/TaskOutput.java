package io.kestra.plugin.todoist.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TaskOutput implements io.kestra.core.models.tasks.Output {
    
    @Schema(
        title = "Task ID",
        description = "The ID of the task"
    )
    private final String taskId;
    
    @Schema(
        title = "Task content",
        description = "The content of the task"
    )
    private final String content;
    
    @Schema(
        title = "Task URL",
        description = "The URL to view the task in Todoist"
    )
    private final String url;
}
