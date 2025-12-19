package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Data Transfer Object for creating a new scene.
 * Contains all information needed to create a bathroom configurator scene.
 */
public class CreateSceneDTO {

    /** The name of the scene */
    @NotBlank(message = "Scene name is required")
    private String name;

    /** Optional description of the scene */
    private String description;

    /** The user who created the scene, defaults to "guest" */
    private String user = "guest";

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

    /** List of products placed in the scene */
    private List<CreateSceneProductDTO> products;

    /** The room model configuration */
    private CreateSceneRoomModelDTO roomModel;

    /** List of surface coverings applied to the room */
    private List<CreateSceneCoveringDTO> coverings;

    /**
     * Default constructor.
     */
    public CreateSceneDTO() {
    }

    /**
     * Constructs a CreateSceneDTO with basic information.
     *
     * @param name        the scene name
     * @param description the scene description
     * @param user        the user creating the scene
     */
    public CreateSceneDTO(String name, String description, String user) {
        this.name = name;
        this.description = description;
        this.user = user;
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

    public List<CreateSceneProductDTO> getProducts() {
        return products;
    }

    public void setProducts(List<CreateSceneProductDTO> products) {
        this.products = products;
    }

    public CreateSceneRoomModelDTO getRoomModel() {
        return roomModel;
    }

    public void setRoomModel(CreateSceneRoomModelDTO roomModel) {
        this.roomModel = roomModel;
    }

    public List<CreateSceneCoveringDTO> getCoverings() {
        return coverings;
    }

    public void setCoverings(List<CreateSceneCoveringDTO> coverings) {
        this.coverings = coverings;
    }

    /**
     * Returns a string representation of this scene.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "CreateSceneDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", user='" + user + '\'' +
                ", isPublic=" + isPublic +
                ", productsCount=" + (products != null ? products.size() : 0) +
                '}';
    }
}