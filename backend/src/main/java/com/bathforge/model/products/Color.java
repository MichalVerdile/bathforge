package com.bathforge.model.products;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Set;

/**
 * Entity representing a color option.
 * Colors are associated with categories and can be applied to products.
 */
@Entity
@Table(name = "colors")
public class Color {

    /** The unique identifier of the color */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The name of the color */
    @NotBlank(message = "Color name is required")
    @Column(nullable = false)
    private String name;

    /** The hexadecimal color code (e.g., #FFFFFF) */
    @NotBlank(message = "Hex code is required")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid hex color code")
    @Column(nullable = false)
    private String hexCode;

    /** The category this color belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** Set of product-color associations */
    @OneToMany(mappedBy = "color", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProductColor> productColors;

    /**
     * Default constructor.
     */
    public Color() {
    }

    /**
     * Constructs a Color with all required fields.
     *
     * @param name     the color name
     * @param hexCode  the hex color code
     * @param category the category this color belongs to
     */
    public Color(String name, String hexCode, Category category) {
        this.name = name;
        this.hexCode = hexCode;
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

    public String getHexCode() {
        return hexCode;
    }

    public void setHexCode(String hexCode) {
        this.hexCode = hexCode;
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
     * Returns a string representation of this color.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "Color{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", hexCode='" + hexCode + '\'' +
                '}';
    }
}