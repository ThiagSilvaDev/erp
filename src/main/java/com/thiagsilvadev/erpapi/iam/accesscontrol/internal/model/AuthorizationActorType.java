package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model;

/**
 * Identifies who performed an audited authorization change. {@link #USER} requires an actor user
 * id, {@link #SYSTEM} is reserved for explicitly system-initiated changes, and {@link #UNKNOWN}
 * records changes whose actor could not be determined (for example, when no authenticated
 * principal was available). An audit trail must never silently label a change as {@link #SYSTEM}
 * when the actor is simply unknown.
 */
public enum AuthorizationActorType {
    USER,
    SYSTEM,
    UNKNOWN
}
