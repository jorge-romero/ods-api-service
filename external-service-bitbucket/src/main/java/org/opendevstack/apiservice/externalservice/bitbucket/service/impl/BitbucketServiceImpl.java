package org.opendevstack.apiservice.externalservice.bitbucket.service.impl;

import org.opendevstack.apiservice.externalservice.bitbucket.client.ApiClient;
import org.opendevstack.apiservice.externalservice.bitbucket.client.api.ProjectApi;
import org.opendevstack.apiservice.externalservice.bitbucket.client.api.RepositoryApi;
import org.opendevstack.apiservice.externalservice.bitbucket.client.BitbucketApiClient;
import org.opendevstack.apiservice.externalservice.bitbucket.client.BitbucketApiClientFactory;
import org.opendevstack.apiservice.externalservice.bitbucket.exception.BitbucketException;
import org.opendevstack.apiservice.externalservice.bitbucket.service.BitbucketService;
import org.opendevstack.apiservice.externalservice.bitbucket.client.model.RestMinimalRef;
import org.opendevstack.apiservice.externalservice.bitbucket.client.model.GetBranches200Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Implementation of BitbucketService.
 * Uses BitbucketApiClientFactory to obtain clients for different Bitbucket
 * instances
 * and delegates operations to the appropriate generated API client.
 */
@Service
@Slf4j
public class BitbucketServiceImpl implements BitbucketService {

    private final BitbucketApiClientFactory clientFactory;

    /**
     * Constructor with dependency injection
     * 
     * @param clientFactory Factory for creating Bitbucket API clients
     */
    public BitbucketServiceImpl(BitbucketApiClientFactory clientFactory) {
        this.clientFactory = clientFactory;
        log.info("BitbucketServiceImpl initialized");
    }

    @Override
    public String getDefaultBranch(String instanceName, String projectKey, String repositorySlug)
            throws BitbucketException {
        log.debug("Getting default branch for repository '{}/{}' in instance '{}'",
                projectKey, repositorySlug, instanceName);

        try {
            BitbucketApiClient bitbucketClient = clientFactory.getClient(instanceName);
            ApiClient apiClient = bitbucketClient.getApiClient();

            ProjectApi projectApi = new ProjectApi(apiClient);

            RestMinimalRef defaultBranch = projectApi.getDefaultBranch2(projectKey, repositorySlug);

            if (defaultBranch != null) {
                return defaultBranch.getDisplayId();
            }

            throw new BitbucketException(
                    String.format("No default branch found for repository '%s/%s'", projectKey, repositorySlug));

        } catch (HttpClientErrorException.NotFound e) {
            throw new BitbucketException(
                    String.format("Repository '%s/%s' not found or has no default branch", projectKey, repositorySlug),
                    e);
        } catch (RestClientException e) {
            log.error("Error retrieving default branch for '{}/{}'", projectKey, repositorySlug, e);
            throw new BitbucketException(
                    String.format("Failed to retrieve default branch for repository '%s/%s'", projectKey,
                            repositorySlug),
                    e);
        }
    }

    @Override
    public boolean branchExists(String instanceName, String projectKey, String repositorySlug, String branchName)
            throws BitbucketException {
        log.debug("Checking if branch '{}' exists in repository '{}/{}' in instance '{}'",
                branchName, projectKey, repositorySlug, instanceName);

        try {
            BitbucketApiClient bitbucketClient = clientFactory.getClient(instanceName);
            ApiClient apiClient = bitbucketClient.getApiClient();

            RepositoryApi repositoryApi = new RepositoryApi(apiClient);

            // Get branches matching the branch name filter
            // Method signature: getBranches(projectKey, repositorySlug, boostMatches,
            // orderBy, details, filterText, base, start, limit)
            GetBranches200Response branches = repositoryApi.getBranches(
                    projectKey, // projectKey
                    repositorySlug, // repositorySlug
                    null, // boostMatches
                    null, // orderBy
                    null, // details
                    branchName, // filterText - search for this branch name
                    null, // base
                    BigDecimal.ZERO, // start
                    BigDecimal.valueOf(1) // limit
            );

            // Check if any branches were returned
            if (branches != null && branches.getValues() != null) {
                // Look for exact match (displayId or id matches the branch name)
                boolean exists = branches.getValues().stream()
                        .anyMatch(branch -> branchName.equals(branch.getDisplayId()) ||
                                branchName.equals(branch.getId()) ||
                                ("refs/heads/" + branchName).equals(branch.getId()));

                log.debug("Branch '{}' {} in repository '{}/{}'",
                        branchName, exists ? "exists" : "does not exist", projectKey, repositorySlug);
                return exists;
            }

            return false;

        } catch (HttpClientErrorException.NotFound e) {
            // Repository not found
            log.debug("Repository '{}/{}' not found", projectKey, repositorySlug);
            return false;

        } catch (RestClientException e) {
            log.error("Error checking if branch '{}' exists in '{}/{}'", branchName, projectKey, repositorySlug, e);
            throw new BitbucketException(
                    String.format("Failed to check if branch '%s' exists in repository '%s/%s'",
                            branchName, projectKey, repositorySlug),
                    e);
        }
    }

    @Override
    public Set<String> getAvailableInstances() {
        return clientFactory.getAvailableInstances();
    }

    @Override
    public boolean hasInstance(String instanceName) {
        return clientFactory.hasInstance(instanceName);
    }
}
