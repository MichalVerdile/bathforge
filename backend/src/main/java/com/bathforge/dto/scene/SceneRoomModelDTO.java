package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SceneRoomModelDTO {

    private Long id;

    @NotNull(message = "Scene ID is required")
    private Long sceneId;

    @NotBlank(message = "Vertices data is required")
    private String verticesData;

    @NotNull(message = "Room height is required")
    private Double roomHeight;

    private String modelType = "CUSTOM";

    private String templatePath;

    private String roomProperties;

    public SceneRoomModelDTO() {
    }

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