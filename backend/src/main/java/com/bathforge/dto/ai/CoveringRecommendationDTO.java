package com.bathforge.dto.ai;

/**
 * Data Transfer Object for surface covering recommendations in AI-generated
 * designs.
 */
public class CoveringRecommendationDTO {

    /** The unique identifier of the recommended product */
    private Long productId;
    /** The name of the recommended product */
    private String productName;
    /** The category of the covering product */
    private String category;
    /** The surface type for the covering (wall or floor) */
    private String surfaceType; // "wall" or "floor"
    /** The texture repeat value along the X axis */
    private Double repeatX;
    /** The texture repeat value along the Y axis */
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
