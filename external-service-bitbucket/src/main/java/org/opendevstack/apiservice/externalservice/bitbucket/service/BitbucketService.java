package org.opendevstack.apiservice.externalservice.bitbucket.service;

import org.opendevstack.apiservice.externalservice.bitbucket.exception.BitbucketException;

import java.util.Set;

/**
 * Service interface for interacting with Bitbucket repositories.
 * Provides high-level methods to work with branches in Bitbucket repositories across multiple instances.
 */
public interface BitbucketService {
    
    /**
     * Get the default branch for a repository in a specific Bitbucket instance
     * 
     * @param instanceName Name of the Bitbucket instance
     * @param projectKey Project key (e.g., "PROJ")
     * @param repositorySlug Repository slug (e.g., "my-repo")
     * @return The default branch name (e.g., "main", "master", "develop")
     * @throws BitbucketException if the default branch cannot be retrieved
     */
    String getDefaultBranch(String instanceName, String projectKey, String repositorySlug) throws BitbucketException;
    
    /**
     * Check if a specific branch exists in a repository
     * 
     * @param instanceName Name of the Bitbucket instance
     * @param projectKey Project key (e.g., "PROJ")
     * @param repositorySlug Repository slug (e.g., "my-repo")
     * @param branchName Name of the branch to check (e.g., "feature/my-feature")
     * @return true if the branch exists, false otherwise
     * @throws BitbucketException if the check fails due to an error (not including branch not found)
     */
    boolean branchExists(String instanceName, String projectKey, String repositorySlug, String branchName) throws BitbucketException;
    
    /**
     * Get all available Bitbucket instance names
     * 
     * @return Set of configured instance names
     */
    Set<String> getAvailableInstances();
    
    /**
     * Check if a Bitbucket instance is configured
     * 
     * @param instanceName Name of the instance to check
     * @return true if configured, false otherwise
     */
    boolean hasInstance(String instanceName);
}
