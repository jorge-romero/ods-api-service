package org.opendevstack.apiservice.externalservice.uipath.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Generic wrapper for UIPath OData responses.
 * UIPath Orchestrator uses OData protocol for querying entities.
 *
 * @param <T> The type of entity in the response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UiPathODataResponse<T> {
    @JsonProperty("@odata.context")
    private String context;
    @JsonProperty("@odata.count")
    private Integer count;
    @JsonProperty("value")
    private List<T> value;

    public UiPathODataResponse() {}
}
