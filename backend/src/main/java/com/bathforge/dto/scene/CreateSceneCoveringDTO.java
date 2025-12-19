package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for creating scene coverings.
 * Represents materials/textures applied to surfaces in a scene.
 */
public class CreateSceneCoveringDTO {

    /** The ID of the product used as covering material */
    @NotNull(message = "Product ID is required")
    private Long productId;

    /** The type of surface to apply the covering to (e.g., wall, floor, ceiling) */
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
    public CreateSceneCoveringDTO() {
    }

    /**
     * Constructs a CreateSceneCoveringDTO with required fields.
     *
     * @param productId         the ID of the covering product
     * @param surfaceType       the type of surface
     * @param surfaceIdentifier the surface identifier
     */
    public CreateSceneCoveringDTO(Long productId, String surfaceType, String surfaceIdentifier) {
        this.productId = productId;
        this.surfaceType = surfaceType;
        this.surfaceIdentifier = surfaceIdentifier;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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
     * Returns a string representation of this covering.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "CreateSceneCoveringDTO{" +
                "productId=" + productId +
                ", surfaceType='" + surfaceType + '\'' +
                ", surfaceIdentifier='" + surfaceIdentifier + '\'' +
                ", repeatX=" + repeatX +
                ", repeatY=" + repeatY +
                '}';
    }
}