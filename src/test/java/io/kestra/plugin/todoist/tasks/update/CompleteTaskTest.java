package io.kestra.plugin.todoist.tasks.update;

import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.plugin.todoist.tasks.create.CreateTask;
import io.kestra.plugin.todoist.models.TaskOutput;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@KestraTest
class CompleteTaskTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void testCompleteTask() throws Exception {
        String apiToken = System.getenv("TODOIST_API_TOKEN");
        
        if (apiToken == null || apiToken.isEmpty()) {
            System.out.println("Skipping test: TODOIST_API_TOKEN not set");
            return;
        }

        RunContext runContext = runContextFactory.of();

        // First create a task to complete
        CreateTask createTask = CreateTask.builder()
            .apiToken(Property.of(apiToken))
            .content(Property.of("Test task for CompleteTask"))
            .build();

        TaskOutput createOutput = createTask.run(runContext);

        // Now complete the task
        CompleteTask completeTask = CompleteTask.builder()
            .apiToken(Property.of(apiToken))
            .taskId(Property.of(createOutput.getTaskId()))
            .build();

        completeTask.run(runContext);
        
        // If no exception is thrown, the task was completed successfully
    }
}
