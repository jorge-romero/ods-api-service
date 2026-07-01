package org.opendevstack.apiservice.externalservice.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProjectComponent {

    private UUID componentId;
    private String name;
    private String productDescription;
    private String productName;
    private String productId;
    private String environment;
    private String status;
    private String resultTraceback;
    private String repositoryURL;
    private String componentType;
    private boolean canBeDeleted;
    private String logoUrl;
    private String componentUrl;

}
