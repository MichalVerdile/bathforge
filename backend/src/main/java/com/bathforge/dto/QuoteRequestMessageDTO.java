package com.bathforge.dto;

import java.time.LocalDateTime;

public class QuoteRequestMessageDTO {
    private Long id;
    private String message;
    private String senderType;
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
