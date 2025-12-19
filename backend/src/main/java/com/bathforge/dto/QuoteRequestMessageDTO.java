package com.bathforge.dto;

import java.time.LocalDateTime;

/**
 * DTO for quote request messages between users and admins.
 */
public class QuoteRequestMessageDTO {
    /** Unique identifier for the message */
    private Long id;

    /** The message content */
    private String message;

    /** The type of sender (e.g., USER, ADMIN) */
    private String senderType;

    /** Timestamp when the message was created */
    private LocalDateTime createdAt;

    public QuoteRequestMessageDTO() {
    }

    public QuoteRequestMessageDTO(Long id, String message, String senderType, LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.senderType = senderType;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
