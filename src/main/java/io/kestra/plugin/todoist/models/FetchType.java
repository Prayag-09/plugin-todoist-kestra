package io.kestra.plugin.todoist.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    title = "Fetch Type",
    description = "Defines how data should be fetched and returned"
)
public enum FetchType {
    @Schema(description = "Fetch all items and return them in the output")
    FETCH,
    
    @Schema(description = "Fetch all items but return only the first one")
    FETCH_ONE,
    
    @Schema(description = "Fetch all items and store them in internal storage, returning a URI")
    STORE
}
