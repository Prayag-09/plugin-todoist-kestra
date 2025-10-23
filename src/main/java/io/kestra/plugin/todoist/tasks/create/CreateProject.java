package io.kestra.plugin.todoist.tasks.create;

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

import java.util.HashMap;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Create a new project in Todoist",
    description = "Creates a new project in Todoist with the specified name and optional parameters"
)
@Plugin(
    examples = {
        @Example(
            title = "Create a simple project",
            code = {
                "apiToken: \"{{ secret('TODOIST_API_TOKEN') }}\"",
                "name: \"Work Projects\""
            }
        ),
        @Example(
            title = "Create a project with color and favorite",
            code = {
                "apiToken: \"{{ secret('TODOIST_API_TOKEN') }}\"",
                "name: \"Personal Goals\"",
                "color: \"blue\"",
                "isFavorite: true"
            }
        )
    }
)
public class CreateProject extends AbstractTodoistTask implements RunnableTask<CreateProject.Output> {
    
    @Schema(
        title = "Project name",
        description = "The name of the project"
    )
    @NotNull
    private Property<String> name;
    
    @Schema(
        title = "Color",
        description = "The color of the project icon (e.g., 'red', 'blue', 'green')"
    )
    private Property<String> color;
    
    @Schema(
        title = "Is Favorite",
        description = "Whether the project is a favorite"
    )
    private Property<Boolean> isFavorite;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        
        String rToken = runContext.render(apiToken).as(String.class).orElseThrow();
        String rName = runContext.render(name).as(String.class).orElseThrow();
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", rName);
        
        runContext.render(color).as(String.class).ifPresent(c -> requestBody.put("color", c));
        runContext.render(isFavorite).as(Boolean.class).ifPresent(f -> requestBody.put("is_favorite", f));
        
        TodoistClient client = new TodoistClient(runContext, rToken, BASE_URL);
        Map<String, Object> result = client.post("/projects", requestBody);
        
        logger.info("Project '{}' created successfully with ID: {}", rName, result.get("id"));
        
        return Output.builder()
            .projectId(result.get("id").toString())
            .name(result.get("name").toString())
            .url(result.get("url") != null ? result.get("url").toString() : null)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Project ID",
            description = "The ID of the created project"
        )
        private final String projectId;
        
        @Schema(
            title = "Project name",
            description = "The name of the created project"
        )
        private final String name;
        
        @Schema(
            title = "Project URL",
            description = "The URL to view the project in Todoist"
        )
        private final String url;
    }
}
