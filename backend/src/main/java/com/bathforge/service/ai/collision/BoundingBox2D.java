package com.bathforge.service.ai.collision;

/**
 * Represents a 2D axis-aligned bounding box on the XZ plane.
 * Used for collision detection between bathroom products.
 */
public class BoundingBox2D {
    private final double minX;
    private final double maxX;
    private final double minZ;
    private final double maxZ;

    /**
     * Create a bounding box centered at the given position with the given dimensions.
     * The box is axis-aligned (no rotation support in this version).
     *
     * @param position The center position of the product
     * @param dimensions The product dimensions
     */
    public BoundingBox2D(Position3D position, ProductDimensions dimensions) {
        double halfWidth = dimensions.getWidth() / 2.0;
        double halfDepth = dimensions.getDepth() / 2.0;

        this.minX = position.x - halfWidth;
        this.maxX = position.x + halfWidth;
        this.minZ = position.z - halfDepth;
        this.maxZ = position.z + halfDepth;
    }

    /**
     * Create a bounding box with explicit bounds.
     *
     * @param minX Minimum X coordinate
     * @param maxX Maximum X coordinate
     * @param minZ Minimum Z coordinate
     * @param maxZ Maximum Z coordinate
     */
    public BoundingBox2D(double minX, double maxX, double minZ, double maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    /**
     * Check if this bounding box intersects with another, with an optional buffer distance.
     * Uses AABB (Axis-Aligned Bounding Box) intersection test.
     *
     * @param other The other bounding box
     * @param bufferDistance Additional buffer zone around boxes (meters)
     * @return true if the boxes intersect (considering buffer), false otherwise
     */
    public boolean intersects(BoundingBox2D other, double bufferDistance) {
        // AABB intersection test with buffer
        // Boxes intersect if they overlap on both X and Z axes
        boolean xOverlap = !(this.maxX + bufferDistance < other.minX ||
                             this.minX - bufferDistance > other.maxX);
        boolean zOverlap = !(this.maxZ + bufferDistance < other.minZ ||
                             this.minZ - bufferDistance > other.maxZ);

        return xOverlap && zOverlap;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxZ() {
        return maxZ;
    }
}
