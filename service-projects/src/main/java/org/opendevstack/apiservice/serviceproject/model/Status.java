package org.opendevstack.apiservice.serviceproject.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {

    PENDING("Pending"),
    RUNNING("Running"),
    FAILED("Failed");

    private final String dbValue;
}
