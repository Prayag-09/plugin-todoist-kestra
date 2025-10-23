package io.kestra.plugin.todoist.tasks.create;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.todoist.client.TodoistClient;
import io.kestra.plugin.todoist.common.AbstractTodoistTask;
import io.kestra.plugin.todoist.models.TaskOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Create multiple tasks in Todoist",
    description = "Creates multiple tasks in Todoist in a batch operation"
)
@Plugin(
    examples = {
        @Example(
            title = "Create multiple tasks",
            full = true,
            code = """
                id: create-batch-tasks
                namespace: io.kestra.plugin.todoist
                
                tasks:
                  - id: create_tasks
                    type: io.kestra.plugin.todoist.tasks.create.CreateTaskBatch
                    apiToken: "{{ secret('TODOIST_API_TOKEN') }}"
                    tasks:
                      - content: "Review PR #123"
                        priority: 3
                      - content: "Update documentation"
                        priority: 2
                      - content: "Deploy to staging"
                        priority: 4
                """
        )
    }
)
public class CreateTaskBatch extends AbstractTodoistTask implements RunnableTask<CreateTaskBatch.Output> {
    
    @Schema(
        title = "Tasks to create",
        description = "List of tasks to create with their properties"
    )
    @NotNull
    private Property<List<Map<String, Object>>> tasks;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        
        String rToken = runContext.render(apiToken).as(String.class).orElseThrow();
        List<Map<String, Object>> rTasks = runContext.render(tasks).asList(Map.class);
        
        TodoistClient client = new TodoistClient(runContext, rToken, BASE_URL);
        List<TaskOutput> createdTasks = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        for (Map<String, Object> taskData : rTasks) {
            try {
                Map<String, Object> result = client.post("/tasks", taskData);
                
                TaskOutput taskOutput = TaskOutput.builder()
                    .taskId(result.get("id").toString())
                    .content(result.get("content").toString())
                    .url(result.get("url").toString())
                    .build();
                
                createdTasks.add(taskOutput);
                successCount++;
                logger.info("Created task: {}", result.get("content"));
            } catch (Exception e) {
                failureCount++;
                logger.warn("Failed to create task: {}", e.getMessage());
            }
        }
        
        logger.info("Batch creation completed: {} succeeded, {} failed", successCount, failureCount);
        
        return Output.builder()
            .tasks(createdTasks)
            .totalCount(rTasks.size())
            .successCount(successCount)
            .failureCount(failureCount)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Created tasks",
            description = "List of successfully created tasks"
        )
        private final List<TaskOutput> tasks;
        
        @Schema(
            title = "Total count",
            description = "Total number of tasks attempted"
        )
        private final Integer totalCount;
        
        @Schema(
            title = "Success count",
            description = "Number of tasks successfully created"
        )
        private final Integer successCount;
        
        @Schema(
            title = "Failure count",
            description = "Number of tasks that failed to create"
        )
        private final Integer failureCount;
    }
}
