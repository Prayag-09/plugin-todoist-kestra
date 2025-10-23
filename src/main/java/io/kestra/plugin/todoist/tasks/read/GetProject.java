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
    title = "Get a project from Todoist",
    description = "Retrieves details of a specific project by ID"
)
@Plugin(
    examples = {
        @Example(
            title = "Get a project by ID",
            code = {
                "apiToken: \"{{ secret('TODOIST_API_TOKEN') }}\"",
                "projectId: \"2203306141\""
            }
        )
    }
)
public class GetProject extends AbstractTodoistTask implements RunnableTask<GetProject.Output> {
    
    @Schema(
        title = "Project ID",
        description = "The ID of the project to retrieve"
    )
    @NotNull
    private Property<String> projectId;

    @Override
    public Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        
        String rToken = runContext.render(apiToken).as(String.class).orElseThrow();
        String rProjectId = runContext.render(projectId).as(String.class).orElseThrow();
        
        TodoistClient client = new TodoistClient(runContext, rToken, BASE_URL);
        Map<String, Object> project = client.get("/projects/" + rProjectId);
        
        logger.info("Project {} retrieved successfully", rProjectId);
        
        return Output.builder()
            .project(project)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Project",
            description = "The complete project object from Todoist"
        )
        private final Map<String, Object> project;
    }
}
