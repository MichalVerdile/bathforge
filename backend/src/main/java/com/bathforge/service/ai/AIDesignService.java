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

            // Set simple metadata from user input (no AI call needed)
            response.setStyle(request.getStyle());
            response.setColorPalettes(request.getColorPalettes());
            response.setFeatures(request.getFeatures());
            response.setDescription("AI-generated bathroom design"); // Simple placeholder
            response.setStatus(AIDesignResponseDTO.GenerationStatus.GENERATED);

            // Save the design as a scene
            logger.debug("Saving AI-generated design as scene...");
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
     * Generate product and covering recommendations based on requested features
     */
    private void generateProductRecommendations(AIDesignRequestDTO request, AIDesignResponseDTO response) {
        logger.debug("Building product recommendations prompt...");

        Map<String, Object> templateData = buildTemplateData(request);

        // Fetch showcase products (with descriptions) and all coverings
        logger.debug("Fetching showcase products and coverings for AI selection...");
        List<ProductDTO> allProducts = productService.getProductsForAISelection();
        logger.info("Found {} products available for AI selection", allProducts.size());

        // Format products as JSON for the prompt (includes coverings with descriptions)
        String productsJson = formatProductsForPrompt(allProducts);
        templateData.put("availableProducts", productsJson);

        String prompt = applyTemplate(PROMPTS.ProductRecommendationPrompt.getPromptText(), templateData);
        String aiResponse = promptService.generateDesignFromPrompt(prompt);

        logger.debug("Received product recommendations response from OpenAI");

        parseRecommendations(aiResponse, response, request);
        logger.info("Parsed {} product and {} covering recommendations from AI response",
                response.getProductRecommendations().size(),
                response.getCoveringRecommendations() != null ? response.getCoveringRecommendations().size() : 0);
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
                                logger.debug("Color '{}' not found for product '{}', using first available color",
                                        rec.getColor(), rec.getProductName());
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

                // Add doors and windows to room properties
                try {
                    Map<String, Object> roomProperties = new HashMap<>();
                    if (request.getRoomConfiguration().getDoors() != null && !request.getRoomConfiguration().getDoors().isEmpty()) {
                        roomProperties.put("doors", request.getRoomConfiguration().getDoors());
                        logger.debug("Added {} doors to room properties", request.getRoomConfiguration().getDoors().size());
                    }
                    if (request.getRoomConfiguration().getWindows() != null && !request.getRoomConfiguration().getWindows().isEmpty()) {
                        roomProperties.put("windows", request.getRoomConfiguration().getWindows());
                        logger.debug("Added {} windows to room properties", request.getRoomConfiguration().getWindows().size());
                    }
                    if (!roomProperties.isEmpty()) {
                        String roomPropertiesJson = objectMapper.writeValueAsString(roomProperties);
                        roomModelDTO.setRoomProperties(roomPropertiesJson);
                        logger.info("Saved room properties with doors and windows");
                    }
                } catch (Exception e) {
                    logger.error("Failed to serialize room properties (doors/windows): {}", e.getMessage(), e);
                    // Continue without room properties - don't fail the entire save
                }

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

        // Add detailed color palette descriptions for AI
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
     * Get detailed color descriptions for the selected color palettes
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
     * Extract room bounds from room configuration.
     * Converts 2D vertices to 3D coordinates and calculates bounding box.
     *
     * @param roomConfig The room configuration from the request
     * @return RoomBounds representing the room's boundaries
     */
    private RoomBounds extractRoomBounds(RoomConfigurationDTO roomConfig) {
        if (roomConfig == null || roomConfig.getVertices() == null || roomConfig.getVertices().isEmpty()) {
            // Default room: 2.2m x 2.2m
            logger.debug("No room configuration provided, using default bounds");
            return new RoomBounds(-1.1, 1.1, -1.1, 1.1);
        }

        List<VertexDTO> vertices = roomConfig.getVertices();

        // Calculate centroid
        double centerX = vertices.stream()
                .mapToDouble(VertexDTO::getX)
                .average().orElse(0);
        double centerY = vertices.stream()
                .mapToDouble(VertexDTO::getY)
                .average().orElse(0);

        // Convert to 3D coordinates (cm to meters: multiply by 0.01)
        List<Position3D> vertices3D = new ArrayList<>();
        for (VertexDTO vertex : vertices) {
            double x = (vertex.getX() - centerX) * 0.01;
            double z = (vertex.getY() - centerY) * 0.01;
            vertices3D.add(new Position3D(x, 0, z));
        }

        // Calculate bounding box
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
     * Calculate positions for AI-recommended products using smart placement.
     * Uses actual room vertices for L-shaped, U-shaped, and irregular rooms.
     * Validates that products fit inside the room polygon.
     * Bathtubs are always placed in the center.
     */
    private void calculateProductPositions(
            List<ProductRecommendationDTO> products,
            AIDesignRequestDTO request) {
        if (products == null || products.isEmpty()) {
            return;
        }

        logger.info("Calculating positions for {} products using smart placement", products.size());

        // Extract room bounds
        RoomBounds room = extractRoomBounds(request.getRoomConfiguration());
        logger.info("Room bounds: {}", room);

        // Safety buffer to add extra space from walls
        final double SAFETY_BUFFER = 0.25; // 25cm extra clearance

        // Sort products: bathtubs go last (will be placed in center)
        // Other large items also sorted by size so largest items get placed last
        products.sort((a, b) -> {
            boolean aIsBathtub = "bathtubs".equalsIgnoreCase(a.getCategory()) ||
                                 "bath tubs".equalsIgnoreCase(a.getCategory()) ||
                                 a.getCategory().toLowerCase().contains("bathtub");
            boolean bIsBathtub = "bathtubs".equalsIgnoreCase(b.getCategory()) ||
                                 "bath tubs".equalsIgnoreCase(b.getCategory()) ||
                                 b.getCategory().toLowerCase().contains("bathtub");

            // Bathtubs always go last (to center)
            if (aIsBathtub && !bIsBathtub) return 1;
            if (!aIsBathtub && bIsBathtub) return -1;

            // For non-bathtubs, sort by footprint (smaller first, so larger ones go toward center)
            ProductDimensions dimsA = ProductDimensions.forCategory(a.getCategory());
            ProductDimensions dimsB = ProductDimensions.forCategory(b.getCategory());
            return Double.compare(dimsA.getFootprint(), dimsB.getFootprint());
        });

        logger.info("Sorted products for placement: {}", products.stream()
                .map(p -> p.getProductName() + " (" + p.getCategory() + ")")
                .toList());

        // Track used positions to avoid placing multiple products at the same spot
        List<BoundingBox2D> usedPositions = new ArrayList<>();

        // Assign products to positions
        for (int i = 0; i < products.size(); i++) {
            ProductRecommendationDTO product = products.get(i);

            // Get product dimensions based on category
            ProductDimensions dims = ProductDimensions.forCategory(product.getCategory());
            logger.debug("Product '{}' dimensions: {}", product.getProductName(), dims);

            // Check if this is a bathtub (should go in center)
            boolean isBathtub = "bathtubs".equalsIgnoreCase(product.getCategory()) ||
                               "bath tubs".equalsIgnoreCase(product.getCategory()) ||
                               product.getCategory().toLowerCase().contains("bathtub");

            // Find a valid position for this product
            Position3D position = findValidPosition(room, dims, usedPositions, SAFETY_BUFFER, isBathtub);

            if (position == null) {
                logger.warn("Could not find valid position for product '{}', using center as fallback",
                           product.getProductName());
                position = room.getCenter();
            }

            // Get height based on mounting type
            double height = getDefaultHeightForMountingType(product.getMountingType());

            product.setPositionX(position.x);
            product.setPositionY(height);
            product.setPositionZ(position.z);
            product.setRotationX(0.0);
            product.setRotationY(0.0);
            product.setRotationZ(0.0);

            // Record this position as used
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
     * Find a valid position for a product that:
     * 1. Is inside the room polygon
     * 2. Doesn't collide with already placed products
     * 3. Has proper clearance from walls
     * 4. Spreads items evenly across the room
     */
    private Position3D findValidPosition(RoomBounds room, ProductDimensions dims,
                                          List<BoundingBox2D> usedPositions,
                                          double safetyBuffer, boolean preferCenter) {
        double offset = Math.max(dims.getWidth(), dims.getDepth()) / 2.0 + safetyBuffer;

        // If product should be in center (bathtubs), try center first
        if (preferCenter) {
            Position3D center = room.getCenter();
            if (isValidPosition(center, dims, room, usedPositions)) {
                logger.debug("Placing bathtub at center position");
                return center;
            }
        }

        // Generate candidate positions in priority order
        List<Position3D> candidates = new ArrayList<>();

        // Strategy 1: Bounding box corners (prioritize spreading to corners)
        // This ensures items spread out across the room first
        candidates.add(new Position3D(room.getMinX() + offset, 0, room.getMaxZ() - offset)); // Top-left
        candidates.add(new Position3D(room.getMaxX() - offset, 0, room.getMaxZ() - offset)); // Top-right
        candidates.add(new Position3D(room.getMinX() + offset, 0, room.getMinZ() + offset)); // Bottom-left
        candidates.add(new Position3D(room.getMaxX() - offset, 0, room.getMinZ() + offset)); // Bottom-right

        // Strategy 2: Edge midpoints (for better distribution)
        double midX = (room.getMinX() + room.getMaxX()) / 2.0;
        double midZ = (room.getMinZ() + room.getMaxZ()) / 2.0;
        candidates.add(new Position3D(midX, 0, room.getMaxZ() - offset)); // Top-middle
        candidates.add(new Position3D(midX, 0, room.getMinZ() + offset)); // Bottom-middle
        candidates.add(new Position3D(room.getMinX() + offset, 0, midZ)); // Left-middle
        candidates.add(new Position3D(room.getMaxX() - offset, 0, midZ)); // Right-middle

        // Strategy 3: Use actual room vertices if available (for L, U, irregular shapes)
        // These provide additional placement options specific to the room shape
        if (room.hasVertices()) {
            List<Position3D> vertices = room.getVertices();
            for (Position3D vertex : vertices) {
                // Try positions offset inward from each vertex
                candidates.add(new Position3D(vertex.x + offset, 0, vertex.z + offset));
                candidates.add(new Position3D(vertex.x - offset, 0, vertex.z + offset));
                candidates.add(new Position3D(vertex.x + offset, 0, vertex.z - offset));
                candidates.add(new Position3D(vertex.x - offset, 0, vertex.z - offset));
            }
        }

        // Strategy 4: Center (only for non-bathtubs as last resort before grid)
        if (!preferCenter) {
            candidates.add(room.getCenter());
        }

        // Strategy 5: Grid sampling across the room (last resort)
        double stepSize = 0.5; // 50cm grid
        for (double x = room.getMinX() + offset; x <= room.getMaxX() - offset; x += stepSize) {
            for (double z = room.getMinZ() + offset; z <= room.getMaxZ() - offset; z += stepSize) {
                candidates.add(new Position3D(x, 0, z));
            }
        }

        // Try each candidate position
        for (Position3D candidate : candidates) {
            if (isValidPosition(candidate, dims, room, usedPositions)) {
                logger.debug("Found valid position at ({}, {})",
                    String.format("%.2f", candidate.x),
                    String.format("%.2f", candidate.z));
                return candidate;
            }
        }

        return null; // No valid position found
    }

    /**
     * Check if a position is valid for placing a product:
     * - Product must fit entirely inside the room polygon
     * - Product must not collide with already placed products
     */
    private boolean isValidPosition(Position3D position, ProductDimensions dims,
                                     RoomBounds room, List<BoundingBox2D> usedPositions) {
        // Create bounding box for this product at this position
        BoundingBox2D productBox = new BoundingBox2D(position, dims);

        // Check 1: Product must fit inside the room polygon
        if (!room.containsBox(productBox)) {
            return false;
        }

        // Check 2: Product must not collide with already placed products
        final double COLLISION_BUFFER = 0.15; // 15cm clearance between products
        for (BoundingBox2D usedBox : usedPositions) {
            if (productBox.intersects(usedBox, COLLISION_BUFFER)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the default Y-height for a product based on its mounting type
     * Matches the frontend's default placement logic
     */
    private double getDefaultHeightForMountingType(String mountingType) {
        if (mountingType == null) {
            return 0.08; // Default to floor level
        }

        return switch (mountingType) {
            case "WALL" -> 0.38;         // Wall-mounted items at 38cm
            case "FLOOR" -> 0.08;        // Floor items at 8cm
            case "FREESTANDING" -> 0.08; // Freestanding items at 8cm
            default -> 0.08;             // Default to floor level
        };
    }

    /**
     * Parse product and covering recommendations from JSON response
     */
    private void parseRecommendations(String json, AIDesignResponseDTO response, AIDesignRequestDTO request) {
        try {
            // Parse the response which contains both products and coverings
            Map<String, Object> aiResult = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });

            // Parse products array
            if (aiResult.containsKey("products")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> productsRaw = (List<Map<String, Object>>) aiResult.get("products");
                List<ProductRecommendationDTO> products = new ArrayList<>();

                for (Map<String, Object> productData : productsRaw) {
                    String productName = (String) productData.get("productName");
                    String color = (String) productData.get("color");

                    // Look up the actual product by name
                    ProductDTO actualProduct = productService.findByName(productName);
                    if (actualProduct == null) {
                        logger.warn("AI recommended unknown product: {}", productName);
                        continue;
                    }

                    // Build complete recommendation with correct data from database
                    ProductRecommendationDTO rec = new ProductRecommendationDTO();
                    rec.setProductId(actualProduct.getId());
                    rec.setProductName(actualProduct.getName());
                    rec.setCategory(actualProduct.getCategoryName());
                    rec.setCategoryId(actualProduct.getCategoryId());
                    rec.setPriceRange(actualProduct.getPriceRange() != null ? actualProduct.getPriceRange().toString() : null);
                    rec.setMountingType(actualProduct.getMountingType() != null ? actualProduct.getMountingType().toString() : null);
                    rec.setColor(color);

                    products.add(rec);
                }

                // Calculate positions for products to avoid overlap
                calculateProductPositions(products, request);

                response.setProductRecommendations(products);
            }

            // Parse coverings array
            if (aiResult.containsKey("coverings")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> coveringsRaw = (List<Map<String, Object>>) aiResult.get("coverings");
                List<CoveringRecommendationDTO> coverings = new ArrayList<>();

                for (Map<String, Object> coveringData : coveringsRaw) {
                    String productName = (String) coveringData.get("productName");
                    String surfaceType = (String) coveringData.get("surfaceType");

                    // Look up the actual product by name
                    ProductDTO actualProduct = productService.findByName(productName);
                    if (actualProduct == null) {
                        logger.warn("AI recommended unknown covering: {}", productName);
                        continue;
                    }

                    // Build complete recommendation with correct data from database
                    CoveringRecommendationDTO rec = new CoveringRecommendationDTO();
                    rec.setProductId(actualProduct.getId());
                    rec.setProductName(actualProduct.getName());
                    rec.setCategory(actualProduct.getCategoryName());
                    rec.setSurfaceType(surfaceType != null ? surfaceType : "wall");
                    rec.setRepeatX(3.0); // Sensible default
                    rec.setRepeatY(3.0); // Sensible default

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

    /**
     * Get products available for AI selection (for debugging)
     */
    public List<ProductDTO> getProductsForAISelection() {
        return productService.getProductsForAISelection();
    }
}