package io.kestra.plugin.todoist.client;

public class TodoistApiException extends Exception {
    
    public TodoistApiException(String message) {
        super(message);
    }
    
    public TodoistApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
