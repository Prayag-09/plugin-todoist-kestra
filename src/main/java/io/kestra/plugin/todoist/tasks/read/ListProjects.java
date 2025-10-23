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
    title = "List all projects from Todoist",
    description = "Retrieves a list of all projects from Todoist. Supports FETCH, FETCH_ONE, and STORE modes."
)
@Plugin(
    examples = {
        @Example(
            title = "List all projects",
            code = {
                "apiToken: \"{{ secret('TODOIST_API_TOKEN') }}\""
            }
        ),
        @Example(
            title = "Get only the first project",
            code = {
                "apiToken: \"{{ secret('TODOIST_API_TOKEN') }}\"",
                "fetchType: FETCH_ONE"
            }
        )
    }
)
public class ListProjects extends AbstractTodoistTask implements RunnableTask<ListProjects.Output> {

    @Schema(
        title = "Fetch Type",
        description = "The way to fetch data: FETCH (default) returns all projects, FETCH_ONE returns only the first project, STORE stores projects in internal storage"
    )
    @Builder.Default
    private Property<FetchType> fetchType = Property.of(FetchType.FETCH);

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        
        String rToken = runContext.render(apiToken).as(String.class).orElseThrow();
        FetchType rFetchType = runContext.render(fetchType).as(FetchType.class).orElse(FetchType.FETCH);
        
        TodoistClient client = new TodoistClient(runContext, rToken, BASE_URL);
        List<Map<String, Object>> projects = client.getList("/projects");
        
        logger.info("Retrieved {} projects", projects.size());
        
        return switch (rFetchType) {
            case FETCH_ONE -> {
                if (projects.isEmpty()) {
                    logger.warn("No projects found, returning null");
                    yield Output.builder()
                        .project(null)
                        .count(0)
                        .build();
                }
                logger.debug("Returning first project only (FETCH_ONE mode)");
                yield Output.builder()
                    .project(projects.get(0))
                    .count(projects.size())
                    .build();
            }
            case STORE -> {
                File tempFile = runContext.workingDir().createTempFile(".ion").toFile();
                try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                    for (Map<String, Object> project : projects) {
                        FileSerde.write(output, project);
                    }
                }
                URI uri = runContext.storage().putFile(tempFile);
                logger.info("Stored {} projects in internal storage at {}", projects.size(), uri);
                yield Output.builder()
                    .uri(uri)
                    .count(projects.size())
                    .build();
            }
            default -> {
                logger.debug("Returning all {} projects (FETCH mode)", projects.size());
                yield Output.builder()
                    .projects(projects)
                    .count(projects.size())
                    .build();
            }
        };
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Projects",
            description = "List of projects retrieved from Todoist (FETCH mode)"
        )
        private final List<Map<String, Object>> projects;
        
        @Schema(
            title = "Project",
            description = "Single project retrieved from Todoist (FETCH_ONE mode)"
        )
        private final Map<String, Object> project;
        
        @Schema(
            title = "URI",
            description = "URI of the stored projects file in internal storage (STORE mode)"
        )
        private final URI uri;
        
        @Schema(
            title = "Count",
            description = "Number of projects retrieved"
        )
        private final Integer count;
    }
}
