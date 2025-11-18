package org.opendevstack.apiservice.externalservice.projectsinfoservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Getter
@Setter
public class ProjectFilterConfiguration {

    @Value("${externalservices.projects-info-service.project.filter.project-roles-group-prefix}")
    private String projectRolesGroupPrefix;

    @Value("#{'${externalservices.projects-info-service.project.filter.project-roles-group-suffixes}'.split(',')}")
    private List<String> projectRolesGroupSuffixes;

}

