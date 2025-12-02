package com.bathforge.service.ai.collision;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents the boundaries of a room as a polygon with actual vertices.
 * Supports point-in-polygon testing for custom room shapes.
 */
public class RoomBounds {
    private final double minX;
    private final double maxX;
    private final double minZ;
    private final double maxZ;
    private final List<Position3D> vertices;

    public RoomBounds(double minX, double maxX, double minZ, double maxZ) {
        this(minX, maxX, minZ, maxZ, null);
    }

    public RoomBounds(double minX, double maxX, double minZ, double maxZ, List<Position3D> vertices) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.vertices = vertices != null ? new ArrayList<>(vertices) : null;
    }

    /**
     * Check if a bounding box is fully contained within the room.
     *
     * @param box The bounding box to check
     * @return true if all corners are inside the room
     */
    public boolean containsBox(BoundingBox2D box) {
        // Check all four corners of the bounding box
        Position3D[] corners = {
            new Position3D(box.getMinX(), 0, box.getMinZ()),
            new Position3D(box.getMaxX(), 0, box.getMinZ()),
            new Position3D(box.getMinX(), 0, box.getMaxZ()),
            new Position3D(box.getMaxX(), 0, box.getMaxZ())
        };

        for (Position3D corner : corners) {
            // If we have actual vertices, use point-in-polygon test
            if (vertices != null && !vertices.isEmpty()) {
                if (!isPointInPolygon(corner.x, corner.z)) {
                    return false;
                }
            } else {
                // Fallback to bounding box check
                if (!(corner.x >= minX && corner.x <= maxX &&
                      corner.z >= minZ && corner.z <= maxZ)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Point-in-polygon test using ray casting algorithm.
     * Matches the frontend implementation in collisionUtils.ts
     */
    private boolean isPointInPolygon(double x, double z) {
        if (vertices == null || vertices.size() < 3) {
            return false;
        }

        boolean inside = false;
        int n = vertices.size();

        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = vertices.get(i).x;
            double zi = vertices.get(i).z;
            double xj = vertices.get(j).x;
            double zj = vertices.get(j).z;

            boolean intersect = ((zi > z) != (zj > z))
                    && (x < (xj - xi) * (z - zi) / (zj - zi) + xi);

            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }

    /**
     * Get the actual vertices of the room polygon.
     */
    public List<Position3D> getVertices() {
        return vertices != null ? new ArrayList<>(vertices) : null;
    }

    /**
     * Check if this room has actual polygon vertices (not just a bounding box).
     */
    public boolean hasVertices() {
        return vertices != null && !vertices.isEmpty();
    }

    /**
     * Get the center position of the room.
     *
     * @return Center position
     */
    public Position3D getCenter() {
        return new Position3D(
            (minX + maxX) / 2.0,
            0.0,
            (minZ + maxZ) / 2.0
        );
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
