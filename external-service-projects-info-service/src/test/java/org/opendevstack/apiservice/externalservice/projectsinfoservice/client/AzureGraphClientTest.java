package org.opendevstack.apiservice.externalservice.projectsinfoservice.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.InvalidContentProcessException;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.exception.InvalidTokenException;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureGraphClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AzureGraphClient azureGraphClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(azureGraphClient, "pageSize", 10);
    }

    @Test
    void givenValidAccessToken_whenGetUserGroups_thenReturnGroups() {
        // given
        var accessToken = "testAccessToken";

        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(
                eq("https://graph.microsoft.com/v1.0/me/memberOf?$top=10"),
                eq(HttpMethod.GET),
                captor.capture(),
                eq(String.class)
        )).thenReturn( // @odata.nextLink
                new ResponseEntity<>(
                "{\"@odata.nextLink\":\"https://graph.microsoft.com/v1.0/me/memberOf?$top=10\", \"value\":[{\"displayName\":\"Group1\"},{\"displayName\":\"Group2\"}]}",
                HttpStatus.OK
        )).thenReturn(
                new ResponseEntity<>(
                        "{\"value\":[{\"displayName\":\"Group3\"},{\"displayName\":\"Group4\"}]}",
                        HttpStatus.OK
        )); // No next link, end of pagination

        // when
        var userGroups = azureGraphClient.getUserGroups(accessToken);

        // then
        assertThat(captor.getValue()).isNotNull();
        assertThat(captor.getValue().getHeaders()).isNotNull();

        HttpHeaders headers = captor.getValue().getHeaders();

        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer " + accessToken);
        assertThat(userGroups).contains("Group1", "Group2", "Group3", "Group4");

        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void givenValidAccessToken_whenGetUserGroups_andNoGroups_thenReturnsEmptySet() {
        // given
        var accessToken = "testAccessToken";

        when(restTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)
        )).thenReturn(new ResponseEntity<>(
                "{\"invalid\":\"response\"}",
                HttpStatus.OK
        ));

        // when
        var groups = azureGraphClient.getUserGroups(accessToken);

        // then
        assertThat(groups).isEmpty();
    }

    @Test
    void givenValidAccessToken_whenGetUserGroups_andResponseIsNotValid_thenThrowsInvalidContentProcessException() {
        // given
        var accessToken = "testAccessToken";

        when(restTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)
        )).thenReturn(new ResponseEntity<>(
                "not a valid json",
                HttpStatus.OK
        ));

        // when
        var invalidContentProcessException = assertThrows(InvalidContentProcessException.class, () -> azureGraphClient.getUserGroups(accessToken));

        // then
        assertThat(invalidContentProcessException.getMessage()).isEqualTo("Error while processing server response");
    }

    @Test
    void givenInvalidAccessToken_whenGetUserGroups_thenThrowsInvalidTokenException() {
        // given
        var accessToken = "testAccessToken";

        when(restTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // when
        var invalidTokenException = assertThrows(InvalidTokenException.class, () -> azureGraphClient.getUserGroups(accessToken));

        // then
        assertThat(invalidTokenException.getMessage()).isEqualTo("Error while getting user groups");
    }

    @Test
    void givenAValidAccessToken_whenGetUserEmail_thenReturnEmail() {
        // given
        var accessToken = "testAccessToken";
        var userEmail = "pepito@example.com";

        ArgumentCaptor<HttpEntity<String>> captor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.exchange(
                eq("https://graph.microsoft.com/v1.0/me"),
                eq(HttpMethod.GET),
                captor.capture(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>(
                "{\"mail\": \"" + userEmail + "\"}",
                HttpStatus.OK
        ));

        // when
        var userEmailResponse = azureGraphClient.getUserEmail(accessToken);

        // then
        assertThat(captor.getValue()).isNotNull();
        assertThat(captor.getValue().getHeaders()).isNotNull();

        HttpHeaders headers = captor.getValue().getHeaders();

        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer " + accessToken);
        assertThat(userEmailResponse).isEqualTo(userEmail);
    }
}
