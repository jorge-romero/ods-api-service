package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

public class PlatformMother {

    public static Platform of() {
        return new Platform();
    }

    public static Platform of(String id, String label) {
        return Platform.builder()
                .id(id)
                .label(label)
                .url("http://www.example.com/" + id)
                .abbreviation("ABR" + id.toUpperCase())
                .build();
    }
}
