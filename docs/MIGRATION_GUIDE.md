# Migration Guide: Folder Structure Refactoring

## Overview

This guide helps developers understand and adapt to the new folder structure introduced in the Todoist plugin refactoring.

## Quick Reference

### Package Mapping Table

| Old Location                                   | New Location                                          | Notes                       |
| ---------------------------------------------- | ----------------------------------------------------- | --------------------------- |
| `io.kestra.plugin.todoist.AbstractTodoistTask` | `io.kestra.plugin.todoist.common.AbstractTodoistTask` | Moved to common package     |
| `io.kestra.plugin.todoist.CreateTask`          | `io.kestra.plugin.todoist.tasks.create.CreateTask`    | Organized by operation type |
| `io.kestra.plugin.todoist.GetTask`             | `io.kestra.plugin.todoist.tasks.read.GetTask`         | Read operations grouped     |
| `io.kestra.plugin.todoist.ListTasks`           | `io.kestra.plugin.todoist.tasks.read.ListTasks`       | Read operations grouped     |
| `io.kestra.plugin.todoist.UpdateTask`          | `io.kestra.plugin.todoist.tasks.update.UpdateTask`    | Update operations grouped   |
| `io.kestra.plugin.todoist.CompleteTask`        | `io.kestra.plugin.todoist.tasks.update.CompleteTask`  | Completing is an update     |
| `io.kestra.plugin.todoist.DeleteTask`          | `io.kestra.plugin.todoist.tasks.delete.DeleteTask`    | Delete operations grouped   |
| N/A                                            | `io.kestra.plugin.todoist.client.TodoistClient`       | New HTTP client abstraction |
| N/A                                            | `io.kestra.plugin.todoist.client.TodoistApiException` | New exception class         |
| N/A                                            | `io.kestra.plugin.todoist.models.TaskOutput`          | New shared output model     |

## For Workflow Users

### ✅ No Changes Required!

If you're using the plugin in Kestra workflows, **you don't need to change anything**. All task type names remain the same:

```yaml
# This continues to work exactly as before
tasks:
  - id: create_task
    type: io.kestra.plugin.todoist.CreateTask
    apiToken: "{{ secret('TODOIST_API_TOKEN') }}"
    content: "My task"
```

The plugin system uses annotations and metadata, so internal package structure doesn't affect workflow definitions.

## For Plugin Developers

### Updating Imports

If you're developing or extending the plugin, update your imports:

#### Before

```java
import io.kestra.plugin.todoist.CreateTask;
import io.kestra.plugin.todoist.GetTask;
import io.kestra.plugin.todoist.ListTasks;
import io.kestra.plugin.todoist.UpdateTask;
import io.kestra.plugin.todoist.CompleteTask;
import io.kestra.plugin.todoist.DeleteTask;
import io.kestra.plugin.todoist.AbstractTodoistTask;
```

#### After

```java
import io.kestra.plugin.todoist.tasks.create.CreateTask;
import io.kestra.plugin.todoist.tasks.read.GetTask;
import io.kestra.plugin.todoist.tasks.read.ListTasks;
import io.kestra.plugin.todoist.tasks.update.UpdateTask;
import io.kestra.plugin.todoist.tasks.update.CompleteTask;
import io.kestra.plugin.todoist.tasks.delete.DeleteTask;
import io.kestra.plugin.todoist.common.AbstractTodoistTask;
```

### Using the New Client

The new `TodoistClient` simplifies HTTP operations:

#### Before (in each task)

```java
HttpRequest request = HttpRequest.builder()
    .uri(URI.create(BASE_URL + "/tasks"))
    .addHeader("Authorization", "Bearer " + token)
    .addHeader("Content-Type", "application/json")
    .method("POST")
    .body(HttpRequest.StringRequestBody.builder().content(jsonBody).build())
    .build();

HttpClient client = HttpClient.builder()
    .runContext(runContext)
    .build();

HttpResponse<String> response = client.request(request, String.class);

if (response.getStatus().getCode() >= 400) {
    throw new Exception("Failed: " + response.getStatus().getCode());
}

Map<String, Object> result = JacksonMapper.ofJson()
    .readValue(response.getBody(), Map.class);
```

#### After (using TodoistClient)

```java
TodoistClient client = new TodoistClient(runContext, token, BASE_URL);
Map<String, Object> result = client.post("/tasks", requestBody);
```

### Using Shared Models

The new `TaskOutput` model reduces duplication:

#### Before (in CreateTask and UpdateTask)

```java
@Builder
@Getter
public static class Output implements io.kestra.core.models.tasks.Output {
    private final String taskId;
    private final String content;
    private final String url;
}
```

#### After (import shared model)

```java
import io.kestra.plugin.todoist.models.TaskOutput;

// Use TaskOutput directly
public TaskOutput run(RunContext runContext) throws Exception {
    // ...
    return TaskOutput.builder()
        .taskId(result.get("id").toString())
        .content(result.get("content").toString())
        .url(result.get("url").toString())
        .build();
}
```

## For Test Developers

### Test File Locations

Tests now mirror the main source structure:

#### Before

```
src/test/java/io/kestra/plugin/todoist/
├── CreateTaskTest.java
├── GetTaskTest.java
├── ListTasksTest.java
├── UpdateTaskTest.java
├── CompleteTaskTest.java
└── DeleteTaskTest.java
```

#### After

```
src/test/java/io/kestra/plugin/todoist/tasks/
├── create/
│   └── CreateTaskTest.java
├── read/
│   ├── GetTaskTest.java
│   └── ListTasksTest.java
├── update/
│   ├── UpdateTaskTest.java
│   └── CompleteTaskTest.java
└── delete/
    └── DeleteTaskTest.java
```

### Updating Test Imports

```java
// Before
package io.kestra.plugin.todoist;
import io.kestra.plugin.todoist.CreateTask;

// After
package io.kestra.plugin.todoist.tasks.create;
import io.kestra.plugin.todoist.tasks.create.CreateTask;
```

## Adding New Features

### Creating a New Task

Follow the established pattern:

1. **Choose the appropriate folder**:

   - `tasks/create/` for creation operations
   - `tasks/read/` for retrieval operations
   - `tasks/update/` for modification operations
   - `tasks/delete/` for deletion operations

2. **Extend AbstractTodoistTask**:

```java
package io.kestra.plugin.todoist.tasks.create;

import io.kestra.plugin.todoist.common.AbstractTodoistTask;

@SuperBuilder
@Plugin
public class MyNewTask extends AbstractTodoistTask implements RunnableTask<MyOutput> {
    // Implementation
}
```

3. **Use TodoistClient**:

```java
@Override
public MyOutput run(RunContext runContext) throws Exception {
    String token = runContext.render(apiToken).as(String.class).orElseThrow();
    TodoistClient client = new TodoistClient(runContext, token, BASE_URL);

    Map<String, Object> result = client.post("/endpoint", requestBody);

    return MyOutput.builder()
        .data(result)
        .build();
}
```

4. **Create corresponding test**:

```
src/test/java/io/kestra/plugin/todoist/tasks/create/MyNewTaskTest.java
```

### Adding a New Feature Category

To add a new category (e.g., projects):

1. **Create folder structure**:

```
tasks/
└── projects/
    ├── CreateProject.java
    ├── GetProject.java
    ├── ListProjects.java
    └── DeleteProject.java
```

2. **Follow existing patterns**:
   - Extend `AbstractTodoistTask`
   - Use `TodoistClient`
   - Create shared models if needed
   - Mirror structure in tests

## Common Patterns

### Pattern 1: Simple GET Request

```java
TodoistClient client = new TodoistClient(runContext, token, BASE_URL);
Map<String, Object> data = client.get("/endpoint");
```

### Pattern 2: GET List

```java
TodoistClient client = new TodoistClient(runContext, token, BASE_URL);
List<Map<String, Object>> items = client.getList("/endpoint");
```

### Pattern 3: POST with Response

```java
TodoistClient client = new TodoistClient(runContext, token, BASE_URL);
Map<String, Object> result = client.post("/endpoint", requestBody);
```

### Pattern 4: POST without Response

```java
TodoistClient client = new TodoistClient(runContext, token, BASE_URL);
client.postVoid("/endpoint");
```

### Pattern 5: DELETE

```java
TodoistClient client = new TodoistClient(runContext, token, BASE_URL);
client.delete("/endpoint");
```

## Error Handling

### Before

```java
if (response.getStatus().getCode() >= 400) {
    throw new Exception("Failed: " + response.getStatus().getCode());
}
```

### After

```java
// TodoistClient handles errors automatically
// Throws TodoistApiException with detailed message
try {
    client.post("/endpoint", data);
} catch (TodoistApiException e) {
    // Handle API error
    logger.error("API error: {}", e.getMessage());
    throw e;
}
```

## IDE Support

### IntelliJ IDEA

1. **Refactor → Move**: IntelliJ will update imports automatically
2. **Find Usages**: Locate all references to moved classes
3. **Optimize Imports**: Clean up unused imports

### VS Code

1. Use "Rename Symbol" for package changes
2. Use "Find All References" to locate usages
3. Use "Organize Imports" to clean up

## Troubleshooting

### Issue: Import not found

**Problem**: `Cannot resolve symbol 'CreateTask'`

**Solution**: Update import path:

```java
// Change from
import io.kestra.plugin.todoist.CreateTask;

// To
import io.kestra.plugin.todoist.tasks.create.CreateTask;
```

### Issue: Test not found

**Problem**: Test class not in expected location

**Solution**: Move test to mirror main source structure:

```
src/test/java/io/kestra/plugin/todoist/tasks/create/CreateTaskTest.java
```

### Issue: Build fails

**Problem**: Compilation errors after refactoring

**Solution**:

1. Clean build: `./gradlew clean`
2. Rebuild: `./gradlew build`
3. Check all imports are updated

## Checklist

Use this checklist when adapting to the new structure:

- [ ] Updated all imports in source files
- [ ] Updated all imports in test files
- [ ] Moved test files to mirror source structure
- [ ] Using `TodoistClient` instead of direct HTTP calls
- [ ] Using shared models where applicable
- [ ] Tests passing: `./gradlew test`
- [ ] Build successful: `./gradlew build`
- [ ] Documentation updated
- [ ] Code follows new patterns

## Getting Help

If you encounter issues:

1. **Check Documentation**:

   - `ARCHITECTURE.md` - Architecture overview
   - `REFACTORING.md` - Detailed refactoring guide
   - `README.md` - Updated usage guide

2. **Review Examples**:

   - Look at existing tasks in the new structure
   - Check test files for patterns

3. **Ask Questions**:
   - Open an issue on GitHub
   - Contact the maintainers

## Summary

The refactoring improves:

- ✅ Code organization
- ✅ Maintainability
- ✅ Scalability
- ✅ Developer experience

With **zero breaking changes** for workflow users!
