package com.bathforge.dto.ai;

/**
 * Data Transfer Object for product recommendations in AI-generated designs.
 */
public class ProductRecommendationDTO {

    /** The unique identifier of the recommended product */
    private Long productId;
    /** The name of the recommended product */
    private String productName;
    /** The category of the product */
    private String category;
    /** The category ID of the product */
    private Long categoryId;
    /** The price range of the product */
    private String priceRange;
    /** The mounting type of the product */
    private String mountingType;
    /** The color of the product */
    private String color;
    /** The X position of the product in the scene */
    private Double positionX;
    /** The Y position of the product in the scene */
    private Double positionY;
    /** The Z position of the product in the scene */
    private Double positionZ;
    /** The X rotation of the product in the scene */
    private Double rotationX;
    /** The Y rotation of the product in the scene */
    private Double rotationY;
    /** The Z rotation of the product in the scene */
    private Double rotationZ;

    // Constructors
    public ProductRecommendationDTO() {
    }

    public ProductRecommendationDTO(Long productId, String productName, String category) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public String getMountingType() {
        return mountingType;
    }

    public void setMountingType(String mountingType) {
        this.mountingType = mountingType;
    }

    @Override
    public String toString() {
        return "ProductRecommendationDTO{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", category='" + category + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}