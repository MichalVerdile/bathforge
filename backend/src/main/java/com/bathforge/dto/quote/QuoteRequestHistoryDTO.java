package com.bathforge.dto.quote;

import java.time.LocalDateTime;

public class QuoteRequestHistoryDTO {

    private Long id;
    private String roomDimensions;
    private String additionalNotes;
    private String sceneSnapshot;
    private LocalDateTime createdAt;
    private String status;

    public QuoteRequestHistoryDTO() {
    }

    public QuoteRequestHistoryDTO(Long id, String roomDimensions, String additionalNotes,
            String sceneSnapshot, LocalDateTime createdAt, String status) {
        this.id = id;
        this.roomDimensions = roomDimensions;
        this.additionalNotes = additionalNotes;
        this.sceneSnapshot = sceneSnapshot;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
