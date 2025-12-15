package com.bathforge.dto.admin;

import java.time.LocalDateTime;

public class QuoteRequestAdminDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private String userPhone;
    private String userCompany;
    private String roomDimensions;
    private String additionalNotes;
    private String sceneSnapshot;
    private String status;
    private String adminResponse;
    private String documentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public QuoteRequestAdminDTO() {
    }

    public QuoteRequestAdminDTO(Long id, Long userId, String userEmail, String userFullName,
            String userPhone, String userCompany, String roomDimensions,
            String additionalNotes, String sceneSnapshot, String status,
            String adminResponse, String documentUrl,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFullName = userFullName;
        this.userPhone = userPhone;
        this.userCompany = userCompany;
        this.roomDimensions = roomDimensions;
        this.additionalNotes = additionalNotes;
        this.sceneSnapshot = sceneSnapshot;
        this.status = status;
        this.adminResponse = adminResponse;
        this.documentUrl = documentUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserCompany() {
        return userCompany;
    }

    public void setUserCompany(String userCompany) {
        this.userCompany = userCompany;
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
}
