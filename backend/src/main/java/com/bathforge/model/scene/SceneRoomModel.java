package com.bathforge.model.scene;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing the 3D room model in a scene.
 * Defines the geometric shape and properties of the room.
 */
@Entity
@Table(name = "scene_room_models")
public class SceneRoomModel {

    /** The unique identifier of the room model */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The scene this room model belongs to (one-to-one relationship) */
    @OneToOne
    @JoinColumn(name = "scene_id", nullable = false)
    @NotNull(message = "Scene is required")
    private Scene scene;

    /** JSON string containing vertex coordinates defining the room shape */
    @Column(name = "vertices_data", columnDefinition = "TEXT")
    @NotNull(message = "Vertices data is required")
    private String verticesData;

    /** The height of the room in meters */
    @Column(name = "room_height")
    @NotNull(message = "Room height is required")
    private Double roomHeight;

    /** The type of room model (CUSTOM or TEMPLATE) */
    @Column(name = "model_type")
    private String modelType = "CUSTOM";

    /** Path to template file if using a template-based room */
    @Column(name = "template_path")
    private String templatePath;

    /** JSON string containing additional room properties */
    @Column(name = "room_properties", columnDefinition = "TEXT")
    private String roomProperties;

    /**
     * Default constructor.
     */
    public SceneRoomModel() {
    }

    /**
     * Constructs a SceneRoomModel with required fields.
     *
     * @param scene        the scene this room model belongs to
     * @param verticesData the vertex data defining room shape
     * @param roomHeight   the room height
     */
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

    /**
     * Returns a string representation of this room model.
     *
     * @return string representation
     */
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