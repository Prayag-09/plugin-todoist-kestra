package io.kestra.plugin.todoist.client;

import io.kestra.core.http.HttpRequest;
import io.kestra.core.http.HttpResponse;
import io.kestra.core.http.client.HttpClient;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class TodoistClient {
    
    private final RunContext runContext;
    private final String apiToken;
    private final String baseUrl;
    
    public TodoistClient(RunContext runContext, String apiToken, String baseUrl) {
        this.runContext = runContext;
        this.apiToken = apiToken;
        this.baseUrl = baseUrl;
    }
    
    public TodoistClient(RunContext runContext, String apiToken) {
        this(runContext, apiToken, "https://api.todoist.com/rest/v2");
    }
    
    private HttpRequest.HttpRequestBuilder createRequestBuilder(String url) {
        return HttpRequest.builder()
            .uri(URI.create(url))
            .addHeader("Authorization", "Bearer " + apiToken)
            .addHeader("Content-Type", "application/json");
    }
    
    private HttpResponse<String> sendRequest(HttpRequest request) throws Exception {
        HttpClient client = HttpClient.builder()
            .runContext(runContext)
            .build();
        return client.request(request, String.class);
    }
    
    public Map<String, Object> post(String endpoint, Map<String, Object> body) throws Exception {
        String jsonBody = JacksonMapper.ofJson().writeValueAsString(body);
        
        HttpRequest request = createRequestBuilder(baseUrl + endpoint)
            .method("POST")
            .body(HttpRequest.StringRequestBody.builder().content(jsonBody).build())
            .build();
        
        HttpResponse<String> response = sendRequest(request);
        
        if (response.getStatus().getCode() >= 400) {
            throw new TodoistApiException("API request failed: " + response.getStatus().getCode() + " - " + response.getBody());
        }
        
        return JacksonMapper.ofJson().readValue(response.getBody(), Map.class);
    }
    
    public Map<String, Object> get(String endpoint) throws Exception {
        HttpRequest request = createRequestBuilder(baseUrl + endpoint)
            .method("GET")
            .build();
        
        HttpResponse<String> response = sendRequest(request);
        
        if (response.getStatus().getCode() >= 400) {
            throw new TodoistApiException("API request failed: " + response.getStatus().getCode() + " - " + response.getBody());
        }
        
        return JacksonMapper.ofJson().readValue(response.getBody(), Map.class);
    }
    
    public List<Map<String, Object>> getList(String endpoint) throws Exception {
        HttpRequest request = createRequestBuilder(baseUrl + endpoint)
            .method("GET")
            .build();
        
        HttpResponse<String> response = sendRequest(request);
        
        if (response.getStatus().getCode() >= 400) {
            throw new TodoistApiException("API request failed: " + response.getStatus().getCode() + " - " + response.getBody());
        }
        
        return JacksonMapper.ofJson().readValue(response.getBody(), List.class);
    }
    
    public void delete(String endpoint) throws Exception {
        HttpRequest request = createRequestBuilder(baseUrl + endpoint)
            .method("DELETE")
            .build();
        
        HttpResponse<String> response = sendRequest(request);
        
        if (response.getStatus().getCode() >= 400) {
            throw new TodoistApiException("API request failed: " + response.getStatus().getCode() + " - " + response.getBody());
        }
    }
    
    public void postVoid(String endpoint) throws Exception {
        HttpRequest request = createRequestBuilder(baseUrl + endpoint)
            .method("POST")
            .body(HttpRequest.StringRequestBody.builder().content("").build())
            .build();
        
        HttpResponse<String> response = sendRequest(request);
        
        if (response.getStatus().getCode() >= 400) {
            throw new TodoistApiException("API request failed: " + response.getStatus().getCode() + " - " + response.getBody());
        }
    }
}
