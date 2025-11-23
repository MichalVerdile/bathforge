package com.bathforge.dto.ai;

import java.time.LocalDateTime;
import java.util.List;

public class AIDesignResponseDTO {

    private String designId;
    private String generatedPrompt;
    private String description;
    private String style;
    private List<String> colorPalettes;
    private List<String> features;
    private List<ProductRecommendationDTO> productRecommendations;
    private List<CoveringRecommendationDTO> coveringRecommendations;
    private String sceneConfiguration;
    private LocalDateTime generatedAt;
    private GenerationStatus status;

    public enum GenerationStatus {
        PENDING,
        GENERATED,
        FAILED
    }

    // Constructors
    public AIDesignResponseDTO() {
        this.generatedAt = LocalDateTime.now();
        this.status = GenerationStatus.PENDING;
    }

    public AIDesignResponseDTO(String designId, String generatedPrompt, String description) {
        this();
        this.designId = designId;
        this.generatedPrompt = generatedPrompt;
        this.description = description;
    }

    // Getters and Setters
    public String getDesignId() {
        return designId;
    }

    public void setDesignId(String designId) {
        this.designId = designId;
    }

    public String getGeneratedPrompt() {
        return generatedPrompt;
    }

    public void setGeneratedPrompt(String generatedPrompt) {
        this.generatedPrompt = generatedPrompt;
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

    public String getSceneConfiguration() {
        return sceneConfiguration;
    }

    public void setSceneConfiguration(String sceneConfiguration) {
        this.sceneConfiguration = sceneConfiguration;
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