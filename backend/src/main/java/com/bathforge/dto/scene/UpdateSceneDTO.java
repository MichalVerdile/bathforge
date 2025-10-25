package com.bathforge.dto.scene;

import java.util.List;

public class UpdateSceneDTO {

    private String name;

    private String description;

    private String sceneData;

    private String cameraPosition;

    private String lightingSettings;

    private String backgroundColor;

    private Boolean isPublic;

    private List<CreateSceneProductDTO> products;

    public UpdateSceneDTO() {
    }

    // Getters and setters
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