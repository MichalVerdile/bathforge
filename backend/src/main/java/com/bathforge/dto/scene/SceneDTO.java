package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class SceneDTO {

    private Long id;

    @NotBlank(message = "Scene name is required")
    private String name;

    private String description;

    @NotBlank(message = "User is required")
    private String user;

    private String sceneData;

    private String cameraPosition;

    private String lightingSettings;

    private String backgroundColor;

    private Boolean isPublic = false;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime updatedAt;

    private List<SceneProductDTO> sceneProducts;

    public SceneDTO() {
    }

    public SceneDTO(String name, String description, String user) {
        this.name = name;
        this.description = description;
        this.user = user;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSceneData() {
        return sceneData;
    }

    public void setSceneData(String sceneData) {
        this.sceneData = sceneData;
    }

    public String getCameraPosition() {
        return cameraPosition;
    }

    public void setCameraPosition(String cameraPosition) {
        this.cameraPosition = cameraPosition;
    }

    public String getLightingSettings() {
        return lightingSettings;
    }

    public void setLightingSettings(String lightingSettings) {
        this.lightingSettings = lightingSettings;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
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

    public List<SceneProductDTO> getSceneProducts() {
        return sceneProducts;
    }

    public void setSceneProducts(List<SceneProductDTO> sceneProducts) {
        this.sceneProducts = sceneProducts;
    }

    @Override
    public String toString() {
        return "SceneDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", user='" + user + '\'' +
                ", isPublic=" + isPublic +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", productsCount=" + (sceneProducts != null ? sceneProducts.size() : 0) +
                '}';
    }
}