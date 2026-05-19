package org.opendevstack.apiservice.project.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItem;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemUserAction;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.CatalogItemUserActionParameter;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisionActionParameter;
import org.opendevstack.apiservice.externalservice.marketplace.client.model.ProvisioningStatusUpdateRequestAllOfParameters;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MarketplaceMapperTest {

    private final MarketplaceMapper mapper = Mappers.getMapper(MarketplaceMapper.class);

    @Test
    void mapCreateComponentRequestToCreateComponentParameterList_resolvesTypeFromCatalogUserActions() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("test-amp-test-four");
        request.setProductId("catest_external-lists-item");

        List<String> projectGroup = List.of(
                "CN=BI-AS-ATLASSIAN-P-EDPI-STAKEHOLDER,OU=BIDS-managed,DC=eu,DC=boehringer,DC=com",
                "CN=BI-AS-ATLASSIAN-P-EDPI-MANAGER,OU=BIDS-managed,DC=eu,DC=boehringer,DC=com");
        request.setParams(Map.of(
                "requestor_email", "user@example.com",
                "Project_Group", projectGroup,
                "enable_feature", true
        ));

        CatalogItem catalogItem = new CatalogItem();
        catalogItem.setUserActions(List.of(buildProvisionAction(
                param("requestor_email", "string"),
                param("Project_Group", "multiplelist"),
                param("enable_feature", "boolean")
        )));

        List<ProvisionActionParameter> result =
                mapper.mapCreateComponentRequestToCreateComponentParameterList(request, catalogItem);

        assertThat(result).extracting(ProvisionActionParameter::getName)
                .containsExactlyInAnyOrder("component_id", "catalog_item_slug",
                        "requestor_email", "Project_Group", "enable_feature");

        ProvisionActionParameter projectGroupParam = findByName(result, "Project_Group");
        assertThat(projectGroupParam.getType()).isEqualTo("multiplelist");
        assertThat(projectGroupParam.getValue()).isEqualTo(projectGroup);

        ProvisionActionParameter emailParam = findByName(result, "requestor_email");
        assertThat(emailParam.getType()).isEqualTo("string");
        assertThat(emailParam.getValue()).isEqualTo("user@example.com");

        ProvisionActionParameter booleanParam = findByName(result, "enable_feature");
        assertThat(booleanParam.getType()).isEqualTo("boolean");
        assertThat(booleanParam.getValue()).isEqualTo(true);

        ProvisionActionParameter componentId = findByName(result, "component_id");
        assertThat(componentId.getType()).isEqualTo("string");
        assertThat(componentId.getValue()).isEqualTo("test-amp-test-four");

        ProvisionActionParameter slug = findByName(result, "catalog_item_slug");
        assertThat(slug.getType()).isEqualTo("string");
        assertThat(slug.getValue()).isEqualTo("catest_external-lists-item");
    }

    @Test
    void mapCreateComponentRequestToCreateComponentParameterList_fallsBackToStringWhenCatalogItemMissing() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("test-component");
        request.setProductId("some-slug");
        request.setParams(Map.of("anything", "value"));

        List<ProvisionActionParameter> result =
                mapper.mapCreateComponentRequestToCreateComponentParameterList(request, null);

        ProvisionActionParameter anything = findByName(result, "anything");
        assertThat(anything.getType()).isEqualTo("string");
        assertThat(anything.getValue()).isEqualTo("value");
    }

    @Test
    void mapCreateComponentRequestToCreateComponentParameterList_returnsEmptyWhenRequestNull() {
        assertThat(mapper.mapCreateComponentRequestToCreateComponentParameterList(null, null)).isEmpty();
    }

    // -------------------------------------------------------------------------
    // mapCreateComponentRequestToRegisterComponentParameterList
    // -------------------------------------------------------------------------

    @Test
    void mapCreateComponentRequestToRegisterComponentParameterList_maps_string_values_correctly() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("test-component");
        request.setProductId("some-slug");
        request.setParams(Map.of("my_param", "my_value"));

        List<ProvisioningStatusUpdateRequestAllOfParameters> result =
                mapper.mapCreateComponentRequestToRegisterComponentParameterList(request);

        assertThat(result).hasSize(1);
        ProvisioningStatusUpdateRequestAllOfParameters param =
                result.stream().filter(p -> "my_param".equals(p.getName())).findFirst().orElseThrow();
        assertThat(param.getValues()).containsExactly("my_value");
    }

    @Test
    void mapCreateComponentRequestToRegisterComponentParameterList_maps_list_values_correctly() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("test-component");
        request.setProductId("some-slug");
        List<String> listValue = List.of("value1", "value2", "value3");
        request.setParams(Map.of("list_param", listValue));

        List<ProvisioningStatusUpdateRequestAllOfParameters> result =
                mapper.mapCreateComponentRequestToRegisterComponentParameterList(request);

        assertThat(result).hasSize(1);
        ProvisioningStatusUpdateRequestAllOfParameters param =
                result.stream().filter(p -> "list_param".equals(p.getName())).findFirst().orElseThrow();
        assertThat(param.getValues()).containsExactlyInAnyOrder("value1", "value2", "value3");
    }

    @Test
    void mapCreateComponentRequestToRegisterComponentParameterList_maps_non_string_value_to_string() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("test-component");
        request.setProductId("some-slug");
        request.setParams(Map.of("bool_param", true, "int_param", 42));

        List<ProvisioningStatusUpdateRequestAllOfParameters> result =
                mapper.mapCreateComponentRequestToRegisterComponentParameterList(request);

        assertThat(result).hasSize(2);
        ProvisioningStatusUpdateRequestAllOfParameters boolParam =
                result.stream().filter(p -> "bool_param".equals(p.getName())).findFirst().orElseThrow();
        assertThat(boolParam.getValues()).containsExactly("true");

        ProvisioningStatusUpdateRequestAllOfParameters intParam =
                result.stream().filter(p -> "int_param".equals(p.getName())).findFirst().orElseThrow();
        assertThat(intParam.getValues()).containsExactly("42");
    }

    @Test
    void mapCreateComponentRequestToRegisterComponentParameterList_maps_null_value_to_empty_string_list() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("test-component");
        request.setProductId("some-slug");
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("null_param", null);
        request.setParams(params);

        List<ProvisioningStatusUpdateRequestAllOfParameters> result =
                mapper.mapCreateComponentRequestToRegisterComponentParameterList(request);

        assertThat(result).hasSize(1);
        ProvisioningStatusUpdateRequestAllOfParameters param =
                result.stream().filter(p -> "null_param".equals(p.getName())).findFirst().orElseThrow();
        assertThat(param.getValues()).containsExactly("");
    }

    @Test
    void mapCreateComponentRequestToRegisterComponentParameterList_returns_empty_list_when_params_null() {
        CreateComponentRequest request = new CreateComponentRequest();
        request.setName("test-component");
        request.setProductId("some-slug");
        request.setParams(null);

        List<ProvisioningStatusUpdateRequestAllOfParameters> result =
                mapper.mapCreateComponentRequestToRegisterComponentParameterList(request);

        assertThat(result).isEmpty();
    }

    private static CatalogItemUserAction buildProvisionAction(CatalogItemUserActionParameter... params) {
        CatalogItemUserAction action = new CatalogItemUserAction();
        action.setId("PROVISION");
        action.setParameters(List.of(params));
        return action;
    }

    private static CatalogItemUserActionParameter param(String name, String type) {
        CatalogItemUserActionParameter p = new CatalogItemUserActionParameter();
        p.setName(name);
        p.setType(type);
        return p;
    }

    private static ProvisionActionParameter findByName(List<ProvisionActionParameter> list, String name) {
        return list.stream().filter(p -> name.equals(p.getName())).findFirst().orElseThrow();
    }
}
