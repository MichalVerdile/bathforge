package com.bathforge.dto.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Data Transfer Object for room configuration specifications.
 */
public class RoomConfigurationDTO {

    /** The vertices defining the room shape (minimum 3 required) */
    @Valid
    @NotNull(message = "Room vertices are required")
    @Size(min = 3, message = "Room must have at least 3 vertices")
    private List<VertexDTO> vertices;

    /** The height of the room in meters (1.5 to 4.0) */
    @NotNull(message = "Room height is required")
    @DecimalMin(value = "1.5", message = "Room height must be at least 1.5 meters")
    @DecimalMax(value = "4.0", message = "Room height cannot exceed 4.0 meters")
    private Double height;

    /** The doors in the room */
    @Valid
    private List<DoorDTO> doors;

    /** The windows in the room */
    @Valid
    private List<WindowDTO> windows;

    // Constructors
    public RoomConfigurationDTO() {
    }

    public RoomConfigurationDTO(List<VertexDTO> vertices, Double height) {
        this.vertices = vertices;
        this.height = height;
    }

    public RoomConfigurationDTO(List<VertexDTO> vertices, Double height, List<DoorDTO> doors, List<WindowDTO> windows) {
        this.vertices = vertices;
        this.height = height;
        this.doors = doors;
        this.windows = windows;
    }

    // Getters and Setters
    public List<VertexDTO> getVertices() {
        return vertices;
    }

    public void setVertices(List<VertexDTO> vertices) {
        this.vertices = vertices;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public List<DoorDTO> getDoors() {
        return doors;
    }

    public void setDoors(List<DoorDTO> doors) {
        this.doors = doors;
    }

    public List<WindowDTO> getWindows() {
        return windows;
    }

    public void setWindows(List<WindowDTO> windows) {
        this.windows = windows;
    }

    /**
     * Calculates room area based on vertices using the shoelace formula.
     *
     * @return the area of the room in square meters
     */
    public double calculateArea() {
        if (vertices == null || vertices.size() < 3) {
            return 0.0;
        }

        double area = 0.0;
        int n = vertices.size();

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += vertices.get(i).getX() * vertices.get(j).getY();
            area -= vertices.get(j).getX() * vertices.get(i).getY();
        }

        return Math.abs(area) / 2.0;
    }

    /**
     * Calculates room volume based on area and height.
     *
     * @return the volume of the room in cubic meters
     */
    public double calculateVolume() {
        return calculateArea() * (height != null ? height : 2.5);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RoomConfigurationDTO{");
        sb.append("vertices=").append(vertices);
        sb.append(", height=").append(height);
        sb.append(", area=").append(String.format("%.2f", calculateArea()));
        sb.append(", volume=").append(String.format("%.2f", calculateVolume()));

        if (doors != null && !doors.isEmpty()) {
            sb.append(", doors=").append(doors);
        }
        if (windows != null && !windows.isEmpty()) {
            sb.append(", windows=").append(windows);
        }

        sb.append('}');
        return sb.toString();
    }

    /**
     * Data Transfer Object for 2D vertex coordinates.
     */
    public static class VertexDTO {

        /** The X coordinate of the vertex */
        @NotNull(message = "Vertex X coordinate is required")
        private Double x;

        /** The Y coordinate of the vertex */
        @NotNull(message = "Vertex Y coordinate is required")
        private Double y;

        // Constructors
        public VertexDTO() {
        }

        public VertexDTO(Double x, Double y) {
            this.x = x;
            this.y = y;
        }

        // Getters and Setters
        public Double getX() {
            return x;
        }

        public void setX(Double x) {
            this.x = x;
        }

        public Double getY() {
            return y;
        }

        public void setY(Double y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return "VertexDTO{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
}