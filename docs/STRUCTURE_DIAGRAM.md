# Plugin Structure Diagram

## Visual Overview

```
plugin-todoist/
│
├── src/main/java/io/kestra/plugin/todoist/
│   │
│   ├── 📦 client/                    [HTTP Communication Layer]
│   │   ├── TodoistClient.java       → Centralized API client
│   │   └── TodoistApiException.java → Custom exception handling
│   │
│   ├── 📦 common/                    [Shared Base Classes]
│   │   └── AbstractTodoistTask.java → Base class for all tasks
│   │
│   ├── 📦 models/                    [Data Transfer Objects]
│   │   └── TaskOutput.java          → Shared output model
│   │
│   ├── 📦 tasks/                     [Task Implementations]
│   │   │
│   │   ├── 📁 create/               [Creation Operations]
│   │   │   └── CreateTask.java      → Create new tasks
│   │   │
│   │   ├── 📁 read/                 [Retrieval Operations]
│   │   │   ├── GetTask.java         → Get single task
│   │   │   └── ListTasks.java       → List multiple tasks
│   │   │
│   │   ├── 📁 update/               [Modification Operations]
│   │   │   ├── UpdateTask.java      → Update task properties
│   │   │   └── CompleteTask.java    → Mark task complete
│   │   │
│   │   └── 📁 delete/               [Deletion Operations]
│   │       └── DeleteTask.java      → Delete tasks
│   │
│   └── package-info.java            → Plugin metadata
│
└── src/test/java/io/kestra/plugin/todoist/tasks/
    │
    ├── 📁 create/
    │   └── CreateTaskTest.java
    │
    ├── 📁 read/
    │   ├── GetTaskTest.java
    │   └── ListTasksTest.java
    │
    ├── 📁 update/
    │   ├── UpdateTaskTest.java
    │   └── CompleteTaskTest.java
    │
    └── 📁 delete/
        └── DeleteTaskTest.java
```

## Layer Dependencies

```
┌─────────────────────────────────────────────────────────┐
│                    Task Layer                           │
│  (CreateTask, GetTask, ListTasks, UpdateTask, etc.)    │
└────────────────────┬────────────────────────────────────┘
                     │ depends on
                     ↓
┌─────────────────────────────────────────────────────────┐
│                  Common Layer                           │
│            (AbstractTodoistTask)                        │
└────────────────────┬────────────────────────────────────┘
                     │ depends on
                     ↓
┌─────────────────────────────────────────────────────────┐
│                  Client Layer                           │
│         (TodoistClient, TodoistApiException)            │
└────────────────────┬────────────────────────────────────┘
                     │ depends on
                     ↓
┌─────────────────────────────────────────────────────────┐
│                  Kestra Core                            │
│    (RunContext, HttpClient, Property, etc.)            │
└─────────────────────────────────────────────────────────┘
```

## Data Flow

### Example: Creating a Task

```
User Workflow (YAML)
        ↓
┌───────────────────────────────────────┐
│  CreateTask                           │
│  (tasks/create/CreateTask.java)       │
│                                       │
│  1. Validates input                   │
│  2. Renders properties                │
└───────────────┬───────────────────────┘
                ↓
┌───────────────────────────────────────┐
│  AbstractTodoistTask                  │
│  (common/AbstractTodoistTask.java)    │
│                                       │
│  - Provides apiToken                  │
│  - Provides BASE_URL                  │
└───────────────┬───────────────────────┘
                ↓
┌───────────────────────────────────────┐
│  TodoistClient                        │
│  (client/TodoistClient.java)          │
│                                       │
│  1. Builds HTTP request               │
│  2. Adds authentication               │
│  3. Sends request                     │
│  4. Handles errors                    │
│  5. Parses response                   │
└───────────────┬───────────────────────┘
                ↓
┌───────────────────────────────────────┐
│  Kestra HttpClient                    │
│                                       │
│  - Executes HTTP request              │
│  - Returns response                   │
└───────────────┬───────────────────────┘
                ↓
┌───────────────────────────────────────┐
│  Todoist API                          │
│  (https://api.todoist.com/rest/v2)    │
└───────────────┬───────────────────────┘
                ↓
        Response flows back up
                ↓
┌───────────────────────────────────────┐
│  TaskOutput                           │
│  (models/TaskOutput.java)             │
│                                       │
│  - taskId                             │
│  - content                            │
│  - url                                │
└───────────────────────────────────────┘
```

## CRUD Organization

```
tasks/
│
├── CREATE Operations
│   └── CreateTask
│       ├── Input: content, description, priority, etc.
│       ├── HTTP: POST /tasks
│       └── Output: TaskOutput (taskId, content, url)
│
├── READ Operations
│   ├── GetTask
│   │   ├── Input: taskId
│   │   ├── HTTP: GET /tasks/{id}
│   │   └── Output: task object
│   │
│   └── ListTasks
│       ├── Input: projectId (optional)
│       ├── HTTP: GET /tasks
│       └── Output: tasks list, count
│
├── UPDATE Operations
│   ├── UpdateTask
│   │   ├── Input: taskId, content, description, priority, etc.
│   │   ├── HTTP: POST /tasks/{id}
│   │   └── Output: TaskOutput (taskId, content, url)
│   │
│   └── CompleteTask
│       ├── Input: taskId
│       ├── HTTP: POST /tasks/{id}/close
│       └── Output: void
│
└── DELETE Operations
    └── DeleteTask
        ├── Input: taskId
        ├── HTTP: DELETE /tasks/{id}
        └── Output: void
```

## Class Relationships

```
                    ┌──────────────────┐
                    │   Kestra Task    │
                    │   (interface)    │
                    └────────┬─────────┘
                             │
                             │ extends
                             ↓
                    ┌──────────────────┐
                    │ AbstractTodoist  │
                    │      Task        │
                    │                  │
                    │ + apiToken       │
                    │ + BASE_URL       │
                    └────────┬─────────┘
                             │
                ┌────────────┼────────────┐
                │            │            │
                ↓            ↓            ↓
        ┌──────────┐  ┌──────────┐  ┌──────────┐
        │ Create   │  │   Read   │  │  Update  │
        │  Task    │  │   Task   │  │   Task   │
        └──────────┘  └──────────┘  └──────────┘
                │            │            │
                └────────────┼────────────┘
                             │
                             │ uses
                             ↓
                    ┌──────────────────┐
                    │ TodoistClient    │
                    │                  │
                    │ + post()         │
                    │ + get()          │
                    │ + getList()      │
                    │ + delete()       │
                    │ + postVoid()     │
                    └──────────────────┘
```

## Package Dependencies Graph

```
┌─────────────────────────────────────────────────────────┐
│                    io.kestra.plugin.todoist             │
│                                                         │
│  ┌──────────────┐    ┌──────────────┐                 │
│  │   common/    │◄───│   tasks/     │                 │
│  │              │    │              │                 │
│  │ Abstract     │    │ CreateTask   │                 │
│  │ TodoistTask  │    │ GetTask      │                 │
│  └──────┬───────┘    │ ListTasks    │                 │
│         │            │ UpdateTask   │                 │
│         │            │ CompleteTask │                 │
│         │            │ DeleteTask   │                 │
│         │            └──────┬───────┘                 │
│         │                   │                         │
│         │                   │                         │
│         └───────┬───────────┘                         │
│                 │                                     │
│                 ↓                                     │
│         ┌──────────────┐                             │
│         │   client/    │                             │
│         │              │                             │
│         │ TodoistClient│                             │
│         │ TodoistApi   │                             │
│         │ Exception    │                             │
│         └──────────────┘                             │
│                 ↑                                     │
│                 │                                     │
│         ┌──────────────┐                             │
│         │   models/    │                             │
│         │              │                             │
│         │ TaskOutput   │                             │
│         └──────────────┘                             │
└─────────────────────────────────────────────────────────┘
```

## Future Extensibility

```
tasks/
│
├── 📁 create/          [Current]
├── 📁 read/            [Current]
├── 📁 update/          [Current]
├── 📁 delete/          [Current]
│
├── 📁 projects/        [Future]
│   ├── CreateProject
│   ├── GetProject
│   ├── ListProjects
│   └── DeleteProject
│
├── 📁 labels/          [Future]
│   ├── CreateLabel
│   ├── GetLabel
│   ├── ListLabels
│   └── DeleteLabel
│
├── 📁 comments/        [Future]
│   ├── CreateComment
│   ├── GetComment
│   ├── ListComments
│   └── DeleteComment
│
└── 📁 sections/        [Future]
    ├── CreateSection
    ├── GetSection
    ├── ListSections
    └── DeleteSection
```

## Key Design Principles

1. **Separation of Concerns**

   - Each layer has a single responsibility
   - Clear boundaries between layers

2. **DRY (Don't Repeat Yourself)**

   - Shared code in base classes
   - Reusable models and clients

3. **SOLID Principles**

   - Single Responsibility
   - Open/Closed
   - Liskov Substitution
   - Interface Segregation
   - Dependency Inversion

4. **Scalability**

   - Easy to add new features
   - Clear patterns to follow
   - Organized by functionality

5. **Maintainability**
   - Self-documenting structure
   - Easy to locate code
   - Consistent patterns
