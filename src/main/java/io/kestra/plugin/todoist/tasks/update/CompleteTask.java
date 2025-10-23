package io.kestra.plugin.todoist.tasks.update;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.VoidOutput;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.todoist.client.TodoistClient;
import io.kestra.plugin.todoist.common.AbstractTodoistTask;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Complete a task in Todoist",
    description = "Marks a task as completed in Todoist"
)
@Plugin(
    examples = {
        @Example(
            title = "Complete a task by ID",
            code = {
                "apiToken: \"{{ secret('TODOIST_API_TOKEN') }}\"",
                "taskId: \"7498765432\""
            }
        )
    }
)
public class CompleteTask extends AbstractTodoistTask implements RunnableTask<VoidOutput> {
    
    @Schema(
        title = "Task ID",
        description = "The ID of the task to complete"
    )
    @NotNull
    private Property<String> taskId;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        
        String rToken = runContext.render(apiToken).as(String.class).orElseThrow();
        String rTaskId = runContext.render(taskId).as(String.class).orElseThrow();
        
        TodoistClient client = new TodoistClient(runContext, rToken, BASE_URL);
        client.postVoid("/tasks/" + rTaskId + "/close");
        
        logger.info("Task {} completed successfully", rTaskId);
        
        return null;
    }
}
