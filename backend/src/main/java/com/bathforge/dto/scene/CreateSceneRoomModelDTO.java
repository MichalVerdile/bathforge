package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for creating room models.
 * Defines the 3D geometry and properties of a room in a scene.
 */
public class CreateSceneRoomModelDTO {

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
    public CreateSceneRoomModelDTO() {
    }

    /**
     * Constructs a CreateSceneRoomModelDTO with required fields.
     *
     * @param verticesData the vertex data defining room shape
     * @param roomHeight   the room height
     */
    public CreateSceneRoomModelDTO(String verticesData, Double roomHeight) {
        this.verticesData = verticesData;
        this.roomHeight = roomHeight;
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
        return "CreateSceneRoomModelDTO{" +
                "modelType='" + modelType + '\'' +
                ", roomHeight=" + roomHeight +
                ", templatePath='" + templatePath + '\'' +
                '}';
    }
}