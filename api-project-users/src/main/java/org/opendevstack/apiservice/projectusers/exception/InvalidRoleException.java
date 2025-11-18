package org.opendevstack.apiservice.projectusers.exception;

/**
 * Exception thrown when an invalid role is provided.
 */
public class InvalidRoleException extends ProjectUserException {
    public InvalidRoleException(String role) {
        super(String.format(ErrorMessages.INVALID_ROLE, role), ErrorCodes.INVALID_ROLE);
    }
}
