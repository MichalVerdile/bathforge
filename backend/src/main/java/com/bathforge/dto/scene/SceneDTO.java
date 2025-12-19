package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for scenes.
 * Represents a complete bathroom configurator scene with all its components and
 * metadata.
 */
public class SceneDTO {

    /** The unique identifier of the scene */
    private Long id;

    /** The name of the scene */
    @NotBlank(message = "Scene name is required")
    private String name;

    /** Optional description of the scene */
    private String description;

    /** The user who owns this scene */
    @NotBlank(message = "User is required")
    private String user;

    /** JSON string containing scene configuration data */
    private String sceneData;

    /** JSON string containing camera position */
    private String cameraPosition;

    /** JSON string containing lighting configuration */
    private String lightingSettings;

    /** Hex color code for the background */
    private String backgroundColor;

    /** Whether the scene is publicly visible */
    private Boolean isPublic = false;

    /** Timestamp when the scene was created */
    @NotNull
    private LocalDateTime createdAt;

    /** Timestamp when the scene was last updated */
    @NotNull
    private LocalDateTime updatedAt;

    /** List of products placed in the scene */
    private List<SceneProductDTO> sceneProducts;

    /** The room model configuration */
    private SceneRoomModelDTO roomModel;

    /** List of surface coverings applied to the room */
    private List<SceneCoveringDTO> sceneCoverings;

    /**
     * Default constructor.
     */
    public SceneDTO() {
    }

    /**
     * Constructs a SceneDTO with basic information.
     *
     * @param name        the scene name
     * @param description the scene description
     * @param user        the scene owner
     */
    public SceneDTO(String name, String description, String user) {
        this.name = name;
        this.description = description;
        this.user = user;
    }

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

    public SceneRoomModelDTO getRoomModel() {
        return roomModel;
    }

    public void setRoomModel(SceneRoomModelDTO roomModel) {
        this.roomModel = roomModel;
    }

    public List<SceneCoveringDTO> getSceneCoverings() {
        return sceneCoverings;
    }

    public void setSceneCoverings(List<SceneCoveringDTO> sceneCoverings) {
        this.sceneCoverings = sceneCoverings;
    }

    /**
     * Returns a string representation of this scene.
     *
     * @return string representation
     */
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