package org.opendevstack.apiservice.project.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.project.model.Component;
import org.opendevstack.apiservice.project.model.CreateComponentRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.opendevstack.apiservice.project.util.TestObjectsBuilder.buildTestComponent;
import static org.opendevstack.apiservice.project.util.TestObjectsBuilder.buildTestCreateComponentRequest;

@ExtendWith(MockitoExtension.class)
class ComponentsServiceTest {

    @Mock
    private ComponentsFacade.MarketplaceExternalServicePlaceholder marketPlaceExternalServicePlaceholder;

    private ComponentsFacade componentsFacade;

    @BeforeEach
    void setup() {
        componentsFacade = new ComponentsFacade(marketPlaceExternalServicePlaceholder);
    }

    @Test
    void testGetProjectComponent_whenSuccess_thenReturnCorrectComponent() throws Exception {
        Component testComponent = buildTestComponent();

        when(marketPlaceExternalServicePlaceholder.getProjectComponent(anyString(), eq("testId")))
                .thenReturn(testComponent);

        Component retrievedComponent = componentsFacade.getProjectComponent("testId", "testId");
        assertThat(retrievedComponent).isEqualTo(testComponent);
    }

    @Test
    void testGetProjectComponent_whenNoComponentFound_thenReturnNull() throws Exception {
        when(marketPlaceExternalServicePlaceholder.getProjectComponent(anyString(), eq("testId")))
                .thenReturn(null);

        Component retrievedComponent = componentsFacade.getProjectComponent("testId", "testId");
        assertThat(retrievedComponent).isNull();
    }

    @Test
    void testCreateProjectComponent_whenSuccess_thenReturnCorrectComponent() throws Exception {
        Component testComponent = buildTestComponent();
        CreateComponentRequest testRequest = buildTestCreateComponentRequest();

        when(marketPlaceExternalServicePlaceholder.createProjectComponent(anyString(), eq(testRequest)))
                .thenReturn(testComponent);

        Component retrievedComponent = componentsFacade.createProjectComponent("testId", testRequest);
        assertThat(retrievedComponent).isEqualTo(testComponent);
    }


    @Test
    void testCreateProjectComponent_whenFailure_thenReturnNull() throws Exception {
        CreateComponentRequest testRequest = buildTestCreateComponentRequest();

        when(marketPlaceExternalServicePlaceholder.createProjectComponent(anyString(), eq(testRequest)))
                .thenReturn(null);

        Component retrievedComponent = componentsFacade.createProjectComponent("testId", testRequest);
        assertThat(retrievedComponent).isNull();
    }
}