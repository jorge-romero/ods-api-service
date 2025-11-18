package org.opendevstack.apiservice.externalservice.aap.service;

import org.opendevstack.apiservice.externalservice.aap.exception.AutomationPlatformException;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationExecutionResult;
import org.opendevstack.apiservice.externalservice.aap.model.AutomationJobStatus;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Generic service interface for integrating with automation platforms like Ansible Automation Platform.
 * This interface provides a generic way to call different workflows and modules.
 */
public interface AutomationPlatformService {

    /**
     * Executes a workflow on the automation platform synchronously.
     *
     * @param workflowName the name of the workflow to execute
     * @param parameters   the parameters to pass to the workflow
     * @return the execution result
     * @throws AutomationPlatformException if workflow execution fails
     */
    AutomationExecutionResult executeWorkflow(String workflowName, Map<String, Object> parameters) throws AutomationPlatformException;

    /**
     * Executes a workflow on the automation platform asynchronously.
     *
     * @param workflowName the name of the workflow to execute
     * @param parameters   the parameters to pass to the workflow
     * @return a CompletableFuture containing the execution result
     */
    CompletableFuture<AutomationExecutionResult> executeWorkflowAsync(String workflowName, Map<String, Object> parameters);


    /**
     * Checks the status of a running job on the automation platform.
     *
     * @param jobId the ID of the job to check
     * @return the job status and result
     * @throws AutomationPlatformException if status check fails
     */
    AutomationJobStatus getJobStatus(String jobId) throws AutomationPlatformException;

    /**
     * Checks the status of a workflow job on the automation platform.
     *
     * @param workflowId the ID of the workflow job to check
     * @return the workflow job status and result
     * @throws AutomationPlatformException if status check fails
     */
    AutomationJobStatus getWorkflowJobStatus(String workflowId) throws AutomationPlatformException;


    /**
     * Validates connection to the automation platform.
     *
     * @return true if connection is valid, false otherwise
     */
    boolean validateConnection();

    /**
     * Checks if the automation platform is healthy and reachable.
     * This method is used by health indicators and should not throw exceptions.
     *
     * @return true if the platform is healthy, false otherwise
     */
    boolean isHealthy();


}
