/*
 * Based on LuckPerms' DataMutateResult
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

import org.jetbrains.annotations.NotNull;

/**
 * Result of a data mutation operation.
 */
public enum DataMutateResult {

    /**
     * The operation was successful.
     */
    SUCCESS("Success"),

    /**
     * The operation failed because the data already exists.
     */
    FAIL_ALREADY_EXISTS("Already exists"),

    /**
     * The operation failed because the data doesn't exist.
     */
    FAIL_DOES_NOT_EXIST("Does not exist"),

    /**
     * The operation failed due to a constraint violation.
     */
    FAIL_CONSTRAINT_VIOLATION("Constraint violation"),

    /**
     * The operation failed for an unknown reason.
     */
    FAIL("Failed");

    private final String name;

    DataMutateResult(String name) {
        this.name = name;
    }

    /**
     * Gets a friendly name for this result.
     *
     * @return the name
     */
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * Checks if the operation was successful.
     *
     * @return true if successful
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * Checks if the operation failed.
     *
     * @return true if failed
     */
    public boolean isFailure() {
        return this != SUCCESS;
    }
}
