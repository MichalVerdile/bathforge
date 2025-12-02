package com.bathforge.service.ai.collision;

/**
 * Represents a 3D position in space.
 * Immutable value object for position calculations.
 */
public class Position3D {
    public final double x;
    public final double y;
    public final double z;

    public Position3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
