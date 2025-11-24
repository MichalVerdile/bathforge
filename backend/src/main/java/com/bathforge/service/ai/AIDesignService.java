package com.bathforge.service.ai;

import com.bathforge.dto.ai.AIDesignRequestDTO;
import com.bathforge.dto.ai.AIDesignResponseDTO;
import com.bathforge.dto.ai.CoveringRecommendationDTO;
import com.bathforge.dto.ai.ProductRecommendationDTO;
import com.bathforge.dto.products.ProductDTO;
import com.bathforge.dto.scene.CreateSceneDTO;
import com.bathforge.dto.scene.CreateSceneProductDTO;
import com.bathforge.dto.scene.CreateSceneRoomModelDTO;
import com.bathforge.service.products.ProductService;
import com.bathforge.service.scene.SceneService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AIDesignService {

    private static final Logger logger = LoggerFactory.getLogger(AIDesignService.class);

    private final ProductService productService;
    private final OpenAIPromptService promptService;
    private final SceneService sceneService;
    private final ObjectMapper objectMapper;

    @Autowired
    public AIDesignService(ProductService productService, OpenAIPromptService promptService,
            SceneService sceneService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.promptService = promptService;
        this.sceneService = sceneService;
        this.objectMapper = objectMapper;
    }

    /**
     * Generate an AI-powered bathroom design based on user preferences
     */
    public AIDesignResponseDTO generateDesign(AIDesignRequestDTO request) {
        logger.info("Starting AI design generation for style: {}, features: {}",
                request.getStyle(), request.getFeatures());

        AIDesignResponseDTO response = new AIDesignResponseDTO();
        response.setDesignId(UUID.randomUUID().toString());

        try {
            // Generate product and covering recommendations
            logger.debug("Generating product and covering recommendations...");
            generateProductRecommendations(request, response);
            logger.info("Generated {} product recommendations and {} covering recommendations",
                    response.getProductRecommendations() != null ? response.getProductRecommendations().size() : 0,
                    response.getCoveringRecommendations() != null ? response.getCoveringRecommendations().size() : 0);

            // Generate design metadata
            logger.debug("Generating design metadata...");
            generateDesignMetadata(request, response);

            // Save the design as a scene
            if (response.getStatus() == AIDesignResponseDTO.GenerationStatus.GENERATED) {
                logger.debug("Saving AI-generated design as scene...");
                saveDesignAsScene(request, response);
            }

            logger.info("Successfully completed AI design generation with ID: {}", response.getDesignId());

        } catch (Exception e) {
            logger.error("Failed to generate AI design: {}", e.getMessage(), e);
            response.setStatus(AIDesignResponseDTO.GenerationStatus.FAILED);
            response.setDescription("Design generation failed: " + e.getMessage());
        }

        return response;
    }

    /**
     * Generate design metadata (description, style, colorPalettes, features,
     * sceneConfiguration)
     */
    private void generateDesignMetadata(AIDesignRequestDTO request, AIDesignResponseDTO response) {
        logger.debug("Building design metadata prompt...");

        Map<String, Object> templateData = buildTemplateData(request);
        String prompt = applyTemplate(PROMPTS.DesignMetadataPrompt.getPromptText(), templateData);

        logger.debug("Sending design metadata request to OpenAI...");
        String aiResponse = promptService.generateDesignFromPrompt(prompt);
        logger.debug("Received design metadata response from OpenAI");

        // Parse the metadata JSON response
        try {
            Map<String, Object> metadata = objectMapper.readValue(aiResponse,
                    new TypeReference<Map<String, Object>>() {
                    });

            response.setDescription((String) metadata.get("description"));
            response.setStyle((String) metadata.get("style"));

            @SuppressWarnings("unchecked")
            List<String> colorPalettes = (List<String>) metadata.get("colorPalettes");
            response.setColorPalettes(colorPalettes);

            @SuppressWarnings("unchecked")
            List<String> features = (List<String>) metadata.get("features");
            response.setFeatures(features);

            response.setSceneConfiguration((String) metadata.get("sceneConfiguration"));
            response.setStatus(AIDesignResponseDTO.GenerationStatus.GENERATED);

            logger.info("Successfully parsed design metadata");
        } catch (Exception e) {
            logger.error("Failed to parse design metadata JSON: {}", e.getMessage(), e);
            response.setStatus(AIDesignResponseDTO.GenerationStatus.FAILED);
            throw new RuntimeException("Failed to parse design metadata", e);
        }
    }

    /**
     * Generate product and covering recommendations based on requested features
     */
    private void generateProductRecommendations(AIDesignRequestDTO request, AIDesignResponseDTO response) {
        logger.debug("Building product recommendations prompt...");

        Map<String, Object> templateData = buildTemplateData(request);

        // Fetch all available products with their colors
        logger.debug("Fetching all available products and colors...");
        List<ProductDTO> allProducts = productService.getAllProducts();
        logger.info("Found {} products available for AI selection", allProducts.size());

        // Separate covering products for image analysis
        List<ProductDTO> coveringProducts = allProducts.stream()
                .filter(p -> "coverings".equalsIgnoreCase(p.getCategoryName()))
                .collect(java.util.stream.Collectors.toList());

        logger.info("Found {} covering products for image analysis", coveringProducts.size());

        // Separate non-covering products for GLB model analysis
        List<ProductDTO> modelProducts = allProducts.stream()
                .filter(p -> !"coverings".equalsIgnoreCase(p.getCategoryName()))
                .collect(java.util.stream.Collectors.toList());

        logger.info("Found {} products with 3D models for analysis", modelProducts.size());

        // Format products as JSON for the prompt
        String productsJson = formatProductsForPrompt(allProducts);
        templateData.put("availableProducts", productsJson);

        // Load and format covering images as base64 data
        Map<Long, String> coveringImages = loadCoveringImages(coveringProducts);
        String coveringImagesData = formatCoveringImagesForPrompt(coveringProducts, coveringImages);
        templateData.put("coveringImagesData", coveringImagesData);

        // Load and format 3D model data as base64
        Map<Long, String> productModels = loadProductModels(modelProducts);
        String productModelsData = formatProductModelsForPrompt(modelProducts, productModels);
        templateData.put("productModelsData", productModelsData);

        String prompt = applyTemplate(PROMPTS.ProductRecommendationPrompt.getPromptText(), templateData);

        String aiResponse = promptService.generateDesignFromPrompt(prompt);

        logger.debug("Received product recommendations response from OpenAI");

        parseRecommendations(aiResponse, response);
        logger.info("Parsed {} product and {} covering recommendations from AI response",
                response.getProductRecommendations().size(),
                response.getCoveringRecommendations() != null ? response.getCoveringRecommendations().size() : 0);
    }

    /**
     * Load covering product images as base64 encoded data
     */
    private Map<Long, String> loadCoveringImages(List<ProductDTO> coveringProducts) {
        Map<Long, String> imageData = new HashMap<>();

        for (ProductDTO product : coveringProducts) {
            try {
                String imageUrl = product.getThumbnail() != null ? product.getThumbnail() : product.getModelPath();

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Check if it's an image file (not a 3D model)
                    if (isImageFile(imageUrl)) {
                        String base64Image = promptService.loadImageAsBase64(imageUrl);
                        imageData.put(product.getId(), base64Image);
                        logger.debug("Loaded image for covering product: {} (ID: {})", product.getName(),
                                product.getId());
                    } else {
                        logger.debug("Skipping non-image file for product {}: {}", product.getId(), imageUrl);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to load image for covering product {} (ID: {}): {}",
                        product.getName(), product.getId(), e.getMessage());
            }
        }

        logger.info("Successfully loaded {} covering images out of {} products",
                imageData.size(), coveringProducts.size());
        return imageData;
    }

    /**
     * Check if URL points to an image file
     */
    private boolean isImageFile(String url) {
        if (url == null)
            return false;
        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg") ||
                lowerUrl.endsWith(".png") || lowerUrl.endsWith(".webp") ||
                lowerUrl.endsWith(".gif") || lowerUrl.endsWith(".bmp");
    }

    /**
     * Check if URL points to a GLB model file
     */
    private boolean isGlbFile(String url) {
        if (url == null)
            return false;
        return url.toLowerCase().endsWith(".glb");
    }

    /**
     * Load product 3D models as base64 encoded data
     */
    private Map<Long, String> loadProductModels(List<ProductDTO> products) {
        Map<Long, String> modelData = new HashMap<>();

        for (ProductDTO product : products) {
            try {
                String modelUrl = product.getModelPath();

                if (modelUrl != null && !modelUrl.isEmpty() && isGlbFile(modelUrl)) {
                    String base64Model = promptService.loadImageAsBase64(modelUrl);
                    modelData.put(product.getId(), base64Model);
                    logger.debug("Loaded GLB model for product: {} (ID: {})", product.getName(), product.getId());
                }
            } catch (Exception e) {
                logger.warn("Failed to load GLB model for product {} (ID: {}): {}",
                        product.getName(), product.getId(), e.getMessage());
            }
        }

        logger.info("Successfully loaded {} GLB models out of {} products",
                modelData.size(), products.size());
        return modelData;
    }

    /**
     * Build context text for product models with embedded base64 data
     */
    private String formatProductModelsForPrompt(List<ProductDTO> products, Map<Long, String> modelData) {
        if (modelData.isEmpty()) {
            return "No 3D model data available.";
        }

        StringBuilder data = new StringBuilder();

        for (ProductDTO product : products) {
            if (modelData.containsKey(product.getId())) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("productId", product.getId());
                productMap.put("productName", product.getName());
                productMap.put("category", product.getCategoryName());
                productMap.put("availableColors", product.getAvailableColors().stream()
                        .map(c -> c.getName())
                        .collect(java.util.stream.Collectors.toList()));

                // Include first 200 characters of base64 as sample
                String base64Data = modelData.get(product.getId());
                String base64Sample = base64Data.length() > 200
                        ? base64Data.substring(0, 200) + "... [truncated]"
                        : base64Data;
                productMap.put("modelDataSample", base64Sample);
                productMap.put("modelDataLength", base64Data.length());

                try {
                    data.append(objectMapper.writeValueAsString(productMap));
                    data.append("\n");
                } catch (Exception e) {
                    logger.error("Failed to serialize product model data for product {}", product.getId(), e);
                }
            }
        }

        return data.toString();
    }

    /**
     * Build context text for covering images with embedded base64 data
     */
    private String formatCoveringImagesForPrompt(List<ProductDTO> coveringProducts, Map<Long, String> imageData) {
        if (imageData.isEmpty()) {
            return "No covering images available.";
        }

        StringBuilder data = new StringBuilder();

        for (ProductDTO product : coveringProducts) {
            if (imageData.containsKey(product.getId())) {
                Map<String, Object> coveringMap = new HashMap<>();
                coveringMap.put("productId", product.getId());
                coveringMap.put("productName", product.getName());
                coveringMap.put("category", product.getCategoryName());
                coveringMap.put("availableColors", product.getAvailableColors().stream()
                        .map(c -> c.getName())
                        .collect(java.util.stream.Collectors.toList()));

                // Include first 200 characters of base64 as sample
                String base64Data = imageData.get(product.getId());
                String base64Sample = base64Data.length() > 200
                        ? base64Data.substring(0, 200) + "... [truncated]"
                        : base64Data;
                coveringMap.put("imageDataSample", base64Sample);
                coveringMap.put("imageDataLength", base64Data.length());

                try {
                    data.append(objectMapper.writeValueAsString(coveringMap));
                    data.append("\n");
                } catch (Exception e) {
                    logger.error("Failed to serialize covering image data for product {}", product.getId(), e);
                }
            }
        }

        return data.toString();
    }

    /**
     * Save the AI-generated design as a scene in the database
     */
    private void saveDesignAsScene(AIDesignRequestDTO request, AIDesignResponseDTO response) {
        try {
            logger.debug("Creating scene DTO from AI design...");

            CreateSceneDTO sceneDTO = new CreateSceneDTO();
            sceneDTO.setName("AI Design - " + response.getStyle());
            sceneDTO.setDescription(response.getDescription());
            sceneDTO.setUser("guest");
            sceneDTO.setIsPublic(false);
            sceneDTO.setBackgroundColor("#0f172a");

            // Convert product recommendations to scene products
            List<CreateSceneProductDTO> sceneProducts = new ArrayList<>();
            for (ProductRecommendationDTO rec : response.getProductRecommendations()) {
                CreateSceneProductDTO productDTO = new CreateSceneProductDTO();
                productDTO.setProductId(rec.getProductId());

                // Find matching color ID if color name is specified
                if (rec.getColor() != null) {
                    try {
                        var colors = productService.getColorsForProduct(rec.getProductId());
                        colors.stream()
                                .filter(c -> c.getName().equalsIgnoreCase(rec.getColor()))
                                .findFirst()
                                .ifPresent(c -> productDTO.setColorId(c.getId()));
                    } catch (Exception e) {
                        logger.warn("Could not find color '{}' for product {}", rec.getColor(), rec.getProductId());
                    }
                }

                productDTO.setPositionX(rec.getPositionX());
                productDTO.setPositionY(rec.getPositionY());
                productDTO.setPositionZ(rec.getPositionZ());
                productDTO.setRotationX(rec.getRotationX() != null ? rec.getRotationX() : 0.0);
                productDTO.setRotationY(rec.getRotationY() != null ? rec.getRotationY() : 0.0);
                productDTO.setRotationZ(rec.getRotationZ() != null ? rec.getRotationZ() : 0.0);
                productDTO.setScaleX(1.0);
                productDTO.setScaleY(1.0);
                productDTO.setScaleZ(1.0);

                sceneProducts.add(productDTO);
            }
            sceneDTO.setProducts(sceneProducts);

            // Add room model if available
            if (request.getRoomConfiguration() != null) {
                CreateSceneRoomModelDTO roomModelDTO = new CreateSceneRoomModelDTO();

                // Convert vertices to JSON string
                try {
                    String verticesJson = objectMapper.writeValueAsString(
                            request.getRoomConfiguration().getVertices());
                    roomModelDTO.setVerticesData(verticesJson);
                } catch (Exception e) {
                    logger.error("Failed to serialize room vertices", e);
                    throw new RuntimeException("Failed to serialize room vertices", e);
                }

                roomModelDTO.setRoomHeight(request.getRoomConfiguration().getHeight());
                roomModelDTO.setModelType("CUSTOM");
                sceneDTO.setRoomModel(roomModelDTO);
            }

            // Save the scene
            logger.debug("Saving scene to database...");
            var savedScene = sceneService.createScene(sceneDTO);
            logger.info("Successfully saved AI design as scene with ID: {}", savedScene.getId());

        } catch (Exception e) {
            logger.error("Failed to save AI design as scene: {}", e.getMessage(), e);
            // Don't throw - we still want to return the design even if scene save fails
        }
    }

    /**
     * Build template data map for prompt generation
     */
    private Map<String, Object> buildTemplateData(AIDesignRequestDTO request) {
        Map<String, Object> map = new HashMap<>();
        map.put("style", request.getStyle());
        map.put("colorPalettes", request.getColorPalettes());
        map.put("features", request.getFeatures());

        if (request.getRoomConfiguration() != null) {
            map.put("roomConfiguration.height", request.getRoomConfiguration().getHeight());
            map.put("roomConfiguration.vertices", request.getRoomConfiguration().getVertices());
        } else {
            map.put("roomConfiguration.height", "2.5");
            map.put("roomConfiguration.vertices", "[]");
        }

        map.put("additionalRequirements",
                request.getAdditionalRequirements() != null ? request.getAdditionalRequirements() : "None");

        return map;
    }

    /**
     * Apply template substitution
     */
    private String applyTemplate(String template, Map<String, Object> values) {
        String result = template;
        for (var entry : values.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            result = result.replace(key, String.valueOf(entry.getValue()));
        }
        return result;
    }

    /**
     * Format products as JSON string for OpenAI prompt
     */
    private String formatProductsForPrompt(List<ProductDTO> products) {
        try {
            List<Map<String, Object>> formattedProducts = new ArrayList<>();

            for (ProductDTO product : products) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("productId", product.getId());
                productMap.put("productName", product.getName());
                productMap.put("description", product.getDescription());
                productMap.put("category", product.getCategoryName());
                productMap.put("categoryId", product.getCategoryId());
                productMap.put("priceRange", product.getPriceRange());
                productMap.put("mountingType", product.getMountingType());

                // Add available colors
                List<String> colorNames = product.getAvailableColors().stream()
                        .map(color -> color.getName())
                        .collect(java.util.stream.Collectors.toList());
                productMap.put("availableColors", colorNames);

                formattedProducts.add(productMap);
            }

            return objectMapper.writeValueAsString(formattedProducts);
        } catch (Exception e) {
            logger.error("Failed to format products as JSON: {}", e.getMessage(), e);
            return "[]";
        }
    }

    /**
     * Parse product and covering recommendations from JSON response
     */
    private void parseRecommendations(String json, AIDesignResponseDTO response) {
        try {
            // Parse the response which contains both products and coverings
            Map<String, Object> aiResult = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });

            // Parse products array
            if (aiResult.containsKey("products")) {
                String productsJson = objectMapper.writeValueAsString(aiResult.get("products"));
                List<ProductRecommendationDTO> products = objectMapper.readValue(productsJson,
                        new TypeReference<List<ProductRecommendationDTO>>() {
                        });
                response.setProductRecommendations(products);
            }

            // Parse coverings array
            if (aiResult.containsKey("coverings")) {
                String coveringsJson = objectMapper.writeValueAsString(aiResult.get("coverings"));
                List<CoveringRecommendationDTO> coverings = objectMapper.readValue(coveringsJson,
                        new TypeReference<List<CoveringRecommendationDTO>>() {
                        });
                response.setCoveringRecommendations(coverings);
            }
        } catch (Exception e) {
            logger.error("Failed to parse AI recommendations JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse AI recommendations JSON", e);
        }
    }

    /**
     * Validate design request
     */
    public boolean validateRequest(AIDesignRequestDTO request) {
        if (request == null) {
            logger.warn("Validation failed: request is null");
            return false;
        }

        boolean isValid = request.getStyle() != null && !request.getStyle().trim().isEmpty() &&
                request.getColorPalettes() != null && !request.getColorPalettes().isEmpty() &&
                request.getFeatures() != null && !request.getFeatures().isEmpty();

        if (!isValid) {
            logger.warn("Validation failed for request: {}", request);
        }

        return isValid;
    }

    /**
     * Test OpenAI connection with a simple prompt
     */
    public String testOpenAIConnection(String testPrompt) {
        logger.info("Testing OpenAI connection with prompt: {}", testPrompt);

        try {
            String response = promptService.generateDesignFromPrompt(testPrompt);
            logger.info("OpenAI test successful");
            return response;
        } catch (Exception e) {
            logger.error("OpenAI test failed: {}", e.getMessage(), e);
            throw new RuntimeException("OpenAI connection test failed: " + e.getMessage(), e);
        }
    }
}