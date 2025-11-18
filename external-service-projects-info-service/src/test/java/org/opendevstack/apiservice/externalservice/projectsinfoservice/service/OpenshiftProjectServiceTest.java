package org.opendevstack.apiservice.externalservice.projectsinfoservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.config.OpenshiftClusterConfiguration;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Metadata;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.OpenshiftProjectCluster;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.Project;
import org.opendevstack.apiservice.externalservice.projectsinfoservice.model.ProjectList;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenshiftProjectServiceTest {

    @Mock
    private RestTemplate mockRestTemplate;

    @InjectMocks
    private OpenShiftProjectService openshiftProjectService;

    @BeforeEach
    void setUp() {
        Map<String, Map<String, String>> clusters = new HashMap<>();
        Map<String, String> euCluster = new HashMap<>();
        Map<String, String> usTest = new HashMap<>();
        euCluster.put("url", "https://cluster1.example.com");
        euCluster.put("token", "mytoken");
        usTest.put("url", "https://cluster2.example.com");
        usTest.put("token", "mytoken");
        clusters.put("cluster1", euCluster);
        clusters.put("cluster2", usTest);

        OpenshiftClusterConfiguration openshiftClusterConfig = new OpenshiftClusterConfiguration();
        openshiftClusterConfig.setClusters(clusters);

        // Manually inject the config since it's not a Spring bean
        openshiftProjectService = new OpenShiftProjectService(mockRestTemplate, openshiftClusterConfig);


        // Set the @Value field using ReflectionTestUtils
        ReflectionTestUtils.setField(openshiftProjectService, "projectApiUrl",
                "/apis/project.openshift.io/v1/projects");
    }

    @Test
    void givenTwoProjectsInDifferentClusters_whenFetchProjects_thenReturnTwoProjectsWithTheirClusters() {
        // Mock project list response
        Project project1 = new Project();
        Metadata metadata1 = new Metadata();
        metadata1.setName("myapp-cd");
        project1.setMetadata(metadata1);

        Project project2 = new Project();
        Metadata metadata2 = new Metadata();
        metadata2.setName("anotherapp"); // Should be filtered out
        project2.setMetadata(metadata2);

        ProjectList projectList = new ProjectList();
        projectList.setItems(List.of(project1, project2));

        ResponseEntity<ProjectList> responseEntity = new ResponseEntity<>(projectList, HttpStatus.OK);

        when(mockRestTemplate.exchange(
                eq("https://cluster1.example.com" + "/apis/project.openshift.io/v1/projects"),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(ProjectList.class)
        )).thenReturn(responseEntity);

        when(mockRestTemplate.exchange(
                eq("https://cluster2.example.com" + "/apis/project.openshift.io/v1/projects"),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(ProjectList.class)
        )).thenReturn(responseEntity);

        // Execute
        List<OpenshiftProjectCluster> result = openshiftProjectService.fetchProjects();

        // Verify
        assertEquals(2, result.size()); // One for each cluster
        OpenshiftProjectCluster projectCluster = result.getFirst();
        assertEquals("MYAPP", projectCluster.getProject());
        // Cluster name should be either "cluster1" or "cluster2"
        assertTrue(List.of("cluster1", "cluster2").contains(projectCluster.getCluster()));
    }

    @Test
    void givenProjectsWithLowercaseNames_whenFetchProjects_thenReturnProjectsWithUppercaseNames() {
        Project project1 = new Project();
        Metadata metadata1 = new Metadata();
        metadata1.setName("myapp-cd");
        project1.setMetadata(metadata1);

        Project project2 = new Project();
        Metadata metadata2 = new Metadata();
        metadata2.setName("anotherapp-cd");
        project2.setMetadata(metadata2);

        ProjectList projectList = new ProjectList();
        projectList.setItems(List.of(project1, project2));

        ResponseEntity<ProjectList> responseEntity = new ResponseEntity<>(projectList, HttpStatus.OK);

        when(mockRestTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(ProjectList.class)))
                .thenReturn(responseEntity);

        List<OpenshiftProjectCluster> result = openshiftProjectService.fetchProjects();

        assertEquals(4, result.size());
        result.forEach(projectCluster ->
                assertEquals(projectCluster.getProject(), projectCluster.getProject().toUpperCase())
        );
    }
}
