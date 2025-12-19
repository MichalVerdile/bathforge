package com.bathforge.service.ai.collision;

/**
 * Represents a 3D position in space.
 * Immutable value object for position calculations.
 */
public class Position3D {
    /** X-coordinate in 3D space */
    public final double x;

    /** Y-coordinate in 3D space (typically height) */
    public final double y;

    /** Z-coordinate in 3D space */
    public final double z;

    /**
     * Constructs a 3D position with the specified coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     */
    public Position3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
