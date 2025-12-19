package com.bathforge.model.products;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

/**
 * Entity representing a bathroom product.
 * Products belong to categories and can have multiple color options.
 */
@Entity
@Table(name = "products")
public class Product {

    /** The unique identifier of the product */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The name of the product */
    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    /** Detailed description of the product */
    @Column(length = 1000)
    private String description;

    /** The price range category of the product */
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Price range is required")
    @Column(nullable = false)
    private PriceRange priceRange;

    /** Path to the 3D model file */
    @Column(nullable = false)
    private String modelPath;

    /** Optional URL (relative or absolute) to a thumbnail image for this product */
    @Column(name = "thumbnail", length = 1024)
    private String thumbnail;

    /** The type of mounting for installation */
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Mounting type is required")
    @Column(nullable = false)
    private MountingType mountingType;

    /** The category this product belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Category is required")
    private Category category;

    /** Set of color options available for this product */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProductColor> productColors;

    /**
     * Enumeration of price range categories.
     */
    public enum PriceRange {
        /** Low price range */
        LOW,
        /** Medium price range */
        MEDIUM,
        /** High price range */
        HIGH
    }

    /**
     * Enumeration of mounting types for product installation.
     */
    public enum MountingType {
        /** Floor-mounted installation */
        FLOOR,
        /** Wall-mounted installation */
        WALL,
        /** Freestanding installation */
        FREESTANDING
    }

    /**
     * Default constructor.
     */
    public Product() {
    }

    /**
     * Constructs a Product with all required fields.
     *
     * @param name         the product name
     * @param description  the product description
     * @param priceRange   the price range
     * @param modelPath    the path to the 3D model
     * @param mountingType the mounting type
     * @param category     the product category
     */
    public Product(String name, String description, PriceRange priceRange,
            String modelPath, MountingType mountingType, Category category) {
        this.name = name;
        this.description = description;
        this.priceRange = priceRange;
        this.modelPath = modelPath;
        this.mountingType = mountingType;
        this.category = category;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<ProductColor> getProductColors() {
        return productColors;
    }

    public void setProductColors(Set<ProductColor> productColors) {
        this.productColors = productColors;
    }

    /**
     * Returns a string representation of this product.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", priceRange=" + priceRange +
                ", modelPath='" + modelPath + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", mountingType=" + mountingType +
                ", category=" + (category != null ? category.getName() : "null") +
                '}';
    }
}