package com.bathforge.dto.ai;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for AI-powered bathroom design generation responses.
 */
public class AIDesignResponseDTO {

    /** The unique identifier for the generated design */
    private String designId;
    /** The description of the generated design */
    private String description;
    /** The style of the generated design */
    private String style;
    /** The color palettes used in the design */
    private List<String> colorPalettes;
    /** The features included in the design */
    private List<String> features;
    /** Product recommendations for the design */
    private List<ProductRecommendationDTO> productRecommendations;
    /** Covering recommendations for the design */
    private List<CoveringRecommendationDTO> coveringRecommendations;
    /** Timestamp when the design was generated */
    private LocalDateTime generatedAt;
    /** The status of the design generation process */
    private GenerationStatus status;

    /**
     * Enum representing the status of design generation.
     */
    public enum GenerationStatus {
        /** Design generation is pending */
        PENDING,
        /** Design has been successfully generated */
        GENERATED,
        /** Design generation has failed */
        FAILED
    }

    // Constructors
    public AIDesignResponseDTO() {
        this.generatedAt = LocalDateTime.now();
        this.status = GenerationStatus.PENDING;
    }

    public AIDesignResponseDTO(String designId, String description) {
        this();
        this.designId = designId;
        this.description = description;
    }

    // Getters and Setters
    public String getDesignId() {
        return designId;
    }

    public void setDesignId(String designId) {
        this.designId = designId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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

    public List<ProductRecommendationDTO> getProductRecommendations() {
        return productRecommendations;
    }

    public void setProductRecommendations(List<ProductRecommendationDTO> productRecommendations) {
        this.productRecommendations = productRecommendations;
    }

    public List<CoveringRecommendationDTO> getCoveringRecommendations() {
        return coveringRecommendations;
    }

    public void setCoveringRecommendations(List<CoveringRecommendationDTO> coveringRecommendations) {
        this.coveringRecommendations = coveringRecommendations;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public GenerationStatus getStatus() {
        return status;
    }

    public void setStatus(GenerationStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AIDesignResponseDTO{" +
                "designId='" + designId + '\'' +
                ", description='" + description + '\'' +
                ", style='" + style + '\'' +
                ", status=" + status +
                ", generatedAt=" + generatedAt +
                '}';
    }
}