package com.bathforge.model.scene;

import com.bathforge.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entity representing a bathroom configurator scene.
 * A scene contains the complete configuration of a bathroom including products,
 * room model, and coverings.
 */
@Entity
@Table(name = "scenes")
public class Scene {

    /** The unique identifier of the scene */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The name of the scene */
    @NotBlank(message = "Scene name is required")
    @Column(nullable = false)
    private String name;

    /** Optional description of the scene */
    @Column(length = 1000)
    private String description;

    /** The user who owns this scene */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Legacy username field for backwards compatibility (will be removed in future)
     */
    @Column(name = "username")
    private String username;

    /** JSON string containing complete scene configuration data */
    @Column(name = "scene_data", columnDefinition = "TEXT")
    private String sceneData;

    /** JSON string containing camera position */
    @Column(name = "camera_position")
    private String cameraPosition;

    /** JSON string containing lighting configuration */
    @Column(name = "lighting_settings")
    private String lightingSettings;

    /** Hex color code for the background */
    @Column(name = "background_color")
    private String backgroundColor;

    /** Whether the scene is publicly visible */
    @Column(name = "is_public")
    private Boolean isPublic = false;

    /** Timestamp when the scene was created */
    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the scene was last updated */
    @NotNull
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Set of products placed in this scene */
    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SceneProduct> sceneProducts;

    /** The room model configuration for this scene */
    @OneToOne(mappedBy = "scene", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private SceneRoomModel roomModel;

    /** Set of surface coverings applied in this scene */
    @OneToMany(mappedBy = "scene", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SceneCovering> sceneCoverings;

    /**
     * Default constructor.
     * Initializes creation and update timestamps.
     */
    public Scene() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructs a Scene with name, description, and username.
     * Used for backwards compatibility with legacy username-based scenes.
     *
     * @param name        the scene name
     * @param description the scene description
     * @param username    the username of the scene owner
     */
    public Scene(String name, String description, String username) {
        this();
        this.name = name;
        this.description = description;
        this.username = username;
    }

    /**
     * Constructs a Scene with name, description, and user entity.
     *
     * @param name        the scene name
     * @param description the scene description
     * @param user        the user entity owning this scene
     */
    public Scene(String name, String description, User user) {
        this();
        this.name = name;
        this.description = description;
        this.user = user;
    }

    /**
     * Lifecycle callback executed before updating a scene.
     * Updates the last modified timestamp.
     */
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

    public User getUserEntity() {
        return user;
    }

    public void setUserEntity(User user) {
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

    /**
     * Returns a string representation of this scene.
     *
     * @return string representation
     */
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