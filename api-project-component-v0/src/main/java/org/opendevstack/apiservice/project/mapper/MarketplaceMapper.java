package org.opendevstack.apiservice.project.mapper;

import org.mapstruct.Mapper;
import org.opendevstack.apiservice.externalservice.marketplace.exception.MarketplaceException;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemUserAction;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemUserActionParameter;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProjectComponentExtendedInfo;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisionActionParameter;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisioningStatusUpdateRequestAllOfParameters;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;
import org.opendevstack.apiservice.project.model.EnvironmentsDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface MarketplaceMapper {

    String DEFAULT_PARAMETER_TYPE = "string";

    default Component mapMarketplaceComponentToV0Component(ProjectComponentExtendedInfo source, CatalogItem catalogItem) throws MarketplaceException {
        Component component = new Component();
        component.setId(source.getComponentId());
        component.setEnvironment(EnvironmentsDTO.DEV); // Env is always DEV so we hardcode it as such
        component.setRepositoryURL(source.getComponentUrl());
        component.setComponentType(""); // We agreed to hardcode the type as empty
        component.setStatus(StatusMap.toOldStatus(source.getStatus()));
        if (component.getStatus() == null) {
            throw new MarketplaceException("No status mapping found for status " + source.getStatus());
        }
        if (catalogItem != null) {
            component.setProductId(catalogItem.getId());
            component.setProductName(catalogItem.getTitle());
            component.setProductDescription(catalogItem.getShortDescription());
        }

        if (source.getParameters() != null) {
            source.getParameters().forEach(
                    param -> component.putParamsItem(param.getName(), param.getValues())
            );
        }
        return component;
    }

    default List<ProvisionActionParameter> mapCreateComponentRequestToCreateComponentParameterList(
            CreateComponentRequest createComponentRequest, CatalogItem catalogItem) {
        if (createComponentRequest == null) {
            return List.of();
        }

        Map<String, String> parameterTypesByName = buildParameterTypeIndex(catalogItem);

        List<ProvisionActionParameter> parameters = new ArrayList<>();
        parameters.add(createParameter("component_id", createComponentRequest.getName(), DEFAULT_PARAMETER_TYPE));
        parameters.add(createParameter("catalog_item_slug", createComponentRequest.getProductId(), DEFAULT_PARAMETER_TYPE));

        if (createComponentRequest.getParams() != null && !createComponentRequest.getParams().isEmpty()) {
            createComponentRequest.getParams().forEach((name, value) -> {
                String type = parameterTypesByName.getOrDefault(name, DEFAULT_PARAMETER_TYPE);
                parameters.add(createParameter(name, value, type));
            });
        }

        return parameters;
    }

    default List<ProvisioningStatusUpdateRequestAllOfParameters> mapCreateComponentRequestToRegisterComponentParameterList(
            CreateComponentRequest createComponentRequest) {
        if (createComponentRequest == null) {
            return List.of();
        }

        List<ProvisioningStatusUpdateRequestAllOfParameters> parameters = new ArrayList<>();
        if (createComponentRequest.getParams() != null && !createComponentRequest.getParams().isEmpty()) {
            createComponentRequest.getParams().forEach((name, value) -> {
                parameters.add(createRegisterParameter(name, value));
            });
        }

        return parameters;
    }

    default ProvisionActionParameter createParameter(String name, Object value, String type) {
        return new ProvisionActionParameter().name(name).type(type).value(value);
    }

    default ProvisioningStatusUpdateRequestAllOfParameters createRegisterParameter(String name, Object value) {
        List<String> values;
        if (value == null) {
            values = List.of("");
        } else if (value instanceof List<?> list) {
            values = list.stream()
                    .map(Object::toString)
                    .toList();
        } else {
            values = List.of(value.toString());
        }
        return new ProvisioningStatusUpdateRequestAllOfParameters().name(name).values(values);
    }

    /**
     * Builds a map of parameter name -> type from the catalog item user actions.
     * The type comes from the {@link CatalogItemUserActionParameter#getType()} value
     * (e.g. {@code string}, {@code boolean}, {@code multiplelist}, {@code singlelist}, ...).
     */
    private static Map<String, String> buildParameterTypeIndex(CatalogItem catalogItem) {
        Map<String, String> index = new HashMap<>();
        if (catalogItem == null || catalogItem.getUserActions() == null) {
            return index;
        }
        for (CatalogItemUserAction action : catalogItem.getUserActions()) {
            if (action == null || action.getParameters() == null) {
                continue;
            }
            for (CatalogItemUserActionParameter param : action.getParameters()) {
                if (param == null || param.getName() == null || param.getType() == null) {
                    continue;
                }
                index.putIfAbsent(param.getName(), param.getType());
            }
        }
        return index;
    }
}
