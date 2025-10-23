package io.kestra.plugin.todoist.tasks.read;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.todoist.client.TodoistClient;
import io.kestra.plugin.todoist.common.AbstractTodoistTask;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Get a task from Todoist",
    description = "Retrieves details of a specific task by ID"
)
@Plugin(
    examples = {
        @Example(
            title = "Get a task by ID",
            code = {
                "apiToken: \"{{ secret('TODOIST_API_TOKEN') }}\"",
                "taskId: \"7498765432\""
            }
        )
    }
)
public class GetTask extends AbstractTodoistTask implements RunnableTask<GetTask.Output> {
    
    @Schema(
        title = "Task ID",
        description = "The ID of the task to retrieve"
    )
    @NotNull
    private Property<String> taskId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        
        String rToken = runContext.render(apiToken).as(String.class).orElseThrow();
        String rTaskId = runContext.render(taskId).as(String.class).orElseThrow();
        
        TodoistClient client = new TodoistClient(runContext, rToken, BASE_URL);
        Map<String, Object> task = client.get("/tasks/" + rTaskId);
        
        logger.info("Task {} retrieved successfully", rTaskId);
        
        return Output.builder()
            .task(task)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Task",
            description = "The complete task object from Todoist"
        )
        private final Map<String, Object> task;
    }
}
