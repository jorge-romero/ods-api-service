package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PlatformLink {
    private String label;
    private String url;
    private String type;
    private String tooltip;
}