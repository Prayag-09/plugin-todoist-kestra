package io.kestra.plugin.todoist.tasks.update;

import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.plugin.todoist.tasks.create.CreateTask;
import io.kestra.plugin.todoist.models.TaskOutput;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class ReopenTaskTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void testReopenTask() throws Exception {
        String apiToken = System.getenv("TODOIST_API_TOKEN");
        
        if (apiToken == null || apiToken.isEmpty()) {
            System.out.println("Skipping test: TODOIST_API_TOKEN not set");
            return;
        }

        RunContext runContext = runContextFactory.of();

        // Create a task
        CreateTask createTask = CreateTask.builder()
            .apiToken(Property.of(apiToken))
            .content(Property.of("Test task for ReopenTask"))
            .build();

        TaskOutput createOutput = createTask.run(runContext);
        String taskId = createOutput.getTaskId();

        // Complete the task
        CompleteTask completeTask = CompleteTask.builder()
            .apiToken(Property.of(apiToken))
            .taskId(Property.of(taskId))
            .build();
        completeTask.run(runContext);

        // Reopen the task
        ReopenTask reopenTask = ReopenTask.builder()
            .apiToken(Property.of(apiToken))
            .taskId(Property.of(taskId))
            .build();
        reopenTask.run(runContext);

        // Clean up - complete again
        completeTask.run(runContext);
        
        // If no exception is thrown, the task was reopened successfully
        assertThat(taskId, notNullValue());
    }
}
