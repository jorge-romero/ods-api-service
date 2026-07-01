package org.opendevstack.apiservice.project.validation;

import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectUpdateValidationException;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.UpdateProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ProjectRequestValidator {
    
    @Value("${apis.projects.locations}")
    private List<String> locations;
    
    public void validateCreateRequest(CreateProjectRequest request) {
        validateFlavorOrConfigItem(request);
        validateOwnerIfX2AccountIsPresent(request);
        validateLocation(request);
    }

    public void validateUpdateRequest(UpdateProjectRequest request) {
        validateStatus(request);
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

    private void validateStatus(UpdateProjectRequest request) {
        String status = request.getStatus();

        if (status == null || status.trim().isEmpty()) {
            return;
        }

        List<String> statusList = Arrays.stream(Status.values())
                .map(e -> e.getDbValue())
                .toList();
        if (!statusList.contains(status)) {
            throw new ProjectUpdateValidationException(ErrorKey.INVALID_STATUS,
                String.join(", ", statusList));
        }
    }
}
