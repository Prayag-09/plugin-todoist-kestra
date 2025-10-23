# Todoist Plugin Architecture

## Overview

The Kestra Todoist plugin follows a clean, modular architecture that separates concerns and promotes code reusability. This document describes the architectural decisions and design patterns used.

## Directory Structure

```
src/main/java/io/kestra/plugin/todoist/
│
├── client/                          # HTTP Client Layer
│   ├── TodoistClient.java          # Centralized API client
│   └── TodoistApiException.java    # Custom exception for API errors
│
├── common/                          # Common Base Classes
│   └── AbstractTodoistTask.java    # Base class for all tasks
│
├── models/                          # Data Transfer Objects
│   └── TaskOutput.java             # Shared output model
│
├── tasks/                           # Task Implementations
│   ├── create/                     # Creation operations
│   │   └── CreateTask.java
│   ├── read/                       # Read operations
│   │   ├── GetTask.java
│   │   └── ListTasks.java
│   ├── update/                     # Update operations
│   │   ├── CompleteTask.java
│   │   └── UpdateTask.java
│   └── delete/                     # Delete operations
│       └── DeleteTask.java
│
└── package-info.java               # Plugin metadata
```

## Architectural Layers

### 1. Client Layer (`client/`)

**Purpose**: Abstracts HTTP communication with the Todoist API.

**Key Components**:

- `TodoistClient`: Handles all HTTP requests (GET, POST, DELETE)
- `TodoistApiException`: Custom exception for API errors

**Benefits**:

- Single source of truth for API communication
- Consistent error handling
- Easy to mock for testing
- Simplified request/response handling

**Example Usage**:

```java
TodoistClient client = new TodoistClient(runContext, apiToken, baseUrl);
Map<String, Object> task = client.get("/tasks/123");
```

### 2. Common Layer (`common/`)

**Purpose**: Provides shared base classes and utilities.

**Key Components**:

- `AbstractTodoistTask`: Base class with common properties (apiToken, BASE_URL)

**Benefits**:

- Reduces code duplication
- Enforces consistent task structure
- Centralizes common configuration

### 3. Models Layer (`models/`)

**Purpose**: Defines data transfer objects and output models.

**Key Components**:

- `TaskOutput`: Shared output model for task operations

**Benefits**:

- Type safety
- Consistent output structure
- Reusability across multiple tasks

### 4. Tasks Layer (`tasks/`)

**Purpose**: Implements all task operations, organized by CRUD pattern.

**Organization**:

- `create/`: Task creation operations
- `read/`: Task retrieval operations (get, list)
- `update/`: Task modification operations (update, complete)
- `delete/`: Task deletion operations

**Benefits**:

- Clear separation of concerns
- Easy to locate specific functionality
- Intuitive organization for new developers
- Scalable for future additions

## Design Patterns

### 1. Template Method Pattern

`AbstractTodoistTask` provides the template for all tasks:

```java
public abstract class AbstractTodoistTask extends Task {
    @NotNull
    protected Property<String> apiToken;
    protected static final String BASE_URL = "https://api.todoist.com/rest/v2";
}
```

### 2. Builder Pattern

All tasks use Lombok's `@SuperBuilder` for fluent construction:

```java
CreateTask task = CreateTask.builder()
    .apiToken(Property.of(token))
    .content(Property.of("My task"))
    .priority(Property.of(4))
    .build();
```

### 3. Facade Pattern

`TodoistClient` acts as a facade for HTTP operations:

```java
public class TodoistClient {
    public Map<String, Object> post(String endpoint, Map<String, Object> body)
    public Map<String, Object> get(String endpoint)
    public List<Map<String, Object>> getList(String endpoint)
    public void delete(String endpoint)
    public void postVoid(String endpoint)
}
```

## Error Handling Strategy

### Centralized Error Handling

All API errors are handled in `TodoistClient`:

```java
if (response.getStatus().getCode() >= 400) {
    throw new TodoistApiException("API request failed: " +
        response.getStatus().getCode() + " - " + response.getBody());
}
```

### Benefits:

- Consistent error messages
- Single point for error handling logic
- Easy to add retry logic or circuit breakers

## Testing Strategy

### Test Organization

Tests mirror the main source structure:

```
src/test/java/io/kestra/plugin/todoist/tasks/
├── create/CreateTaskTest.java
├── read/GetTaskTest.java
├── read/ListTasksTest.java
├── update/UpdateTaskTest.java
├── update/CompleteTaskTest.java
└── delete/DeleteTaskTest.java
```

### Test Approach

1. **Integration Tests**: Tests use real API calls (requires API token)
2. **Graceful Skipping**: Tests skip if API token not available
3. **Cleanup**: Tests clean up created resources

## Scalability

### Adding New Features

The architecture makes it easy to add new features:

#### 1. New Task Type

```
tasks/
└── projects/
    ├── CreateProject.java
    ├── ListProjects.java
    └── DeleteProject.java
```

#### 2. New Client Method

```java
public class TodoistClient {
    public Map<String, Object> patch(String endpoint, Map<String, Object> body) {
        // Implementation
    }
}
```

#### 3. New Model

```
models/
├── TaskOutput.java
├── ProjectOutput.java
└── LabelOutput.java
```

## Best Practices

### 1. Separation of Concerns

- Each class has a single responsibility
- HTTP logic in client layer
- Business logic in task layer
- Data structures in models layer

### 2. DRY (Don't Repeat Yourself)

- Shared code in base classes
- Reusable models
- Centralized client

### 3. SOLID Principles

- **Single Responsibility**: Each class has one job
- **Open/Closed**: Easy to extend, hard to break
- **Liskov Substitution**: All tasks extend AbstractTodoistTask
- **Interface Segregation**: Clean, focused interfaces
- **Dependency Inversion**: Depend on abstractions (RunContext, Property)

### 4. Clean Code

- Descriptive names
- Small, focused methods
- Consistent formatting
- Comprehensive documentation

## Performance Considerations

### HTTP Client Reuse

The `TodoistClient` is created per task execution, which is appropriate for Kestra's execution model.

### JSON Serialization

Uses Kestra's `JacksonMapper` for efficient JSON processing.

### Resource Management

HTTP clients are properly managed by Kestra's `HttpClient.builder()`.

## Security Considerations

### API Token Handling

- Tokens passed as `Property<String>` for secure rendering
- Never logged or exposed in error messages
- Supports Kestra's secret management

### Input Validation

- `@NotNull` annotations for required fields
- Validation in task execution
- Type-safe property rendering

## Future Enhancements

### Potential Additions

1. **Caching Layer**: Add caching for frequently accessed data
2. **Batch Operations**: Support bulk task operations
3. **Webhooks**: Add webhook support for real-time updates
4. **Rate Limiting**: Implement rate limiting for API calls
5. **Retry Logic**: Add automatic retry for transient failures
6. **Metrics**: Add performance metrics and monitoring

### Extensibility Points

The architecture supports:

- Custom authentication methods
- Alternative HTTP clients
- Custom serialization formats
- Plugin-specific middleware
- Custom error handling strategies

## Conclusion

This architecture provides:

- ✅ Clean separation of concerns
- ✅ High maintainability
- ✅ Easy testability
- ✅ Good scalability
- ✅ Clear organization
- ✅ Type safety
- ✅ Reusability

The modular design makes it easy for developers to understand, extend, and maintain the plugin.
