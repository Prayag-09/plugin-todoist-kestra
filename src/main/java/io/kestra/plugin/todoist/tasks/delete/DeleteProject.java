package io.kestra.plugin.todoist.tasks.delete;

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
    title = "Delete a project in Todoist",
    description = "Permanently deletes a project from Todoist"
)
@Plugin(
    examples = {
        @Example(
            title = "Delete a project by ID",
            code = {
                "apiToken: \"{{ secret('TODOIST_API_TOKEN') }}\"",
                "projectId: \"2203306141\""
            }
        )
    }
)
public class DeleteProject extends AbstractTodoistTask implements RunnableTask<VoidOutput> {
    
    @Schema(
        title = "Project ID",
        description = "The ID of the project to delete"
    )
    @NotNull
    private Property<String> projectId;

    @Override
    public VoidOutput run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();
        
        String rToken = runContext.render(apiToken).as(String.class).orElseThrow();
        String rProjectId = runContext.render(projectId).as(String.class).orElseThrow();
        
        TodoistClient client = new TodoistClient(runContext, rToken, BASE_URL);
        client.delete("/projects/" + rProjectId);
        
        logger.info("Project {} deleted successfully", rProjectId);
        
        return null;
    }
}
