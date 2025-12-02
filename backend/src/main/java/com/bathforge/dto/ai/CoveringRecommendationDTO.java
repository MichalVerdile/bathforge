package com.bathforge.dto.ai;

public class CoveringRecommendationDTO {

    private Long productId;
    private String productName;
    private String category;
    private String surfaceType; // "wall" or "floor"
    private Double repeatX;
    private Double repeatY;

    // Constructors
    public CoveringRecommendationDTO() {
        this.repeatX = 1.0;
        this.repeatY = 1.0;
    }

    public CoveringRecommendationDTO(Long productId, String productName, String category, String surfaceType) {
        this();
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.surfaceType = surfaceType;
    }

    // Getters and Setters
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSurfaceType() {
        return surfaceType;
    }

    public void setSurfaceType(String surfaceType) {
        this.surfaceType = surfaceType;
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

    @Override
    public String toString() {
        return "CoveringRecommendationDTO{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", category='" + category + '\'' +
                ", surfaceType='" + surfaceType + '\'' +
                ", repeatX=" + repeatX +
                ", repeatY=" + repeatY +
                '}';
    }
}
