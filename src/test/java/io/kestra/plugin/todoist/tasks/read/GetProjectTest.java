package io.kestra.plugin.todoist.tasks.read;

import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.junit.annotations.KestraTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class GetProjectTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void testGetProject() throws Exception {
        String apiToken = System.getenv("TODOIST_API_TOKEN");
        
        if (apiToken == null || apiToken.isEmpty()) {
            System.out.println("Skipping test: TODOIST_API_TOKEN not set");
            return;
        }

        RunContext runContext = runContextFactory.of();

        // First list projects to get a valid project ID
        ListProjects listProjects = ListProjects.builder()
            .apiToken(Property.of(apiToken))
            .build();

        ListProjects.Output listOutput = listProjects.run(runContext);
        
        if (listOutput.getProjects().isEmpty()) {
            System.out.println("Skipping test: No projects found");
            return;
        }

        // Get the first project
        List<Map<String, Object>> projects = listOutput.getProjects();
        String projectId = projects.get(0).get("id").toString();

        // Now get the project details
        GetProject getProject = GetProject.builder()
            .apiToken(Property.of(apiToken))
            .projectId(Property.of(projectId))
            .build();

        GetProject.Output output = getProject.run(runContext);

        assertThat(output.getProject(), notNullValue());
        assertThat(output.getProject().get("id").toString(), is(projectId));
        assertThat(output.getProject().get("name"), notNullValue());
    }
}
