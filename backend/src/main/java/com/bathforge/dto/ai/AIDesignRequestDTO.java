package com.bathforge.dto.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class AIDesignRequestDTO {

    @NotNull(message = "Style is required")
    private String style;

    @NotEmpty(message = "At least one color palette is required")
    @Size(max = 3, message = "Maximum 3 color palettes allowed")
    private List<String> colorPalettes;

    @NotEmpty(message = "At least one feature is required")
    private List<String> features;

    private String priceRange; // LOW, MEDIUM, or HIGH

    @Valid
    private RoomConfigurationDTO roomConfiguration;

    private String additionalRequirements;

    // Constructors
    public AIDesignRequestDTO() {
    }

    public AIDesignRequestDTO(String style, List<String> colorPalettes, List<String> features,
            RoomConfigurationDTO roomConfiguration, String additionalRequirements) {
        this.style = style;
        this.colorPalettes = colorPalettes;
        this.features = features;
        this.roomConfiguration = roomConfiguration;
        this.additionalRequirements = additionalRequirements;
    }

    public AIDesignRequestDTO(String style, List<String> colorPalettes, List<String> features, String priceRange,
            RoomConfigurationDTO roomConfiguration, String additionalRequirements) {
        this.style = style;
        this.colorPalettes = colorPalettes;
        this.features = features;
        this.priceRange = priceRange;
        this.roomConfiguration = roomConfiguration;
        this.additionalRequirements = additionalRequirements;
    }

    // Getters and Setters
    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public List<String> getColorPalettes() {
        return colorPalettes;
    }

    public void setColorPalettes(List<String> colorPalettes) {
        this.colorPalettes = colorPalettes;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public RoomConfigurationDTO getRoomConfiguration() {
        return roomConfiguration;
    }

    public void setRoomConfiguration(RoomConfigurationDTO roomConfiguration) {
        this.roomConfiguration = roomConfiguration;
    }

    public String getAdditionalRequirements() {
        return additionalRequirements;
    }

    public void setAdditionalRequirements(String additionalRequirements) {
        this.additionalRequirements = additionalRequirements;
    }

    @Override
    public String toString() {
        return "AIDesignRequestDTO{" +
                "style='" + style + '\'' +
                ", colorPalettes=" + colorPalettes +
                ", features=" + features +
                ", priceRange='" + priceRange + '\'' +
                ", roomConfiguration=" + roomConfiguration +
                ", additionalRequirements='" + additionalRequirements + '\'' +
                '}';
    }
}