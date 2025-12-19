package com.bathforge.dto.scene;

import java.util.List;

/**
 * Data Transfer Object for updating existing scenes.
 * All fields are optional to allow partial updates.
 */
public class UpdateSceneDTO {

    /** The new name for the scene */
    private String name;

    /** The new description for the scene */
    private String description;

    /** JSON string containing updated scene configuration data */
    private String sceneData;

    /** JSON string containing updated camera position */
    private String cameraPosition;

    /** JSON string containing updated lighting configuration */
    private String lightingSettings;

    /** Hex color code for the new background */
    private String backgroundColor;

    /** Updated public visibility status */
    private Boolean isPublic;

    /** Updated list of products in the scene */
    private List<CreateSceneProductDTO> products;

    /** Updated room model configuration */
    private CreateSceneRoomModelDTO roomModel;

    /** Updated list of surface coverings */
    private List<CreateSceneCoveringDTO> coverings;

    /**
     * Default constructor.
     */
    public UpdateSceneDTO() {
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
     * Returns a string representation of this update request.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "UpdateSceneDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isPublic=" + isPublic +
                ", productsCount=" + (products != null ? products.size() : 0) +
                '}';
    }
}