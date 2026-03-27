package org.opendevstack.apiservice.project.validation;

import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.springframework.stereotype.Component;

@Component
public class ProjectRequestValidator {
    
    public void validate(CreateProjectRequest request) {
        validateFlavorOrConfigItem(request);
    }
    
    private void validateFlavorOrConfigItem(CreateProjectRequest request) {
        String projectFlavor = request.getProjectFlavor();
        String configurationItem = request.getConfigurationItem();
        
        boolean hasFlavor = projectFlavor != null && !projectFlavor.trim().isEmpty();
        boolean hasConfigItem = configurationItem != null && !configurationItem.trim().isEmpty();
        
        if (!hasFlavor && !hasConfigItem) {
            throw new ProjectValidationException(
                ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM
            );
        }
    }
}
