package org.opendevstack.apiservice.externalservice.projectsinfoservice.dto;

public class LinkMother {

    public static Link of() {
        return of("label", "https://www.example.com", "general", "some info");
    }

    public static Link of(String label, String url, String type, String tooltip) {
            return Link.builder()
                    .label(label)
                    .url(url)
                    .type(type)
                    .tooltip(tooltip)
                    .build();
    }
}
