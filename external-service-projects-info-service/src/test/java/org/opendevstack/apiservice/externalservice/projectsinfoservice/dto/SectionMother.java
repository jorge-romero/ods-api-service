package org.opendevstack.apiservice.externalservice.projectsinfoservice.dto;

import java.util.List;

public class SectionMother {

    public static Section of() {
        return Section.builder()
                .section("section")
                .tooltip("tooltip")
                .links(List.of(LinkMother.of()))
                .build();
    }
}
