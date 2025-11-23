package com.bathforge.dto.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class RoomConfigurationDTO {

    @Valid
    @NotNull(message = "Room vertices are required")
    @Size(min = 3, message = "Room must have at least 3 vertices")
    private List<VertexDTO> vertices;

    @NotNull(message = "Room height is required")
    @DecimalMin(value = "1.5", message = "Room height must be at least 1.5 meters")
    @DecimalMax(value = "4.0", message = "Room height cannot exceed 4.0 meters")
    private Double height;

    // Constructors
    public RoomConfigurationDTO() {
    }

    public RoomConfigurationDTO(List<VertexDTO> vertices, Double height) {
        this.vertices = vertices;
        this.height = height;
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

    /**
     * Calculate room area based on vertices (using shoelace formula)
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
     * Calculate room volume
     */
    public double calculateVolume() {
        return calculateArea() * (height != null ? height : 2.5);
    }

    @Override
    public String toString() {
        return "RoomConfigurationDTO{" +
                "vertices=" + vertices +
                ", height=" + height +
                ", area=" + String.format("%.2f", calculateArea()) +
                ", volume=" + String.format("%.2f", calculateVolume()) +
                '}';
    }

    public static class VertexDTO {

        @NotNull(message = "Vertex X coordinate is required")
        private Double x;

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