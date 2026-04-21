package org.opendevstack.apiservice.project.validation;

import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectRequestValidator {
    
    @Value("${apis.projects.locations}")
    private List<String> locations;
    
    public void validate(CreateProjectRequest request) {
        validateFlavorOrConfigItem(request);
        validateOwnerIfX2AccountIsPresent(request);
        validateLocation(request);
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
    
    private void validateOwnerIfX2AccountIsPresent(CreateProjectRequest request) {
        String x2Account = request.getX2OdsAccount();
        String owner = request.getOwner();
        
        boolean hasX2Account = x2Account != null && !x2Account.trim().isEmpty();
        boolean hasOwner = owner != null && !owner.trim().isEmpty();
        
        if(hasX2Account && !hasOwner) {
            throw new ProjectValidationException(
                    ErrorKey.MANDATORY_OWNER
            );
        }
    }
    
    private void validateLocation(CreateProjectRequest request) {
        String location = request.getLocation();
        
        if (location == null || location.trim().isEmpty()) {
            return;
        }
        
        if (!locations.contains(location)) {
            throw new ProjectValidationException(ErrorKey.INVALID_LOCATION, 
                String.join(", ", locations));
        }
    }
}
