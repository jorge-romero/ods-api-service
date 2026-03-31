package org.opendevstack.apiservice.externalservice.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProjectComponent {

    private String componentId;
    private String status;
    private boolean canBeDeleted;
    private String logoUrl;
    private String componentUrl;

}
