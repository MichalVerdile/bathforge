package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateSceneDTO {

    @NotBlank(message = "Scene name is required")
    private String name;

    private String description;

    private String user = "guest";

    private String sceneData;

    private String cameraPosition;

    private String lightingSettings;

    private String backgroundColor;

    private Boolean isPublic = false;

    private List<CreateSceneProductDTO> products;

    public CreateSceneDTO() {
    }

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