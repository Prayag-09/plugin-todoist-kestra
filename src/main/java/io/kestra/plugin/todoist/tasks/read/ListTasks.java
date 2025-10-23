package io.kestra.plugin.todoist.tasks.read;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.FileSerde;
import io.kestra.plugin.todoist.client.TodoistClient;
import io.kestra.plugin.todoist.common.AbstractTodoistTask;
import io.kestra.plugin.todoist.models.FetchType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.slf4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "List tasks from Todoist",
    description = "Retrieves a list of tasks from Todoist with optional project filter. Supports FETCH, FETCH_ONE, and STORE modes for handling different data volumes."
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
        ),
        @Example(
            title = "Store large number of tasks in internal storage",
            code = {
                "apiToken: \"{{ secret('TODOIST_API_TOKEN') }}\"",
                "fetchType: STORE"
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

    @Schema(
        title = "Filter",
        description = "Filter tasks by a custom filter string (e.g., 'today', 'overdue', 'priority 1')"
    )
    private Property<String> filter;

    @Schema(
        title = "Fetch Type",
        description = "The way to fetch data: FETCH (default) returns tasks as output, FETCH_ONE returns only the first task, STORE stores tasks in internal storage for large datasets"
    )
    @Builder.Default
    private Property<FetchType> fetchType = Property.of(FetchType.FETCH);

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        
        String rToken = runContext.render(apiToken).as(String.class).orElseThrow();
        FetchType rFetchType = runContext.render(fetchType).as(FetchType.class).orElse(FetchType.FETCH);
        
        StringBuilder urlBuilder = new StringBuilder("/tasks");
        boolean hasParams = false;
        
        if (runContext.render(projectId).as(String.class).isPresent()) {
            String rProjectId = runContext.render(projectId).as(String.class).get();
            urlBuilder.append("?project_id=").append(rProjectId);
            hasParams = true;
        }
        
        if (runContext.render(filter).as(String.class).isPresent()) {
            String rFilter = runContext.render(filter).as(String.class).get();
            urlBuilder.append(hasParams ? "&" : "?").append("filter=").append(rFilter);
        }
        
        String endpoint = urlBuilder.toString();
        
        TodoistClient client = new TodoistClient(runContext, rToken, BASE_URL);
        List<Map<String, Object>> tasks = client.getList(endpoint);
        
        logger.info("Retrieved {} tasks", tasks.size());
        
        return switch (rFetchType) {
            case FETCH_ONE -> {
                if (tasks.isEmpty()) {
                    logger.warn("No tasks found, returning null");
                    yield Output.builder()
                        .task(null)
                        .count(0)
                        .build();
                }
                logger.debug("Returning first task only (FETCH_ONE mode)");
                yield Output.builder()
                    .task(tasks.get(0))
                    .count(tasks.size())
                    .build();
            }
            case STORE -> {
                File tempFile = runContext.workingDir().createTempFile(".ion").toFile();
                try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                    for (Map<String, Object> task : tasks) {
                        FileSerde.write(output, task);
                    }
                }
                URI uri = runContext.storage().putFile(tempFile);
                logger.info("Stored {} tasks in internal storage at {}", tasks.size(), uri);
                yield Output.builder()
                    .uri(uri)
                    .count(tasks.size())
                    .build();
            }
            default -> {
                logger.debug("Returning all {} tasks (FETCH mode)", tasks.size());
                yield Output.builder()
                    .tasks(tasks)
                    .count(tasks.size())
                    .build();
            }
        };
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Tasks",
            description = "List of tasks retrieved from Todoist (FETCH mode)"
        )
        private final List<Map<String, Object>> tasks;
        
        @Schema(
            title = "Task",
            description = "Single task retrieved from Todoist (FETCH_ONE mode)"
        )
        private final Map<String, Object> task;
        
        @Schema(
            title = "URI",
            description = "URI of the stored tasks file in internal storage (STORE mode)"
        )
        private final URI uri;
        
        @Schema(
            title = "Count",
            description = "Total number of tasks retrieved"
        )
        private final Integer count;
    }
}
