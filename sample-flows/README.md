# Todoist Plugin Sample Flows

This directory contains sample flows demonstrating various use cases for the Kestra Todoist plugin.

## Prerequisites

1. Kestra is running at http://localhost:8080
2. Secret `TODOIST_API_TOKEN` is configured
3. Todoist plugin is loaded

## Sample Flows

### 01 - Create Simple Task

**File:** `01-create-simple-task.yaml`

Basic example of creating a single task in Todoist.

**Use case:** Quick task creation from workflows

---

### 02 - List Tasks

**File:** `02-list-today-tasks.yaml`

Lists all tasks from Todoist and logs the count.

**Use case:** Task overview, monitoring workload

---

### 03 - Create and Complete

**File:** `03-create-and-complete.yaml`

Creates a task and immediately completes it.

**Use case:** Automated task tracking, marking automated work as done

---

### 04 - Daily Standup Tasks

**File:** `04-daily-standup-tasks.yaml`

Creates multiple tasks with different priorities for daily standup.

**Use case:** Daily routine setup, team management

---

### 05 - Scheduled Reminder

**File:** `05-scheduled-reminder.yaml`

Scheduled flow that runs every day at 9 AM to create a reminder task.

**Use case:** Recurring reminders, daily check-ins

**Note:** This flow includes a trigger and will run automatically once enabled.

---

### 06 - Conditional Task Creation

**File:** `06-conditional-task-creation.yaml`

Creates a warning task only if you have too many tasks today.

**Use case:** Workload management, preventing overload

**Features:**

- Input parameter for threshold
- Conditional logic
- Smart notifications

---

### 07 - Get and Update Workflow

**File:** `07-get-and-update-workflow.yaml`

Complete workflow: create → retrieve → update → delete.

**Use case:** Full CRUD demonstration, task lifecycle management

**Features:**

- Custom input
- Task retrieval
- Task updates
- Task deletion
- Detailed logging

---

### 08 - Batch Task Creation

**File:** `08-batch-task-creation.yaml`

Creates multiple tasks from a list using loops.

**Use case:** Bulk task creation, project setup, sprint planning

**Features:**

- Array input
- EachSequential loop
- Batch processing

---

### 09 - Update and Delete Task

**File:** `09-update-and-delete-task.yaml`

Demonstrates updating task properties and deleting tasks.

**Use case:** Task maintenance, cleanup workflows

**Features:**

- Multiple updates to same task
- Priority and due date updates
- Content and description updates
- Task deletion

---

## How to Use

### Method 1: Via Kestra UI

1. Open http://localhost:8080
2. Go to **Flows** → **Create**
3. Copy content from any sample flow file
4. Click **Save**
5. Click **Execute**

### Method 2: Via API

```bash
# Create a flow
curl -X POST http://localhost:8080/api/v1/flows \
  -H "Content-Type: application/yaml" \
  --data-binary @sample-flows/01-create-simple-task.yaml

# Execute the flow
curl -X POST http://localhost:8080/api/v1/executions/demo.todoist/create-simple-task
```

### Method 3: Via Kestra CLI

```bash
# Create flow
kestra flow create sample-flows/01-create-simple-task.yaml

# Execute flow
kestra flow execute demo.todoist create-simple-task
```

## Customization Tips

### Change Priority

```yaml
priority: 1  # Normal
priority: 2  # Medium
priority: 3  # High
priority: 4  # Urgent
```

### Due Date Examples

```yaml
dueString: "today"
dueString: "tomorrow"
dueString: "next Monday"
dueString: "2025-12-31"
dueString: "today at 15:00"
```

### Project Filter Example

```yaml
projectId: "2203306141" # Filter by specific project
```

## Common Patterns

### Using Task Outputs

```yaml
- id: create
  type: io.kestra.plugin.todoist.CreateTask
  # ... config ...

- id: use_output
  type: io.kestra.core.tasks.log.Log
  message: "Task ID: {{ outputs.create.taskId }}"
```

### Using Inputs

```yaml
inputs:
  - id: task_name
    type: STRING
    defaults: "My Task"

tasks:
  - id: create
    type: io.kestra.plugin.todoist.CreateTask
    content: "{{ inputs.task_name }}"
```

### Error Handling

```yaml
- id: create_task
  type: io.kestra.plugin.todoist.CreateTask
  # ... config ...
  retry:
    type: constant
    interval: PT30S
    maxAttempt: 3
```

## Next Steps

1. Start with simple flows (01-03)
2. Try scheduled flows (05)
3. Experiment with conditional logic (06)
4. Build complex workflows (07-08)
5. Create your own custom flows!

## Troubleshooting

**Flow fails with "Failed to create task":**

- Check if `TODOIST_API_TOKEN` secret is set correctly
- Verify your API token at https://todoist.com/app/settings/integrations/developer

**Plugin not found:**

- Ensure plugin JAR is in `build/libs/`
- Restart Kestra: `docker compose restart`

**Secret not working:**

- Check `.env_encoded` file exists
- Verify `docker-compose.yml` has `env_file: - .env_encoded`
- Restart containers: `docker compose down && docker compose up -d`
