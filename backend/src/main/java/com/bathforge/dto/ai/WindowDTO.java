package com.bathforge.dto.ai;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for window specifications in room configurations.
 */
public class WindowDTO {

    /** The unique identifier for the window */
    @NotNull(message = "Window ID is required")
    private String id;

    /** The index of the wall where the window is located */
    @NotNull(message = "Window wall index is required")
    private Integer wallIndex;

    /** The position of the window along the wall (0.0 to 1.0) */
    @NotNull(message = "Window position is required")
    @DecimalMin(value = "0.0", message = "Window position must be at least 0")
    @DecimalMax(value = "1.0", message = "Window position cannot exceed 1")
    private Double position;

    /** The width of the window in meters (0.5 to 2.0) */
    @NotNull(message = "Window width is required")
    @DecimalMin(value = "0.5", message = "Window width must be at least 0.5 meters")
    @DecimalMax(value = "2.0", message = "Window width cannot exceed 2.0 meters")
    private Double width;

    /** The height of the window in meters (0.5 to 1.5) */
    @NotNull(message = "Window height is required")
    @DecimalMin(value = "0.5", message = "Window height must be at least 0.5 meters")
    @DecimalMax(value = "1.5", message = "Window height cannot exceed 1.5 meters")
    private Double height;

    /** The elevation of the window from the floor in meters (0.5 to 1.5) */
    @NotNull(message = "Window elevation is required")
    @DecimalMin(value = "0.5", message = "Window elevation must be at least 0.5 meters")
    @DecimalMax(value = "1.5", message = "Window elevation cannot exceed 1.5 meters")
    private Double elevation;

    // Constructors
    public WindowDTO() {
    }

    public WindowDTO(String id, Integer wallIndex, Double position, Double width, Double height, Double elevation) {
        this.id = id;
        this.wallIndex = wallIndex;
        this.position = position;
        this.width = width;
        this.height = height;
        this.elevation = elevation;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getWallIndex() {
        return wallIndex;
    }

    public void setWallIndex(Integer wallIndex) {
        this.wallIndex = wallIndex;
    }

    public Double getPosition() {
        return position;
    }

    public void setPosition(Double position) {
        this.position = position;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    @Override
    public String toString() {
        return "WindowDTO{" +
                "id='" + id + '\'' +
                ", wallIndex=" + wallIndex +
                ", position=" + position +
                ", width=" + width +
                ", height=" + height +
                ", elevation=" + elevation +
                '}';
    }
}
