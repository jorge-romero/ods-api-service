package org.opendevstack.apiservice.project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.opendevstack.apiservice.project.facade.impl.ProjectCreationCommand;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.ProjectRequest;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.Status;

import java.text.MessageFormat;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectRequest toServiceRequest(CreateProjectRequest apiRequest);

    ProjectRequest toServiceRequest(ProjectCreationCommand command);

    @Mapping(source = "status", target = "status", qualifiedByName = "mapStatus")
    @Mapping(source = "projectKey", target = "location", qualifiedByName = "mapLocation")
    @Mapping(source = ".", target = "errorDescription", qualifiedByName = "mapErrorDescription")
    CreateProjectResponse toApiResponse(ProjectResponse serviceResponse);

    @Named("mapStatus")
    default String mapStatus(Status status) {
        if (status == null) {
            return null;
        }
        return status.getDbValue();
    }

    @Named("mapErrorDescription")
    default String mapErrorDescription(ProjectResponse serviceResponse) {
        return (serviceResponse.getStatus() == Status.FAILED) 
                ? MessageFormat.format(
                "There was an error when creating the project {0}.\n\n " +
                        "The error has been reported to our Support team as an incident. " +
                        "You will be informed about the incident via email.", serviceResponse.getProjectKey()) 
                : null;
    }

    @Named("mapLocation")
    default String mapLocation(String projectKey) {
        if (projectKey == null || projectKey.isEmpty()) {
            return null;
        }
        return "/api/pub/v0/projects/" + projectKey;
    }
}
