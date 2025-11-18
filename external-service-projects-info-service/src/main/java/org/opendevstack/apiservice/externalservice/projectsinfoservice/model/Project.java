package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {
    private Metadata metadata;
}