package org.opendevstack.apiservice.externalservice.webhookproxy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from the webhook proxy build trigger.
 * Contains information about the triggered build.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookProxyBuildResponse {
    
    /**
     * HTTP status code from the response
     */
    private int statusCode;
    
    /**
     * Response body (typically from OpenShift BuildConfig webhook)
     */
    private String body;
    
    /**
     * Indicates if the build was triggered successfully
     */
    private boolean success;
    
    /**
     * Error message if the build trigger failed
     */
    private String errorMessage;
}
