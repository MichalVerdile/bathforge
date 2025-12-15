package com.bathforge.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuoteRequestDetailDTO {
    private Long id;
    private String status;
    private String roomDimensions;
    private String additionalNotes;
    private String sceneSnapshot;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String documentUrl;
    private List<QuoteRequestMessageDTO> messages;

    public QuoteRequestDetailDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRoomDimensions() {
        return roomDimensions;
    }

    public void setRoomDimensions(String roomDimensions) {
        this.roomDimensions = roomDimensions;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public String getSceneSnapshot() {
        return sceneSnapshot;
    }

    public void setSceneSnapshot(String sceneSnapshot) {
        this.sceneSnapshot = sceneSnapshot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public List<QuoteRequestMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<QuoteRequestMessageDTO> messages) {
        this.messages = messages;
    }
}
