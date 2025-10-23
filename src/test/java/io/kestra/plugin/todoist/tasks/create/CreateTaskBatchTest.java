package io.kestra.plugin.todoist.tasks.create;

import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.plugin.todoist.tasks.delete.DeleteTask;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
class CreateTaskBatchTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void testCreateTaskBatch() throws Exception {
        String apiToken = System.getenv("TODOIST_API_TOKEN");
        
        if (apiToken == null || apiToken.isEmpty()) {
            System.out.println("Skipping test: TODOIST_API_TOKEN not set");
            return;
        }

        RunContext runContext = runContextFactory.of();

        List<Map<String, Object>> tasksToCreate = List.of(
            Map.of("content", "Batch task 1", "priority", 1),
            Map.of("content", "Batch task 2", "priority", 2),
            Map.of("content", "Batch task 3", "priority", 3)
        );

        CreateTaskBatch task = CreateTaskBatch.builder()
            .apiToken(Property.of(apiToken))
            .tasks(Property.of(tasksToCreate))
            .build();

        CreateTaskBatch.Output output = task.run(runContext);

        assertThat(output.getTotalCount(), is(3));
        assertThat(output.getSuccessCount(), is(3));
        assertThat(output.getFailureCount(), is(0));
        assertThat(output.getTasks(), hasSize(3));

        // Clean up - delete created tasks
        for (var createdTask : output.getTasks()) {
            DeleteTask deleteTask = DeleteTask.builder()
                .apiToken(Property.of(apiToken))
                .taskId(Property.of(createdTask.getTaskId()))
                .build();
            deleteTask.run(runContext);
        }
    }
}
