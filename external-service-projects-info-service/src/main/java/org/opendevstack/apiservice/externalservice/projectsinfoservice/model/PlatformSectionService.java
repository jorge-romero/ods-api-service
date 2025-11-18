package org.opendevstack.apiservice.externalservice.projectsinfoservice.model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PlatformSectionService {
    private String section;
    private String tooltip;
    private List<PlatformLink> links;
}