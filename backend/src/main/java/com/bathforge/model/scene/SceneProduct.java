package com.bathforge.model.scene;

import com.bathforge.model.products.Product;
import com.bathforge.model.products.Color;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "scene_products")
public class SceneProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    @NotNull(message = "Scene is required")
    private Scene scene;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id")
    private Color color; // Can be null if no specific color is chosen

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

    @Column(name = "position_z")
    private Double positionZ;

    @Column(name = "rotation_x")
    private Double rotationX;

    @Column(name = "rotation_y")
    private Double rotationY;

    @Column(name = "rotation_z")
    private Double rotationZ;

    @Column(name = "scale_x")
    private Double scaleX = 1.0;

    @Column(name = "scale_y")
    private Double scaleY = 1.0;

    @Column(name = "scale_z")
    private Double scaleZ = 1.0;

    @Column(name = "custom_properties", columnDefinition = "TEXT")
    private String customProperties; // JSON string for any additional properties

    public SceneProduct() {
    }

    public SceneProduct(Scene scene, Product product, Color color,
            Double positionX, Double positionY, Double positionZ,
            Double rotationX, Double rotationY, Double rotationZ) {
        this.scene = scene;
        this.product = product;
        this.color = color;
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
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

    public Double getScaleX() {
        return scaleX;
    }

    public void setScaleX(Double scaleX) {
        this.scaleX = scaleX;
    }

    public Double getScaleY() {
        return scaleY;
    }

    public void setScaleY(Double scaleY) {
        this.scaleY = scaleY;
    }

    public Double getScaleZ() {
        return scaleZ;
    }

    public void setScaleZ(Double scaleZ) {
        this.scaleZ = scaleZ;
    }

    public String getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(String customProperties) {
        this.customProperties = customProperties;
    }

    @Override
    public String toString() {
        return "SceneProduct{" +
                "id=" + id +
                ", product=" + (product != null ? product.getName() : "null") +
                ", color=" + (color != null ? color.getName() : "null") +
                ", position=[" + positionX + ", " + positionY + ", " + positionZ + "]" +
                ", rotation=[" + rotationX + ", " + rotationY + ", " + rotationZ + "]" +
                ", scale=[" + scaleX + ", " + scaleY + ", " + scaleZ + "]" +
                '}';
    }
}