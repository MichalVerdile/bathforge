package com.bathforge.dto.scene;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateSceneCoveringDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Surface type is required")
    private String surfaceType;

    private String surfaceIdentifier;

    private Double repeatX = 1.0;

    private Double repeatY = 1.0;

    private String materialProperties;

    public CreateSceneCoveringDTO() {
    }

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