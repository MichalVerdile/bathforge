package com.bathforge.model.quote;

import com.bathforge.model.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quote_requests")
public class QuoteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "room_dimensions", length = 500)
    private String roomDimensions;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @Column(name = "scene_snapshot", columnDefinition = "TEXT")
    private String sceneSnapshot;

    @Column(name = "scene_data", columnDefinition = "TEXT")
    private String sceneData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "status")
    private String status = "PENDING"; // PENDING, PROCESSED, QUOTED

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public QuoteRequest() {
    }

    public QuoteRequest(User user, String roomDimensions, String additionalNotes, String sceneSnapshot,
            String sceneData) {
        this.user = user;
        this.roomDimensions = roomDimensions;
        this.additionalNotes = additionalNotes;
        this.sceneSnapshot = sceneSnapshot;
        this.sceneData = sceneData;
    }

    // Getters and Setters
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
}
