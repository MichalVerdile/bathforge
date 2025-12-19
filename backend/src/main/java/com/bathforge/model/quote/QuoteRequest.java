package com.bathforge.model.quote;

import com.bathforge.model.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a quote request from a user.
 * Contains information about the bathroom configuration for which the user is
 * requesting a price quote.
 */
@Entity
@Table(name = "quote_requests")
public class QuoteRequest {

    /** The unique identifier of the quote request */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who created this quote request */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Description of the room dimensions */
    @Column(name = "room_dimensions", length = 500)
    private String roomDimensions;

    /** Additional notes or requirements from the user */
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    /** Base64 encoded snapshot image of the scene */
    @Column(name = "scene_snapshot", columnDefinition = "TEXT")
    private String sceneSnapshot;

    /** JSON string containing the complete scene configuration */
    @Column(name = "scene_data", columnDefinition = "TEXT")
    private String sceneData;

    /** Timestamp when the quote request was created */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** The current status of the quote request (PENDING, PROCESSED, QUOTED) */
    @Column(name = "status")
    private String status = "PENDING";

    /** Admin's response to the quote request */
    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    /** URL to the uploaded quote document */
    @Column(name = "document_url")
    private String documentUrl;

    /** Timestamp when the quote request was last updated */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Lifecycle callback executed before persisting a new quote request.
     * Sets the creation and update timestamps.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Lifecycle callback executed before updating a quote request.
     * Updates the last modified timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Default constructor.
     */
    public QuoteRequest() {
    }

    /**
     * Constructs a QuoteRequest with all required fields.
     *
     * @param user            the user creating the request
     * @param roomDimensions  the room dimensions
     * @param additionalNotes additional notes from the user
     * @param sceneSnapshot   base64 encoded scene snapshot
     * @param sceneData       JSON scene configuration data
     */
    public QuoteRequest(User user, String roomDimensions, String additionalNotes, String sceneSnapshot,
            String sceneData) {
        this.user = user;
        this.roomDimensions = roomDimensions;
        this.additionalNotes = additionalNotes;
        this.sceneSnapshot = sceneSnapshot;
        this.sceneData = sceneData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getSceneData() {
        return sceneData;
    }

    public void setSceneData(String sceneData) {
        this.sceneData = sceneData;
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

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
