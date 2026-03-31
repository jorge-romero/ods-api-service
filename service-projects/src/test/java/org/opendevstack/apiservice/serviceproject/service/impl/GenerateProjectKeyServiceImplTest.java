package org.opendevstack.apiservice.serviceproject.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opendevstack.apiservice.serviceproject.exception.ProjectKeyGenerationException;
import org.opendevstack.apiservice.serviceproject.service.ProjectExistenceService;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenerateProjectKeyServiceImplTest {

    @Mock
    private ProjectExistenceService projectExistenceService;

    @Mock
    private Random random;

    private GenerateProjectKeyServiceImpl tested;

    @BeforeEach
    void setup() {
        tested = new GenerateProjectKeyServiceImpl(projectExistenceService, random);
    }

    @Test
    void generate_project_key_returns_key_when_first_candidate_is_free() throws Exception {
        when(random.nextInt(1_000_000)).thenReturn(7);
        when(projectExistenceService.isProjectFound("SS000007")).thenReturn(false);

        String result = tested.generateProjectKey(null);

        assertThat(result).isEqualTo("SS000007");
    }

    @Test
    void generate_project_key_retries_until_unique_key_found() throws Exception {
        when(random.nextInt(1_000_000)).thenReturn(1, 2);
        when(projectExistenceService.isProjectFound("SS000001")).thenReturn(true);
        when(projectExistenceService.isProjectFound("SS000002")).thenReturn(false);

        String result = tested.generateProjectKey("SS%06d");

        assertThat(result).isEqualTo("SS000002");
    }

    @Test
    void generate_project_key_throws_exception_when_no_unique_key_after_max_retries() throws Exception {
        when(random.nextInt(1_000_000)).thenReturn(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        when(projectExistenceService.isProjectFound(anyString())).thenReturn(true);

        assertThatThrownBy(() -> tested.generateProjectKey("SS%06d"))
                .isInstanceOf(ProjectKeyGenerationException.class)
                .hasMessageContaining("Failed to generate unique project key after 10 retries");
    }

    @Test
    void generate_project_key_uses_custom_pattern_when_provided() throws Exception {
        when(random.nextInt(1_000_000)).thenReturn(42);
        when(projectExistenceService.isProjectFound("AB0042")).thenReturn(false);

        String result = tested.generateProjectKey("AB%04d");

        assertThat(result).isEqualTo("AB0042");
    }
}
