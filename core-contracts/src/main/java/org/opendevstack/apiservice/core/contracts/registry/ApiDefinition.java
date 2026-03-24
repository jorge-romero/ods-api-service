package org.opendevstack.apiservice.core.contracts.registry;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opendevstack.apiservice.core.contracts.auth.AuthType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiDefinition {

    private String id;
    private String name;
    private String basePath;
    private String version;
    private Set<AuthType> authTypes;
    private boolean isPublic;
    private String proxyUrl;
    private boolean enabled;

    public boolean isLocal() {
        return proxyUrl == null || proxyUrl.isBlank();
    }

    public boolean requiresAuth() {
        return authTypes != null
            && !authTypes.isEmpty()
            && !(authTypes.size() == 1 && authTypes.contains(AuthType.NONE));
    }
}
