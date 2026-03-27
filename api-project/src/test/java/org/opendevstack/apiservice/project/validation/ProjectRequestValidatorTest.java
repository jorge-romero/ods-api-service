package org.opendevstack.apiservice.project.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectRequestValidatorTest {

    private ProjectRequestValidator sut;

    @BeforeEach
    void setUp() {
        sut = new ProjectRequestValidator();
    }

    @Test
    void validate_throws_exception_when_project_flavor_and_config_item_both_null() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid Name");
        request.setProjectFlavor(null);
        request.setConfigurationItem(null);

        ProjectValidationException exception = assertThrows(
            ProjectValidationException.class,
            () -> sut.validate(request)
        );

        assertEquals(ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM, exception.getErrorKey());
    }

    @Test
    void validate_throws_exception_when_project_flavor_and_config_item_both_empty() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid Name");
        request.setProjectFlavor("");
        request.setConfigurationItem("  ");

        ProjectValidationException exception = assertThrows(
            ProjectValidationException.class,
            () -> sut.validate(request)
        );

        assertEquals(ErrorKey.BAD_REQUEST_FLAVOR_CONFIG_ITEM, exception.getErrorKey());
    }

    @ParameterizedTest
    @CsvSource({
        "STANDARD, null",
        "null, JIRA",
        "STANDARD, JIRA"
    })
    void validate_succeeds_when_flavor_or_config_item_provided(String projectFlavor, String configurationItem) {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid Name");
        request.setProjectFlavor("null".equals(projectFlavor) ? null : projectFlavor);
        request.setConfigurationItem("null".equals(configurationItem) ? null : configurationItem);

        assertDoesNotThrow(() -> sut.validate(request));
    }
}