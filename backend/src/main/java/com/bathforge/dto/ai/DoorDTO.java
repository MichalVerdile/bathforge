package com.bathforge.dto.ai;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for door specifications in room configurations.
 */
public class DoorDTO {

    /** The unique identifier for the door */
    @NotNull(message = "Door ID is required")
    private String id;

    /** The index of the wall where the door is located */
    @NotNull(message = "Door wall index is required")
    private Integer wallIndex;

    /** The position of the door along the wall (0.0 to 1.0) */
    @NotNull(message = "Door position is required")
    @DecimalMin(value = "0.0", message = "Door position must be at least 0")
    @DecimalMax(value = "1.0", message = "Door position cannot exceed 1")
    private Double position;

    /** The width of the door in meters (0.6 to 1.2) */
    @NotNull(message = "Door width is required")
    @DecimalMin(value = "0.6", message = "Door width must be at least 0.6 meters")
    @DecimalMax(value = "1.2", message = "Door width cannot exceed 1.2 meters")
    private Double width;

    /** The height of the door in meters (1.8 to 2.4) */
    @NotNull(message = "Door height is required")
    @DecimalMin(value = "1.8", message = "Door height must be at least 1.8 meters")
    @DecimalMax(value = "2.4", message = "Door height cannot exceed 2.4 meters")
    private Double height;

    // Constructors
    public DoorDTO() {
    }

    public DoorDTO(String id, Integer wallIndex, Double position, Double width, Double height) {
        this.id = id;
        this.wallIndex = wallIndex;
        this.position = position;
        this.width = width;
        this.height = height;
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

    @Override
    public String toString() {
        return "DoorDTO{" +
                "id='" + id + '\'' +
                ", wallIndex=" + wallIndex +
                ", position=" + position +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
