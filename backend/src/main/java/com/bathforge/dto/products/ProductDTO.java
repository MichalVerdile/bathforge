package com.bathforge.dto.products;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import com.bathforge.model.products.Product.MountingType;
import com.bathforge.model.products.Product.PriceRange;

/**
 * Data Transfer Object for products.
 */
public class ProductDTO {

    /** The unique identifier of the product */
    private Long id;

    /** The name of the product */
    @NotBlank(message = "Product name is required")
    private String name;

    /** The description of the product */
    private String description;

    /** The price range of the product */
    @NotNull(message = "Price range is required")
    private PriceRange priceRange;

    /** The path to the 3D model file */
    @NotBlank(message = "Model path is required")
    private String modelPath;

    /** Optional URL to a thumbnail image for this product */
    private String thumbnail;

    /** The mounting type of the product */
    @NotNull(message = "Mounting type is required")
    private MountingType mountingType;

    /** The category ID this product belongs to */
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    /** The name of the category this product belongs to */
    private String categoryName;

    /** List of available colors for this product */
    private List<ColorDTO> availableColors;

    public ProductDTO() {
    }

    public ProductDTO(String name, String description, PriceRange priceRange,
            String modelPath, MountingType mountingType, Long categoryId) {
        this.name = name;
        this.description = description;
        this.priceRange = priceRange;
        this.modelPath = modelPath;
        this.mountingType = mountingType;
        this.categoryId = categoryId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PriceRange getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(PriceRange priceRange) {
        this.priceRange = priceRange;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public MountingType getMountingType() {
        return mountingType;
    }

    public void setMountingType(MountingType mountingType) {
        this.mountingType = mountingType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<ColorDTO> getAvailableColors() {
        return availableColors;
    }

    public void setAvailableColors(List<ColorDTO> availableColors) {
        this.availableColors = availableColors;
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", priceRange=" + priceRange +
                ", modelPath='" + modelPath + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", mountingType=" + mountingType +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}