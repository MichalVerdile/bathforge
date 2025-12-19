package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for scene room models.
 * Represents the 3D geometry and properties of a room in a scene with full
 * details.
 */
public class SceneRoomModelDTO {

    /** The unique identifier of the room model */
    private Long id;

    /** The ID of the scene this room model belongs to */
    @NotNull(message = "Scene ID is required")
    private Long sceneId;

    /** JSON string containing vertex coordinates defining the room shape */
    @NotBlank(message = "Vertices data is required")
    private String verticesData;

    /** The height of the room in meters */
    @NotNull(message = "Room height is required")
    private Double roomHeight;

    /** The type of room model (CUSTOM or TEMPLATE) */
    private String modelType = "CUSTOM";

    /** Path to template file if using a template-based room */
    private String templatePath;

    /** JSON string containing additional room properties */
    private String roomProperties;

    /**
     * Default constructor.
     */
    public SceneRoomModelDTO() {
    }

    /**
     * Constructs a SceneRoomModelDTO with required fields.
     *
     * @param sceneId      the scene ID
     * @param verticesData the vertex data defining room shape
     * @param roomHeight   the room height
     */
    public SceneRoomModelDTO(Long sceneId, String verticesData, Double roomHeight) {
        this.sceneId = sceneId;
        this.verticesData = verticesData;
        this.roomHeight = roomHeight;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSceneId() {
        return sceneId;
    }

    public void setSceneId(Long sceneId) {
        this.sceneId = sceneId;
    }

    public String getVerticesData() {
        return verticesData;
    }

    public void setVerticesData(String verticesData) {
        this.verticesData = verticesData;
    }

    public Double getRoomHeight() {
        return roomHeight;
    }

    public void setRoomHeight(Double roomHeight) {
        this.roomHeight = roomHeight;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public String getRoomProperties() {
        return roomProperties;
    }

    public void setRoomProperties(String roomProperties) {
        this.roomProperties = roomProperties;
    }

    /**
     * Returns a string representation of this room model.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "SceneRoomModelDTO{" +
                "id=" + id +
                ", sceneId=" + sceneId +
                ", modelType='" + modelType + '\'' +
                ", roomHeight=" + roomHeight +
                ", templatePath='" + templatePath + '\'' +
                '}';
    }
}