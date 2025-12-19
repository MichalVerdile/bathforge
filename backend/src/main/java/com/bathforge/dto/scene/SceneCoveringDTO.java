package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for scene coverings.
 * Represents materials/textures applied to surfaces in a scene with full
 * details.
 */
public class SceneCoveringDTO {

    /** The unique identifier of the scene covering */
    private Long id;

    /** The ID of the scene this covering belongs to */
    @NotNull(message = "Scene ID is required")
    private Long sceneId;

    /** The ID of the product used as covering material */
    @NotNull(message = "Product ID is required")
    private Long productId;

    /** The name of the covering product */
    private String productName;

    /** The path to the product's 3D model */
    private String productModelPath;

    /** The type of surface this covering is applied to */
    @NotBlank(message = "Surface type is required")
    private String surfaceType;

    /** Optional identifier for the specific surface */
    private String surfaceIdentifier;

    /** Horizontal texture repeat factor */
    private Double repeatX = 1.0;

    /** Vertical texture repeat factor */
    private Double repeatY = 1.0;

    /** JSON string containing additional material properties */
    private String materialProperties;

    /**
     * Default constructor.
     */
    public SceneCoveringDTO() {
    }

    /**
     * Constructs a SceneCoveringDTO with required fields.
     *
     * @param sceneId           the scene ID
     * @param productId         the product ID
     * @param surfaceType       the surface type
     * @param surfaceIdentifier the surface identifier
     */
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

    /**
     * Returns a string representation of this scene covering.
     *
     * @return string representation
     */
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