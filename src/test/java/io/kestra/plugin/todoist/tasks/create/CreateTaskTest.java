package io.kestra.plugin.todoist.tasks.create;

import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.plugin.todoist.models.TaskOutput;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class CreateTaskTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void testCreateTask() throws Exception {
        String apiToken = System.getenv("TODOIST_API_TOKEN");
        
        if (apiToken == null || apiToken.isEmpty()) {
            System.out.println("Skipping test: TODOIST_API_TOKEN not set");
            return;
        }

        RunContext runContext = runContextFactory.of();

        CreateTask task = CreateTask.builder()
            .apiToken(Property.of(apiToken))
            .content(Property.of("Test task from Kestra"))
            .taskDescription(Property.of("This is a test task created by the Kestra Todoist plugin"))
            .priority(Property.of(1))
            .build();

        TaskOutput output = task.run(runContext);

        assertThat(output.getTaskId(), notNullValue());
        assertThat(output.getContent(), is("Test task from Kestra"));
        assertThat(output.getUrl(), notNullValue());
    }
}
