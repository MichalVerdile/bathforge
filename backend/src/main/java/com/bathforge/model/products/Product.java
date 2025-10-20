package com.bathforge.model.products;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Price range is required")
    @Column(nullable = false)
    private PriceRange priceRange;

    @Column(nullable = false)
    private String modelPath;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Mounting type is required")
    @Column(nullable = false)
    private MountingType mountingType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @NotNull(message = "Category is required")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProductColor> productColors;

    public enum PriceRange {
        LOW, MEDIUM, HIGH
    }

    public enum MountingType {
        FLOOR, WALL, FREESTANDING
    }

    public Product() {
    }

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

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", priceRange=" + priceRange +
                ", modelPath='" + modelPath + '\'' +
                ", mountingType=" + mountingType +
                ", category=" + (category != null ? category.getName() : "null") +
                '}';
    }
}