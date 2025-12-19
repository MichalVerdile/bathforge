package com.bathforge.dto.admin;

/**
 * Data Transfer Object for user scene information in admin views.
 */
public class UserSceneDTO {
    /** The unique identifier for the scene */
    private Long sceneId;
    /** The name of the scene */
    private String sceneName;
    /** The description of the scene */
    private String sceneDescription;
    /** The user ID who created the scene */
    private Long userId;
    /** The email address of the user */
    private String userEmail;
    /** The full name of the user */
    private String userFullName;
    /** Whether the scene is public */
    private Boolean isPublic;
    /** Timestamp when the scene was created */
    private String createdAt;
    /** Timestamp when the scene was last updated */
    private String updatedAt;

    public UserSceneDTO() {
    }

    // Getters and Setters
    public Long getSceneId() {
        return sceneId;
    }

    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public String getSceneDescription() {
        return sceneDescription;
    }

    public void setSceneDescription(String sceneDescription) {
        this.sceneDescription = sceneDescription;
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

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
