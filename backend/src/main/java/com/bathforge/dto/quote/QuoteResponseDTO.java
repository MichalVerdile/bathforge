package com.bathforge.dto.quote;

public class QuoteResponseDTO {

    private boolean success;
    private String message;
    private Long userId;
    private String userEmail;

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
}
