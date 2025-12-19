package com.bathforge.model.products;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

/**
 * Entity representing a product category.
 * Categories group related products and colors together.
 */
@Entity
@Table(name = "categories")
public class Category {

    /** The unique identifier of the category */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The name of the category */
    @NotBlank(message = "Category name is required")
    @Column(nullable = false, unique = true)
    private String name;

    /** Optional description of the category */
    @Column(length = 500)
    private String description;

    /** Set of products belonging to this category */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Product> products;

    /** Set of colors available for this category */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Color> colors;

    /**
     * Default constructor.
     */
    public Category() {
    }

    /**
     * Constructs a Category with name and description.
     *
     * @param name        the category name
     * @param description the category description
     */
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
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

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }

    public Set<Color> getColors() {
        return colors;
    }

    public void setColors(Set<Color> colors) {
        this.colors = colors;
    }

    /**
     * Returns a string representation of this category.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}