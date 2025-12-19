package com.bathforge.dto.auth;

/**
 * Data Transfer Object for user login responses.
 */
public class LoginResponseDTO {

    /** The JWT authentication token */
    private String token;
    /** The email address of the authenticated user */
    private String email;
    /** The unique identifier of the user */
    private Long userId;
    /** The first name of the user */
    private String firstName;
    /** The last name of the user */
    private String lastName;
    /** The role of the user */
    private String role;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, String email, Long userId, String firstName, String lastName, String role) {
        this.token = token;
        this.email = email;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
