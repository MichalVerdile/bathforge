package com.bathforge.model.scene;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "scenes")
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Scene name is required")
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @NotBlank(message = "User is required")
    @Column(nullable = false)
    private String username;

    @Column(name = "scene_data", columnDefinition = "TEXT")
    private String sceneData;

    @Column(name = "camera_position")
    private String cameraPosition;

    @Column(name = "lighting_settings")
    private String lightingSettings;

    @Column(name = "background_color")
    private String backgroundColor;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SceneProduct> sceneProducts;

    @OneToOne(mappedBy = "scene", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private SceneRoomModel roomModel;

    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SceneCovering> sceneCoverings;

    public Scene() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Scene(String name, String description, String username) {
        this();
        this.name = name;
        this.description = description;
        this.username = username;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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
        return username;
    }

    public void setUser(String username) {
        this.username = username;
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

    public Set<SceneProduct> getSceneProducts() {
        return sceneProducts;
    }

    public void setSceneProducts(Set<SceneProduct> sceneProducts) {
        this.sceneProducts = sceneProducts;
    }

    public SceneRoomModel getRoomModel() {
        return roomModel;
    }

    public void setRoomModel(SceneRoomModel roomModel) {
        this.roomModel = roomModel;
    }

    public Set<SceneCovering> getSceneCoverings() {
        return sceneCoverings;
    }

    public void setSceneCoverings(Set<SceneCovering> sceneCoverings) {
        this.sceneCoverings = sceneCoverings;
    }

    @Override
    public String toString() {
        return "Scene{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", user='" + username + '\'' +
                ", isPublic=" + isPublic +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}