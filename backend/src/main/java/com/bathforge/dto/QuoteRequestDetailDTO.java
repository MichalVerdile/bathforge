package com.bathforge.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for detailed quote request information.
 * Contains all information about a quote request including messages and
 * metadata.
 */
public class QuoteRequestDetailDTO {

    /** The unique identifier of the quote request */
    private Long id;

    /** The current status of the quote request */
    private String status;

    /** Description of room dimensions */
    private String roomDimensions;

    /** Additional notes provided by the customer */
    private String additionalNotes;

    /** Base64 encoded snapshot image of the scene */
    private String sceneSnapshot;

    /** Timestamp when the quote request was created */
    private LocalDateTime createdAt;

    /** Timestamp when the quote request was last updated */
    private LocalDateTime updatedAt;

    /** URL to the uploaded document */
    private String documentUrl;

    /** List of messages associated with this quote request */
    private List<QuoteRequestMessageDTO> messages;

    /**
     * Default constructor.
     */
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
