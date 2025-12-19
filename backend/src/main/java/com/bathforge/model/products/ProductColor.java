package com.bathforge.model.products;

import jakarta.persistence.*;

/**
 * Entity representing the many-to-many relationship between products and
 * colors.
 * Associates specific color options with products.
 */
@Entity
@Table(name = "product_colors")
public class ProductColor {

    /** The unique identifier of the product-color association */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The product in this association */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** The color in this association */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", nullable = false)
    private Color color;

    /**
     * Default constructor.
     */
    public ProductColor() {
    }

    /**
     * Constructs a ProductColor association.
     *
     * @param product the product
     * @param color   the color option
     */
    public ProductColor(Product product, Color color) {
        this.product = product;
        this.color = color;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Returns a string representation of this product-color association.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "ProductColor{" +
                "id=" + id +
                ", product=" + (product != null ? product.getName() : "null") +
                ", color=" + (color != null ? color.getName() : "null") +
                '}';
    }
}