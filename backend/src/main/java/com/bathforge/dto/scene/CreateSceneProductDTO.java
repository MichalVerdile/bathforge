package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotNull;

public class CreateSceneProductDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long colorId;

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

    public CreateSceneProductDTO() {
    }

    public CreateSceneProductDTO(Long productId, Long colorId,
            Double positionX, Double positionY, Double positionZ,
            Double rotationX, Double rotationY, Double rotationZ) {
        this.productId = productId;
        this.colorId = colorId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getColorId() {
        return colorId;
    }

    public void setColorId(Long colorId) {
        this.colorId = colorId;
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
        return "CreateSceneProductDTO{" +
                "productId=" + productId +
                ", colorId=" + colorId +
                ", position=[" + positionX + ", " + positionY + ", " + positionZ + "]" +
                ", rotation=[" + rotationX + ", " + rotationY + ", " + rotationZ + "]" +
                ", scale=[" + scaleX + ", " + scaleY + ", " + scaleZ + "]" +
                '}';
    }
}