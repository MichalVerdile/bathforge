package com.bathforge.service.ai.collision;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides approximate dimensions for bathroom products based on category.
 * Dimensions are in meters and match frontend scaling patterns from modelScalingConfig.ts.
 */
public class ProductDimensions {
    private final double width;   // X-axis (meters)
    private final double depth;   // Z-axis (meters)
    private final double height;  // Y-axis (meters)

    private static final Map<String, ProductDimensions> CATEGORY_DIMENSIONS;

    static {
        CATEGORY_DIMENSIONS = new HashMap<>();

        // Dimensions based on typical bathroom product sizes
        CATEGORY_DIMENSIONS.put("basins", new ProductDimensions(0.60, 0.45, 0.20));
        CATEGORY_DIMENSIONS.put("bathtubs", new ProductDimensions(1.70, 0.80, 0.60));
        CATEGORY_DIMENSIONS.put("wcs", new ProductDimensions(0.50, 0.70, 0.80));
        CATEGORY_DIMENSIONS.put("showers", new ProductDimensions(0.90, 0.90, 2.10));
        CATEGORY_DIMENSIONS.put("furniture", new ProductDimensions(0.80, 0.50, 0.85));
        CATEGORY_DIMENSIONS.put("fittings", new ProductDimensions(0.15, 0.15, 0.25));
        CATEGORY_DIMENSIONS.put("accessories", new ProductDimensions(0.14, 0.14, 0.14));
        CATEGORY_DIMENSIONS.put("towelradiators", new ProductDimensions(0.60, 0.15, 1.20));

        // Additional variants with underscores/hyphens
        CATEGORY_DIMENSIONS.put("towel_radiators", new ProductDimensions(0.60, 0.15, 1.20));
        CATEGORY_DIMENSIONS.put("shower_trays", new ProductDimensions(0.90, 0.90, 0.10));
        CATEGORY_DIMENSIONS.put("showertrays", new ProductDimensions(0.90, 0.90, 0.10));
    }

    /**
     * Conservative default for unknown categories: 50cm x 50cm x 80cm
     */
    private static final ProductDimensions DEFAULT_DIMENSIONS =
        new ProductDimensions(0.50, 0.50, 0.80);

    public ProductDimensions(double width, double depth, double height) {
        this.width = width;
        this.depth = depth;
        this.height = height;
    }

    /**
     * Get dimensions for a product category.
     * Category names are normalized (lowercased, spaces/underscores/hyphens removed).
     *
     * @param category The product category (e.g., "Basins", "Bath Tubs", "WCs")
     * @return ProductDimensions for the category, or default dimensions if unknown
     */
    public static ProductDimensions forCategory(String category) {
        if (category == null || category.isEmpty()) {
            return DEFAULT_DIMENSIONS;
        }

        // Normalize: lowercase, remove spaces, underscores, hyphens
        String normalized = category.toLowerCase()
            .replaceAll("[\\s_-]", "");

        return CATEGORY_DIMENSIONS.getOrDefault(normalized, DEFAULT_DIMENSIONS);
    }

    /**
     * Calculate the footprint (floor area) of this product.
     * Useful for sorting products by size before placement.
     *
     * @return Floor area in square meters
     */
    public double getFootprint() {
        return width * depth;
    }

    public double getWidth() {
        return width;
    }

    public double getDepth() {
        return depth;
    }
}
