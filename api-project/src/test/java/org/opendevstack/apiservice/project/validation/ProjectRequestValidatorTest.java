package org.opendevstack.apiservice.project.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opendevstack.apiservice.project.exception.ErrorKey;
import org.opendevstack.apiservice.project.exception.ProjectValidationException;
import org.opendevstack.apiservice.project.model.CreateProjectRequest;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectRequestValidatorTest {

    private ProjectRequestValidator sut;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        sut = new ProjectRequestValidator();

        Field locationsField = ProjectRequestValidator.class.getDeclaredField("locations");
        locationsField.setAccessible(true);
        locationsField.set(sut, List.of("MADRID", "BARCELONA", "SANT_CUGAT"));

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

    @Test
    void validate_throws_exception_when_x2account_present_but_owner_missing() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid");
        request.setProjectFlavor("STANDARD");
        request.setX2OdsAccount("x2abc123");
        request.setOwner(null);

        ProjectValidationException exception =
                assertThrows(ProjectValidationException.class, () -> sut.validate(request));

        assertEquals(ErrorKey.MANDATORY_OWNER, exception.getErrorKey());
    }

    @ParameterizedTest
    @CsvSource({
            "x2abc, owner1",
            "x2x, xowner",
            "null, owner1",
            "x2valid, ownerX"
    })
    void validate_succeeds_when_owner_present_or_x2account_missing(String x2, String owner) {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid");
        request.setProjectFlavor("STANDARD");

        request.setX2OdsAccount("null".equals(x2) ? null : x2);
        request.setOwner(owner);

        assertDoesNotThrow(() -> sut.validate(request));
    }

    @Test
    void validate_succeeds_when_location_is_null_or_empty() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid");
        request.setProjectFlavor("STANDARD");

        request.setLocation(null);
        assertDoesNotThrow(() -> sut.validate(request));

        request.setLocation("");
        assertDoesNotThrow(() -> sut.validate(request));

        request.setLocation("   ");
        assertDoesNotThrow(() -> sut.validate(request));
    }

    @Test
    void validate_throws_exception_when_location_not_in_allowed_list() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid");
        request.setProjectFlavor("STANDARD");

        request.setLocation("INVALID_LOCATION_NOT_ALLOWED");

        ProjectValidationException exception =
                assertThrows(ProjectValidationException.class, () -> sut.validate(request));

        assertEquals(ErrorKey.INVALID_LOCATION, exception.getErrorKey());
    }

    @ParameterizedTest
    @CsvSource({
            "MADRID",
            "BARCELONA",
            "SANT_CUGAT"
    })
    void validate_succeeds_when_location_is_allowed(String location) {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectName("Valid");
        request.setProjectFlavor("STANDARD");
        request.setLocation(location);

        assertDoesNotThrow(() -> sut.validate(request));
    }
}