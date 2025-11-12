package com.bathforge.model.scene;

import com.bathforge.model.products.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "scene_coverings")
public class SceneCovering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    @NotNull(message = "Scene is required")
    private Scene scene;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product (covering) is required")
    private Product product;

    @Column(name = "surface_type")
    @NotNull(message = "Surface type is required")
    private String surfaceType;

    @Column(name = "surface_identifier")
    private String surfaceIdentifier;

    @Column(name = "repeat_x")
    private Double repeatX = 1.0;

    @Column(name = "repeat_y")
    private Double repeatY = 1.0;

    @Column(name = "material_properties", columnDefinition = "TEXT")
    private String materialProperties;

    public SceneCovering() {
    }

    public SceneCovering(Scene scene, Product product, String surfaceType, String surfaceIdentifier) {
        this.scene = scene;
        this.product = product;
        this.surfaceType = surfaceType;
        this.surfaceIdentifier = surfaceIdentifier;
    }

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

    public String getSurfaceType() {
        return surfaceType;
    }

    public void setSurfaceType(String surfaceType) {
        this.surfaceType = surfaceType;
    }

    public String getSurfaceIdentifier() {
        return surfaceIdentifier;
    }

    public void setSurfaceIdentifier(String surfaceIdentifier) {
        this.surfaceIdentifier = surfaceIdentifier;
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

    public String getMaterialProperties() {
        return materialProperties;
    }

    public void setMaterialProperties(String materialProperties) {
        this.materialProperties = materialProperties;
    }

    @Override
    public String toString() {
        return "SceneCovering{" +
                "id=" + id +
                ", surfaceType='" + surfaceType + '\'' +
                ", surfaceIdentifier='" + surfaceIdentifier + '\'' +
                ", product=" + (product != null ? product.getName() : "null") +
                ", repeatX=" + repeatX +
                ", repeatY=" + repeatY +
                '}';
    }
}