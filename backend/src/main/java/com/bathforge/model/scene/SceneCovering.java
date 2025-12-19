package com.bathforge.model.scene;

import com.bathforge.model.products.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Entity representing a surface covering in a scene.
 * Associates a product (material/texture) with a specific surface in the room.
 */
@Entity
@Table(name = "scene_coverings")
public class SceneCovering {

    /** The unique identifier of the scene covering */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The scene this covering belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    @NotNull(message = "Scene is required")
    private Scene scene;

    /** The product used as the covering material */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product (covering) is required")
    private Product product;

    /**
     * The type of surface this covering is applied to (e.g., wall, floor, ceiling)
     */
    @Column(name = "surface_type")
    @NotNull(message = "Surface type is required")
    private String surfaceType;

    /** Optional identifier for the specific surface */
    @Column(name = "surface_identifier")
    private String surfaceIdentifier;

    /** Horizontal texture repeat factor */
    @Column(name = "repeat_x")
    private Double repeatX = 1.0;

    /** Vertical texture repeat factor */
    @Column(name = "repeat_y")
    private Double repeatY = 1.0;

    /** JSON string containing additional material properties */
    @Column(name = "material_properties", columnDefinition = "TEXT")
    private String materialProperties;

    /**
     * Default constructor.
     */
    public SceneCovering() {
    }

    /**
     * Constructs a SceneCovering with required fields.
     *
     * @param scene             the scene this covering belongs to
     * @param product           the product used as covering
     * @param surfaceType       the type of surface
     * @param surfaceIdentifier the surface identifier
     */
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

    /**
     * Returns a string representation of this scene covering.
     *
     * @return string representation
     */
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