package org.opendevstack.apiservice.externalservice.uipath.service;

import org.opendevstack.apiservice.externalservice.uipath.exception.UiPathException;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItem;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemRequest;
import org.opendevstack.apiservice.externalservice.uipath.model.UiPathQueueItemResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for integrating with UIPath Orchestrator.
 * Provides methods for authentication, queue item management, and status checking.
 */
public interface UiPathOrchestratorService {

    /**
     * Authenticates to UIPath Orchestrator and returns a bearer token.
     * The token can be used for subsequent API calls.
     *
     * @return the authentication token (bearer token)
     * @throws UiPathException.AuthenticationException if authentication fails
     */
    String authenticate() throws UiPathException.AuthenticationException;

    /**
     * Adds a new queue item to UIPath Orchestrator.
     * This triggers a robot to process the item.
     *
     * @param request the queue item request with all necessary data
     * @return the created queue item with assigned ID
     * @throws UiPathException.QueueItemCreationException if creation fails
     */
    UiPathQueueItem addQueueItem(UiPathQueueItemRequest request) throws UiPathException.QueueItemCreationException;

    /**
     * Adds a new queue item asynchronously.
     *
     * @param request the queue item request
     * @return a CompletableFuture containing the created queue item
     */
    CompletableFuture<UiPathQueueItem> addQueueItemAsync(UiPathQueueItemRequest request);

    /**
     * Gets the status of a queue item by its ID.
     *
     * @param queueItemId the ID of the queue item
     * @return the queue item with current status
     * @throws UiPathException.QueueItemNotFoundException if the item is not found
     * @throws UiPathException.StatusCheckException if status check fails
     */
    UiPathQueueItem getQueueItemById(Long queueItemId) 
            throws UiPathException.QueueItemNotFoundException, UiPathException.StatusCheckException;

    /**
     * Gets queue items by reference.
     * Multiple items can have the same reference, returns all matching items.
     *
     * @param reference the reference string to search for
     * @return list of queue items with the given reference (may be empty)
     * @throws UiPathException.StatusCheckException if the query fails
     */
    List<UiPathQueueItem> getQueueItemsByReference(String reference) 
            throws UiPathException.StatusCheckException;

    /**
     * Gets the latest queue item (highest ID) for a given reference.
     * This mimics the behavior of the Ansible role which gets the highest ID.
     *
     * @param reference the reference string to search for
     * @return Optional containing the latest queue item, or empty if not found
     * @throws UiPathException.StatusCheckException if the query fails
     */
    Optional<UiPathQueueItem> getLatestQueueItemByReference(String reference) 
            throws UiPathException.StatusCheckException;

    /**
     * Checks if a queue item has finalized (completed successfully or failed).
     * Uses reference to find the latest queue item, then checks its status.
     *
     * @param reference the reference string to search for
     * @return true if the item has finalized (successful, failed, abandoned, or deleted)
     * @throws UiPathException.QueueItemNotFoundException if no item found with the reference
     * @throws UiPathException.StatusCheckException if status check fails
     */
    boolean hasQueueItemFinalized(String reference) 
            throws UiPathException.QueueItemNotFoundException, UiPathException.StatusCheckException;

    /**
     * Checks if a queue item by ID has finalized.
     *
     * @param queueItemId the ID of the queue item
     * @return true if the item has finalized
     * @throws UiPathException.QueueItemNotFoundException if the item is not found
     * @throws UiPathException.StatusCheckException if status check fails
     */
    boolean hasQueueItemFinalizedById(Long queueItemId) 
            throws UiPathException.QueueItemNotFoundException, UiPathException.StatusCheckException;

    /**
     * Validates connection to UIPath Orchestrator by attempting authentication.
     *
     * @return true if connection is valid, false otherwise
     */
    boolean validateConnection();

    /**
     * Checks if UIPath Orchestrator is healthy and reachable.
     * This method is used by health indicators and should not throw exceptions.
     *
     * @return true if the platform is healthy, false otherwise
     */
    boolean isHealthy();

    /**
     * Checks the status of a queue item by reference and returns a comprehensive result.
     * This is a generic method that encapsulates the logic of:
     * 1. Handling empty/null references (returns NO_REFERENCE)
     * 2. Searching for the latest queue item by reference
     * 3. Determining if the queue item is in progress, successful, or failed
     * 4. Handling errors gracefully
     *
     * This method does not throw exceptions and always returns a result object.
     *
     * @param reference the reference string to search for (can be null or empty)
     * @return a UiPathQueueItemResult containing the status and details
     */
    UiPathQueueItemResult checkQueueItemByReference(String reference);
}
