package org.opendevstack.apiservice.project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.opendevstack.apiservice.project.facade.impl.ProjectCreationCommand;
import org.opendevstack.apiservice.project.model.CreateProjectResponse;
import org.opendevstack.apiservice.serviceproject.model.ProjectResponse;

@Mapper(componentModel = "spring")
public interface ProjectCreationResponseMapper {

    @Mapping(target = "message", constant = "The project creation process has been successfully initiated.")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "httStatus", constant = "OK")
    @Mapping(target = "errorKey", constant = "000")
    @Mapping(target = "projectKey", source = "project.projectKey")
    @Mapping(target = "projectFlavor", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "error", ignore = true)
    @Mapping(target = "errorDescription", ignore = true)
    CreateProjectResponse toSuccessResponse(ProjectCreationCommand command, ProjectResponse project);
}



