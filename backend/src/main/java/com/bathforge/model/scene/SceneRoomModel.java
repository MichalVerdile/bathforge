package com.bathforge.model.scene;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "scene_room_models")
public class SceneRoomModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "scene_id", nullable = false)
    @NotNull(message = "Scene is required")
    private Scene scene;

    @Column(name = "vertices_data", columnDefinition = "TEXT")
    @NotNull(message = "Vertices data is required")
    private String verticesData;

    @Column(name = "room_height")
    @NotNull(message = "Room height is required")
    private Double roomHeight;

    @Column(name = "model_type")
    private String modelType = "CUSTOM";

    @Column(name = "template_path")
    private String templatePath;

    @Column(name = "room_properties", columnDefinition = "TEXT")
    private String roomProperties;

    public SceneRoomModel() {
    }

    public SceneRoomModel(Scene scene, String verticesData, Double roomHeight) {
        this.scene = scene;
        this.verticesData = verticesData;
        this.roomHeight = roomHeight;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
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
        return "SceneRoomModel{" +
                "id=" + id +
                ", modelType='" + modelType + '\'' +
                ", roomHeight=" + roomHeight +
                ", templatePath='" + templatePath + '\'' +
                '}';
    }
}