package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for scene products.
 * Represents a product placed in a scene with full details including
 * transformation and color information.
 */
public class SceneProductDTO {

    /** The unique identifier of the scene product */
    private Long id;

    /** The ID of the scene this product belongs to */
    @NotNull(message = "Scene ID is required")
    private Long sceneId;

    /** The ID of the product */
    @NotNull(message = "Product ID is required")
    private Long productId;

    /** The name of the product */
    private String productName;

    /** The path to the product's 3D model */
    private String productModelPath;

    /** The color variant ID for the product */
    private Long colorId;

    /** The name of the color */
    private String colorName;

    /** The hex code of the color */
    private String colorHexCode;

    /** X-coordinate position in 3D space */
    private Double positionX;

    /** Y-coordinate position in 3D space */
    private Double positionY;

    /** Z-coordinate position in 3D space */
    private Double positionZ;

    /** Rotation around X-axis in degrees */
    private Double rotationX;

    /** Rotation around Y-axis in degrees */
    private Double rotationY;

    /** Rotation around Z-axis in degrees */
    private Double rotationZ;

    /** Scale factor along X-axis */
    private Double scaleX = 1.0;

    /** Scale factor along Y-axis */
    private Double scaleY = 1.0;

    /** Scale factor along Z-axis */
    private Double scaleZ = 1.0;

    /** JSON string containing custom product properties */
    private String customProperties;

    /**
     * Default constructor.
     */
    public SceneProductDTO() {
    }

    /**
     * Constructs a SceneProductDTO with transformation data.
     *
     * @param sceneId   the scene ID
     * @param productId the product ID
     * @param colorId   the color variant ID
     * @param positionX X position
     * @param positionY Y position
     * @param positionZ Z position
     * @param rotationX X rotation
     * @param rotationY Y rotation
     * @param rotationZ Z rotation
     */
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

    /**
     * Returns a string representation of this scene product.
     *
     * @return string representation
     */
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