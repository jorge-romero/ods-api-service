package org.opendevstack.apiservice.externalservice.webhookproxy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an environment variable pair for webhook proxy build requests.
 * Maps to the EnvPair structure in the webhook proxy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvPair {
    
    /**
     * Name of the environment variable
     */
    private String name;
    
    /**
     * Value of the environment variable
     */
    private String value;
}
