package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

public class TestingHubProjectMother {
    public static TestingHubProject of(String key, String id) {
        return new TestingHubProject(id, key);
    }
}
