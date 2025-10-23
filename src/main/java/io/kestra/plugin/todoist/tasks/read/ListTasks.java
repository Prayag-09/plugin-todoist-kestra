package io.kestra.plugin.todoist.tasks.read;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.todoist.client.TodoistClient;
import io.kestra.plugin.todoist.common.AbstractTodoistTask;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "List tasks from Todoist",
    description = "Retrieves a list of tasks from Todoist with optional project filter"
)
@Plugin(
    examples = {
        @Example(
            title = "List all active tasks",
            code = {
                "apiToken: \"{{ secret('TODOIST_API_TOKEN') }}\""
            }
        ),
        @Example(
            title = "List tasks for a specific project",
            code = {
                "apiToken: \"{{ secret('TODOIST_API_TOKEN') }}\"",
                "projectId: \"2203306141\""
            }
        )
    }
)
public class ListTasks extends AbstractTodoistTask implements RunnableTask<ListTasks.Output> {
    
    @Schema(
        title = "Project ID",
        description = "Filter tasks by project ID"
    )
    private Property<String> projectId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        
        String rToken = runContext.render(apiToken).as(String.class).orElseThrow();
        
        StringBuilder urlBuilder = new StringBuilder("/tasks");
        
        runContext.render(projectId).as(String.class).ifPresent(p -> {
            urlBuilder.append("?project_id=").append(p);
        });
        
        String endpoint = urlBuilder.toString();
        
        TodoistClient client = new TodoistClient(runContext, rToken, BASE_URL);
        List<Map<String, Object>> tasks = client.getList(endpoint);
        
        logger.info("Retrieved {} tasks", tasks.size());
        
        return Output.builder()
            .tasks(tasks)
            .count(tasks.size())
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Tasks",
            description = "List of tasks retrieved from Todoist"
        )
        private final List<Map<String, Object>> tasks;
        
        @Schema(
            title = "Count",
            description = "Number of tasks retrieved"
        )
        private final Integer count;
    }
}
