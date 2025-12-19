package com.bathforge.dto.quote;

/**
 * DTO for quote request submission responses.
 */
public class QuoteResponseDTO {

    /** Indicates if the quote request was successful */
    private boolean success;

    /** Response message */
    private String message;

    /** ID of the user who submitted the quote request */
    private Long userId;

    /** Email of the user who submitted the quote request */
    private String userEmail;

    /** JWT token for newly registered users */
    private String token;

    public QuoteResponseDTO() {
    }

    public QuoteResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public QuoteResponseDTO(boolean success, String message, Long userId, String userEmail) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.userEmail = userEmail;
    }

    public QuoteResponseDTO(boolean success, String message, Long userId, String userEmail, String token) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.userEmail = userEmail;
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
