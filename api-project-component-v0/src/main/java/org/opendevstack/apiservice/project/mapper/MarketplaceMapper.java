package org.opendevstack.apiservice.project.mapper;


import org.mapstruct.Mapper;
import org.opendevstack.apiservice.externalservice.marketplace.model.CreateComponentParameter;
import org.opendevstack.apiservice.externalservice.marketplace.model.ProjectComponent;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MarketplaceMapper {

    default Component mapMarketplaceComponentToV0Component(ProjectComponent source) {
        if (source == null) {
            return null;
        }
        Component target = new Component();
        target.setId(source.getComponentId());
        target.setStatus(source.getStatus());
        return target;
    }

    default List<CreateComponentParameter> mapCreateComponentRequestToCreateComponentParameterList(CreateComponentRequest createComponentRequest) {
        return createComponentRequest.getParams().entrySet().stream()
                .map(entry -> new CreateComponentParameter(entry.getKey(), "string", entry.getValue()))
                .toList();
    }
}
