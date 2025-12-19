package com.bathforge.service.ai;

import com.bathforge.dto.ai.AIDesignRequestDTO;
import com.bathforge.dto.ai.AIDesignResponseDTO;
import com.bathforge.dto.ai.CoveringRecommendationDTO;
import com.bathforge.dto.ai.ProductRecommendationDTO;
import com.bathforge.dto.ai.RoomConfigurationDTO;
import com.bathforge.dto.ai.RoomConfigurationDTO.VertexDTO;
import com.bathforge.dto.products.ProductDTO;
import com.bathforge.dto.scene.CreateSceneDTO;
import com.bathforge.dto.scene.CreateSceneProductDTO;
import com.bathforge.dto.scene.CreateSceneRoomModelDTO;
import com.bathforge.service.ai.collision.BoundingBox2D;
import com.bathforge.service.ai.collision.Position3D;
import com.bathforge.service.ai.collision.ProductDimensions;
import com.bathforge.service.ai.collision.RoomBounds;
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
import java.util.stream.Collectors;

/**
 * Service for AI-powered bathroom design generation.
 * Integrates with OpenAI to generate product recommendations and positioning
 * based on user preferences.
 * Handles intelligent product placement with collision detection and room
 * boundary validation.
 */
@Service
public class AIDesignService {

    private static final Logger logger = LoggerFactory.getLogger(AIDesignService.class);

    private final ProductService productService;
    private final OpenAIPromptService promptService;
    private final SceneService sceneService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs an AIDesignService with required dependencies.
     *
     * @param productService service for product operations
     * @param promptService  service for OpenAI prompt processing
     * @param sceneService   service for scene management
     * @param objectMapper   JSON object mapper
     */
    @Autowired
    public AIDesignService(ProductService productService, OpenAIPromptService promptService,
            SceneService sceneService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.promptService = promptService;
        this.sceneService = sceneService;
        this.objectMapper = objectMapper;
    }

    /**
     * Generates an AI-powered bathroom design based on user preferences.
     * Orchestrates the entire design generation process including product
     * recommendations,
     * positioning, and scene creation.
     *
     * @param request the design request containing style, features, and room
     *                configuration
     * @return AIDesignResponseDTO containing recommended products and design
     *         details
     */
    public AIDesignResponseDTO generateDesign(AIDesignRequestDTO request) {
        logger.info("Starting AI design generation for style: {}, features: {}",
                request.getStyle(), request.getFeatures());

        AIDesignResponseDTO response = new AIDesignResponseDTO();
        response.setDesignId(UUID.randomUUID().toString());

        try {
            generateProductRecommendations(request, response);
            logger.info("Generated {} product recommendations and {} covering recommendations",
                    response.getProductRecommendations() != null ? response.getProductRecommendations().size() : 0,
                    response.getCoveringRecommendations() != null ? response.getCoveringRecommendations().size() : 0);

            response.setStyle(request.getStyle());
            response.setColorPalettes(request.getColorPalettes());
            response.setFeatures(request.getFeatures());
            response.setDescription("AI-generated bathroom design");
            response.setStatus(AIDesignResponseDTO.GenerationStatus.GENERATED);

            saveDesignAsScene(request, response);

            logger.info("Successfully completed AI design generation with ID: {}", response.getDesignId());

        } catch (Exception e) {
            logger.error("Failed to generate AI design: {}", e.getMessage(), e);
            response.setStatus(AIDesignResponseDTO.GenerationStatus.FAILED);
            response.setDescription("Design generation failed: " + e.getMessage());
        }

        return response;
    }

    /**
     * Generates product and covering recommendations based on requested features.
     * Filters products by price range if specified and sends formatted data to
     * OpenAI.
     *
     * @param request  the design request with preferences
     * @param response the response object to populate with recommendations
     */
    private void generateProductRecommendations(AIDesignRequestDTO request, AIDesignResponseDTO response) {
        logger.debug("Building product recommendations prompt...");

        Map<String, Object> templateData = buildTemplateData(request);

        List<ProductDTO> allProducts = productService.getProductsForAISelection();
        logger.info("Found {} products available for AI selection", allProducts.size());

        if (request.getPriceRange() != null && !request.getPriceRange().isEmpty()) {
            String priceRangeUpper = request.getPriceRange().toUpperCase();
            allProducts = allProducts.stream()
                    .filter(product -> product.getPriceRange() != null &&
                            product.getPriceRange().name().equals(priceRangeUpper))
                    .collect(Collectors.toList());
            logger.info("After price range filter: {} products available", allProducts.size());
        }

        String productsJson = formatProductsForPrompt(allProducts);
        templateData.put("availableProducts", productsJson);

        String prompt = applyTemplate(PROMPTS.ProductRecommendationPrompt.getPromptText(), templateData);
        String aiResponse = promptService.generateDesignFromPrompt(prompt);

        parseRecommendations(aiResponse, response, request);
        logger.info("Parsed {} product and {} covering recommendations from AI response",
                response.getProductRecommendations().size(),
                response.getCoveringRecommendations() != null ? response.getCoveringRecommendations().size() : 0);
    }

    /**
     * Saves the AI-generated design as a scene in the database.
     * Creates scene with products, room model, and associated metadata.
     *
     * @param request  the original design request
     * @param response the generated design response with recommendations
     */
    private void saveDesignAsScene(AIDesignRequestDTO request, AIDesignResponseDTO response) {
        try {
            CreateSceneDTO sceneDTO = new CreateSceneDTO();
            sceneDTO.setName("AI Design - " + response.getStyle());
            sceneDTO.setDescription(response.getDescription());
            sceneDTO.setUser("guest");
            sceneDTO.setIsPublic(false);
            sceneDTO.setBackgroundColor("#0f172a");

            List<CreateSceneProductDTO> sceneProducts = new ArrayList<>();
            for (ProductRecommendationDTO rec : response.getProductRecommendations()) {
                CreateSceneProductDTO productDTO = new CreateSceneProductDTO();
                productDTO.setProductId(rec.getProductId());

                if (rec.getColor() != null && !rec.getColor().isEmpty()) {
                    try {
                        var colors = productService.getColorsForProduct(rec.getProductId());
                        if (colors != null && !colors.isEmpty()) {
                            var matchingColor = colors.stream()
                                    .filter(c -> c.getName().equalsIgnoreCase(rec.getColor()))
                                    .findFirst();

                            if (matchingColor.isPresent()) {
                                productDTO.setColorId(matchingColor.get().getId());
                            } else {
                                if (!colors.isEmpty()) {
                                    productDTO.setColorId(colors.get(0).getId());
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error matching color for product '{}': {}", rec.getProductName(), e.getMessage());
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

            if (request.getRoomConfiguration() != null) {
                CreateSceneRoomModelDTO roomModelDTO = new CreateSceneRoomModelDTO();

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

                try {
                    Map<String, Object> roomProperties = new HashMap<>();
                    if (request.getRoomConfiguration().getDoors() != null
                            && !request.getRoomConfiguration().getDoors().isEmpty()) {
                        roomProperties.put("doors", request.getRoomConfiguration().getDoors());
                    }
                    if (request.getRoomConfiguration().getWindows() != null
                            && !request.getRoomConfiguration().getWindows().isEmpty()) {
                        roomProperties.put("windows", request.getRoomConfiguration().getWindows());
                    }
                    if (!roomProperties.isEmpty()) {
                        String roomPropertiesJson = objectMapper.writeValueAsString(roomProperties);
                        roomModelDTO.setRoomProperties(roomPropertiesJson);
                        logger.info("Saved room properties with doors and windows");
                    }
                } catch (Exception e) {
                    logger.error("Failed to serialize room properties (doors/windows): {}", e.getMessage(), e);
                }

                sceneDTO.setRoomModel(roomModelDTO);
            }

            var savedScene = sceneService.createScene(sceneDTO);
            logger.info("Successfully saved AI design as scene with ID: {}", savedScene.getId());

        } catch (Exception e) {
            logger.error("Failed to save AI design as scene: {}", e.getMessage(), e);
        }
    }

    /**
     * Builds template data map for prompt generation.
     * Extracts relevant information from the request for AI prompt construction.
     *
     * @param request the design request
     * @return map of template variables and their values
     */
    private Map<String, Object> buildTemplateData(AIDesignRequestDTO request) {
        Map<String, Object> map = new HashMap<>();
        map.put("style", request.getStyle());
        map.put("colorPalettes", request.getColorPalettes());
        map.put("features", request.getFeatures());

        String colorPaletteDescriptions = getColorPaletteDescriptions(request.getColorPalettes());
        map.put("colorPaletteDescriptions", colorPaletteDescriptions);

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
     * Gets detailed color descriptions for the selected color palettes.
     * Converts palette names to human-readable color descriptions for AI
     * processing.
     *
     * @param colorPalettes list of color palette identifiers
     * @return formatted string with color descriptions
     */
    private String getColorPaletteDescriptions(List<String> colorPalettes) {
        if (colorPalettes == null || colorPalettes.isEmpty()) {
            return "Neutral colors (White, Beige, Gray)";
        }

        Map<String, String> paletteDescriptions = new HashMap<>();
        paletteDescriptions.put("spa-serenity",
                "Light blue-gray, soft beige, pale tones");
        paletteDescriptions.put("modern-monochrome",
                "Black, white, gray");
        paletteDescriptions.put("natural-warmth",
                "Warm beige, brown, cream");
        paletteDescriptions.put("urban-chic",
                "Gray, silver, chrome");
        paletteDescriptions.put("luxe-dark",
                "Dark charcoal, gold, navy");
        paletteDescriptions.put("sage-stone",
                "Sage green, stone gray, taupe");

        StringBuilder result = new StringBuilder();
        for (String palette : colorPalettes) {
            String description = paletteDescriptions.getOrDefault(palette.toLowerCase(),
                    "Neutral colors (White, Gray, Beige)");
            result.append(description).append(". ");
        }

        return result.toString().trim();
    }

    /**
     * Applies template substitution to replace placeholders with actual values.
     *
     * @param template the template string with {{placeholders}}
     * @param values   map of placeholder names to replacement values
     * @return the processed template with substituted values
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
     * Formats products as JSON string for OpenAI prompt.
     * Serializes product data including categories, colors, and mounting types.
     *
     * @param products list of products to format
     * @return JSON string representation of products, or empty array on error
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
     * Extract room bounds from room configuration.
     * Converts 2D vertices to 3D coordinates and calculates bounding box.
     *
     * @param roomConfig The room configuration from the request
     * @return RoomBounds representing the room's boundaries
     */
    private RoomBounds extractRoomBounds(RoomConfigurationDTO roomConfig) {
        if (roomConfig == null || roomConfig.getVertices() == null || roomConfig.getVertices().isEmpty()) {
            logger.debug("No room configuration provided, using default bounds");
            return new RoomBounds(-1.1, 1.1, -1.1, 1.1);
        }

        List<VertexDTO> vertices = roomConfig.getVertices();

        double centerX = vertices.stream()
                .mapToDouble(VertexDTO::getX)
                .average().orElse(0);
        double centerY = vertices.stream()
                .mapToDouble(VertexDTO::getY)
                .average().orElse(0);

        List<Position3D> vertices3D = new ArrayList<>();
        for (VertexDTO vertex : vertices) {
            double x = (vertex.getX() - centerX) * 0.01;
            double z = (vertex.getY() - centerY) * 0.01;
            vertices3D.add(new Position3D(x, 0, z));
        }

        double minX = vertices3D.stream()
                .mapToDouble(v -> v.x)
                .min().orElse(-1.1);
        double maxX = vertices3D.stream()
                .mapToDouble(v -> v.x)
                .max().orElse(1.1);
        double minZ = vertices3D.stream()
                .mapToDouble(v -> v.z)
                .min().orElse(-1.1);
        double maxZ = vertices3D.stream()
                .mapToDouble(v -> v.z)
                .max().orElse(1.1);

        RoomBounds bounds = new RoomBounds(minX, maxX, minZ, maxZ, vertices3D);
        logger.debug("Extracted room bounds: {} with {} vertices", bounds, vertices3D.size());
        return bounds;
    }

    /**
     * Calculates positions for AI-recommended products using smart placement.
     * Uses actual room vertices for L-shaped, U-shaped, and irregular rooms.
     * Validates that products fit inside the room polygon and don't collide.
     * Bathtubs are always placed in the center when possible.
     *
     * @param products list of recommended products to position
     * @param request  the design request containing room configuration
     */
    private void calculateProductPositions(
            List<ProductRecommendationDTO> products,
            AIDesignRequestDTO request) {
        if (products == null || products.isEmpty()) {
            return;
        }

        logger.info("Calculating positions for {} products using smart placement", products.size());

        RoomBounds room = extractRoomBounds(request.getRoomConfiguration());
        logger.info("Room bounds: {}", room);

        final double SAFETY_BUFFER = 0.25;

        products.sort((a, b) -> {
            boolean aIsBathtub = "bathtubs".equalsIgnoreCase(a.getCategory()) ||
                    "bath tubs".equalsIgnoreCase(a.getCategory()) ||
                    a.getCategory().toLowerCase().contains("bathtub");
            boolean bIsBathtub = "bathtubs".equalsIgnoreCase(b.getCategory()) ||
                    "bath tubs".equalsIgnoreCase(b.getCategory()) ||
                    b.getCategory().toLowerCase().contains("bathtub");

            if (aIsBathtub && !bIsBathtub)
                return 1;
            if (!aIsBathtub && bIsBathtub)
                return -1;

            ProductDimensions dimsA = ProductDimensions.forCategory(a.getCategory());
            ProductDimensions dimsB = ProductDimensions.forCategory(b.getCategory());
            return Double.compare(dimsA.getFootprint(), dimsB.getFootprint());
        });

        logger.info("Sorted products for placement: {}", products.stream()
                .map(p -> p.getProductName() + " (" + p.getCategory() + ")")
                .toList());

        List<BoundingBox2D> usedPositions = new ArrayList<>();

        for (int i = 0; i < products.size(); i++) {
            ProductRecommendationDTO product = products.get(i);

            ProductDimensions dims = ProductDimensions.forCategory(product.getCategory());

            boolean isBathtub = "bathtubs".equalsIgnoreCase(product.getCategory()) ||
                    "bath tubs".equalsIgnoreCase(product.getCategory()) ||
                    product.getCategory().toLowerCase().contains("bathtub");

            Position3D position = findValidPosition(room, dims, usedPositions, SAFETY_BUFFER, isBathtub);

            if (position == null) {
                logger.warn("Could not find valid position for product '{}', using center as fallback",
                        product.getProductName());
                position = room.getCenter();
            }

            double height = getDefaultHeightForMountingType(product.getMountingType());

            product.setPositionX(position.x);
            product.setPositionY(height);
            product.setPositionZ(position.z);
            product.setRotationX(0.0);
            product.setRotationY(0.0);
            product.setRotationZ(0.0);

            BoundingBox2D productBox = new BoundingBox2D(position, dims);
            usedPositions.add(productBox);

            logger.debug("Placed product '{}' at ({}, {}, {})",
                    product.getProductName(),
                    String.format("%.2f", position.x),
                    String.format("%.2f", height),
                    String.format("%.2f", position.z));
        }

        logger.info("Completed positioning {} products using smart placement", products.size());
    }

    /**
     * Finds a valid position for a product that:
     * 1. Is inside the room polygon
     * 2. Doesn't collide with already placed products
     * 3. Has proper clearance from walls
     * 4. Spreads items evenly across the room
     *
     * @param room          the room boundaries
     * @param dims          the product dimensions
     * @param usedPositions list of already occupied positions
     * @param safetyBuffer  minimum clearance from walls and objects
     * @param preferCenter  whether to prefer center placement (for bathtubs)
     * @return valid Position3D or null if no valid position found
     */
    private Position3D findValidPosition(RoomBounds room, ProductDimensions dims,
            List<BoundingBox2D> usedPositions,
            double safetyBuffer, boolean preferCenter) {
        double offset = Math.max(dims.getWidth(), dims.getDepth()) / 2.0 + safetyBuffer;

        if (preferCenter) {
            Position3D center = room.getCenter();
            if (isValidPosition(center, dims, room, usedPositions)) {
                logger.debug("Placing bathtub at center position");
                return center;
            }
        }

        List<Position3D> candidates = new ArrayList<>();

        candidates.add(new Position3D(room.getMinX() + offset, 0, room.getMaxZ() - offset)); // Top-left
        candidates.add(new Position3D(room.getMaxX() - offset, 0, room.getMaxZ() - offset)); // Top-right
        candidates.add(new Position3D(room.getMinX() + offset, 0, room.getMinZ() + offset)); // Bottom-left
        candidates.add(new Position3D(room.getMaxX() - offset, 0, room.getMinZ() + offset)); // Bottom-right

        double midX = (room.getMinX() + room.getMaxX()) / 2.0;
        double midZ = (room.getMinZ() + room.getMaxZ()) / 2.0;
        candidates.add(new Position3D(midX, 0, room.getMaxZ() - offset));
        candidates.add(new Position3D(midX, 0, room.getMinZ() + offset));
        candidates.add(new Position3D(room.getMinX() + offset, 0, midZ));
        candidates.add(new Position3D(room.getMaxX() - offset, 0, midZ));

        if (room.hasVertices()) {
            List<Position3D> vertices = room.getVertices();
            for (Position3D vertex : vertices) {
                candidates.add(new Position3D(vertex.x + offset, 0, vertex.z + offset));
                candidates.add(new Position3D(vertex.x - offset, 0, vertex.z + offset));
                candidates.add(new Position3D(vertex.x + offset, 0, vertex.z - offset));
                candidates.add(new Position3D(vertex.x - offset, 0, vertex.z - offset));
            }
        }

        if (!preferCenter) {
            candidates.add(room.getCenter());
        }

        double stepSize = 0.5;
        for (double x = room.getMinX() + offset; x <= room.getMaxX() - offset; x += stepSize) {
            for (double z = room.getMinZ() + offset; z <= room.getMaxZ() - offset; z += stepSize) {
                candidates.add(new Position3D(x, 0, z));
            }
        }

        for (Position3D candidate : candidates) {
            if (isValidPosition(candidate, dims, room, usedPositions)) {
                logger.debug("Found valid position at ({}, {})",
                        String.format("%.2f", candidate.x),
                        String.format("%.2f", candidate.z));
                return candidate;
            }
        }

        return null;
    }

    /**
     * Checks if a position is valid for placing a product.
     * Validates that the product fits entirely inside the room polygon
     * and doesn't collide with already placed products.
     *
     * @param position      the position to check
     * @param dims          the product dimensions
     * @param room          the room boundaries
     * @param usedPositions list of already occupied positions
     * @return true if the position is valid, false otherwise
     */
    private boolean isValidPosition(Position3D position, ProductDimensions dims,
            RoomBounds room, List<BoundingBox2D> usedPositions) {
        BoundingBox2D productBox = new BoundingBox2D(position, dims);

        if (!room.containsBox(productBox)) {
            return false;
        }

        final double COLLISION_BUFFER = 0.15;
        for (BoundingBox2D usedBox : usedPositions) {
            if (productBox.intersects(usedBox, COLLISION_BUFFER)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the default Y-height for a product based on its mounting type.
     * Matches the frontend's default placement logic.
     *
     * @param mountingType the mounting type (WALL, FLOOR, FREESTANDING)
     * @return the default Y-coordinate height in meters
     */
    private double getDefaultHeightForMountingType(String mountingType) {
        if (mountingType == null) {
            return 0.08;
        }

        return switch (mountingType) {
            case "WALL" -> 0.38;
            case "FLOOR" -> 0.08;
            case "FREESTANDING" -> 0.08;
            default -> 0.08;
        };
    }

    /**
     * Parses product and covering recommendations from JSON response.
     * Validates products against the database and enriches recommendations with
     * full product data.
     *
     * @param json     the JSON response from OpenAI
     * @param response the response object to populate
     * @param request  the original request for position calculation
     * @throws RuntimeException if JSON parsing fails
     */
    private void parseRecommendations(String json, AIDesignResponseDTO response, AIDesignRequestDTO request) {
        try {
            Map<String, Object> aiResult = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });

            if (aiResult.containsKey("products")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> productsRaw = (List<Map<String, Object>>) aiResult.get("products");
                List<ProductRecommendationDTO> products = new ArrayList<>();

                for (Map<String, Object> productData : productsRaw) {
                    String productName = (String) productData.get("productName");
                    String color = (String) productData.get("color");

                    ProductDTO actualProduct = productService.findByName(productName);
                    if (actualProduct == null) {
                        logger.warn("AI recommended unknown product: {}", productName);
                        continue;
                    }

                    ProductRecommendationDTO rec = new ProductRecommendationDTO();
                    rec.setProductId(actualProduct.getId());
                    rec.setProductName(actualProduct.getName());
                    rec.setCategory(actualProduct.getCategoryName());
                    rec.setCategoryId(actualProduct.getCategoryId());
                    rec.setPriceRange(
                            actualProduct.getPriceRange() != null ? actualProduct.getPriceRange().toString() : null);
                    rec.setMountingType(
                            actualProduct.getMountingType() != null ? actualProduct.getMountingType().toString()
                                    : null);
                    rec.setColor(color);

                    products.add(rec);
                }

                calculateProductPositions(products, request);

                response.setProductRecommendations(products);
            }

            if (aiResult.containsKey("coverings")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> coveringsRaw = (List<Map<String, Object>>) aiResult.get("coverings");
                List<CoveringRecommendationDTO> coverings = new ArrayList<>();

                for (Map<String, Object> coveringData : coveringsRaw) {
                    String productName = (String) coveringData.get("productName");
                    String surfaceType = (String) coveringData.get("surfaceType");

                    ProductDTO actualProduct = productService.findByName(productName);
                    if (actualProduct == null) {
                        logger.warn("AI recommended unknown covering: {}", productName);
                        continue;
                    }

                    CoveringRecommendationDTO rec = new CoveringRecommendationDTO();
                    rec.setProductId(actualProduct.getId());
                    rec.setProductName(actualProduct.getName());
                    rec.setCategory(actualProduct.getCategoryName());
                    rec.setSurfaceType(surfaceType != null ? surfaceType : "wall");
                    rec.setRepeatX(3.0);
                    rec.setRepeatY(3.0);

                    coverings.add(rec);
                    logger.info("Resolved covering '{}' to ID {} (surfaceType: {})",
                            productName, actualProduct.getId(), rec.getSurfaceType());
                }

                response.setCoveringRecommendations(coverings);
            }
        } catch (Exception e) {
            logger.error("Failed to parse AI recommendations JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse AI recommendations JSON", e);
        }
    }

    /**
     * Validates a design request for required fields.
     * Checks that style, color palettes, and features are provided.
     *
     * @param request the design request to validate
     * @return true if request is valid, false otherwise
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
     * Tests OpenAI connection with a simple prompt.
     * Used for diagnostics and connection validation.
     *
     * @param testPrompt the test prompt to send
     * @return the AI response
     * @throws RuntimeException if connection test fails
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

    /**
     * Gets products available for AI selection.
     * Primarily used for debugging and testing.
     *
     * @return list of products available for AI recommendation
     */
    public List<ProductDTO> getProductsForAISelection() {
        return productService.getProductsForAISelection();
    }
}