package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

public class PlatformLinkMother {

    public static PlatformLink of() {
        return PlatformLink.builder()
                .label("label")
                .url("https://google.com")
                .type("general")
                .tooltip("Some help here")
                .build();
    }

    public static PlatformLink of(String label, String url, String type, String tooltip) {
        return PlatformLink.builder()
                .label(label)
                .url(url)
                .type(type)
                .tooltip(tooltip)
                .build();
    }
}
