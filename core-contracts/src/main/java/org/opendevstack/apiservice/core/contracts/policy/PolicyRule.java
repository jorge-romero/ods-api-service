package org.opendevstack.apiservice.core.contracts.policy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyRule {

    private UUID id;
    private String apiDefinitionId;
    private String clientId;
    private String policyType;
    private Map<String, Object> config;
}
