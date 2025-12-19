package com.bathforge.model.user;

/**
 * Enumeration of user roles in the system.
 * Determines the level of access and permissions a user has.
 */
public enum UserRole {
    /** Regular customer who can create scenes and request quotes */
    CUSTOMER,
    /** Administrator with full system access */
    ADMIN,
    /** Industry professional with extended product access */
    INDUSTRY
}
