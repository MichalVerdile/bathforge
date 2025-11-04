package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotNull;

public class SceneProductDTO {

    private Long id;

    @NotNull(message = "Scene ID is required")
    private Long sceneId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;

    private String productModelPath;

    private Long colorId;

    private String colorName;

    private String colorHexCode;

    private Double positionX;

    private Double positionY;

    private Double positionZ;

    private Double rotationX;

    private Double rotationY;

    private Double rotationZ;

    private Double scaleX = 1.0;

    private Double scaleY = 1.0;

    private Double scaleZ = 1.0;

    private String customProperties;

    public SceneProductDTO() {
    }

    public SceneProductDTO(Long sceneId, Long productId, Long colorId,
            Double positionX, Double positionY, Double positionZ,
            Double rotationX, Double rotationY, Double rotationZ) {
        this.sceneId = sceneId;
        this.productId = productId;
        this.colorId = colorId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
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

    public Long getColorId() {
        return colorId;
    }

    public void setColorId(Long colorId) {
        this.colorId = colorId;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getColorHexCode() {
        return colorHexCode;
    }

    public void setColorHexCode(String colorHexCode) {
        this.colorHexCode = colorHexCode;
    }

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY;
    }

    public Double getPositionZ() {
        return positionZ;
    }

    public void setPositionZ(Double positionZ) {
        this.positionZ = positionZ;
    }

    public Double getRotationX() {
        return rotationX;
    }

    public void setRotationX(Double rotationX) {
        this.rotationX = rotationX;
    }

    public Double getRotationY() {
        return rotationY;
    }

    public void setRotationY(Double rotationY) {
        this.rotationY = rotationY;
    }

    public Double getRotationZ() {
        return rotationZ;
    }

    public void setRotationZ(Double rotationZ) {
        this.rotationZ = rotationZ;
    }

    public Double getScaleX() {
        return scaleX;
    }

    public void setScaleX(Double scaleX) {
        this.scaleX = scaleX;
    }

    public Double getScaleY() {
        return scaleY;
    }

    public void setScaleY(Double scaleY) {
        this.scaleY = scaleY;
    }

    public Double getScaleZ() {
        return scaleZ;
    }

    public void setScaleZ(Double scaleZ) {
        this.scaleZ = scaleZ;
    }

    public String getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(String customProperties) {
        this.customProperties = customProperties;
    }

    @Override
    public String toString() {
        return "SceneProductDTO{" +
                "id=" + id +
                ", sceneId=" + sceneId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", colorName='" + colorName + '\'' +
                ", position=[" + positionX + ", " + positionY + ", " + positionZ + "]" +
                ", rotation=[" + rotationX + ", " + rotationY + ", " + rotationZ + "]" +
                ", scale=[" + scaleX + ", " + scaleY + ", " + scaleZ + "]" +
                '}';
    }
}