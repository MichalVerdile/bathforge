package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SceneCoveringDTO {

    private Long id;

    @NotNull(message = "Scene ID is required")
    private Long sceneId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;
    private String productModelPath;

    @NotBlank(message = "Surface type is required")
    private String surfaceType;

    private String surfaceIdentifier;

    private Double repeatX = 1.0;

    private Double repeatY = 1.0;

    private String materialProperties;

    public SceneCoveringDTO() {
    }

    public SceneCoveringDTO(Long sceneId, Long productId, String surfaceType, String surfaceIdentifier) {
        this.sceneId = sceneId;
        this.productId = productId;
        this.surfaceType = surfaceType;
        this.surfaceIdentifier = surfaceIdentifier;
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductModelPath() {
        return productModelPath;
    }

    public void setProductModelPath(String productModelPath) {
        this.productModelPath = productModelPath;
    }

    public String getSurfaceType() {
        return surfaceType;
    }

    public void setSurfaceType(String surfaceType) {
        this.surfaceType = surfaceType;
    }

    public String getSurfaceIdentifier() {
        return surfaceIdentifier;
    }

    public void setSurfaceIdentifier(String surfaceIdentifier) {
        this.surfaceIdentifier = surfaceIdentifier;
    }

    public Double getRepeatX() {
        return repeatX;
    }

    public void setRepeatX(Double repeatX) {
        this.repeatX = repeatX;
    }

    public Double getRepeatY() {
        return repeatY;
    }

    public void setRepeatY(Double repeatY) {
        this.repeatY = repeatY;
    }

    public String getMaterialProperties() {
        return materialProperties;
    }

    public void setMaterialProperties(String materialProperties) {
        this.materialProperties = materialProperties;
    }

    @Override
    public String toString() {
        return "SceneCoveringDTO{" +
                "id=" + id +
                ", sceneId=" + sceneId +
                ", productId=" + productId +
                ", surfaceType='" + surfaceType + '\'' +
                ", surfaceIdentifier='" + surfaceIdentifier + '\'' +
                ", repeatX=" + repeatX +
                ", repeatY=" + repeatY +
                '}';
    }
}