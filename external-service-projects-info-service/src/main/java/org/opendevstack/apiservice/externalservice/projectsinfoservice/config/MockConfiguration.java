package org.opendevstack.apiservice.externalservice.projectsinfoservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Getter
@Setter
public class MockConfiguration {

    @Value("#{'${externalservices.projects-info-service.mock.clusters}'.split(',')}")
    private List<String> clusters;

    @Value("#{'${externalservices.projects-info-service.mock.projects.default}'.split(',')}")
    private List<String> defaultProjects;

    @Value("${externalservices.projects-info-service.mock.projects.users}")
    private String usersProjects;
}
