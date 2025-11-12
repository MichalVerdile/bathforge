package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateSceneRoomModelDTO {

    @NotBlank(message = "Vertices data is required")
    private String verticesData;

    @NotNull(message = "Room height is required")
    private Double roomHeight;

    private String modelType = "CUSTOM";

    private String templatePath;

    private String roomProperties;

    public CreateSceneRoomModelDTO() {
    }

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

    @Override
    public String toString() {
        return "CreateSceneRoomModelDTO{" +
                "modelType='" + modelType + '\'' +
                ", roomHeight=" + roomHeight +
                ", templatePath='" + templatePath + '\'' +
                '}';
    }
}