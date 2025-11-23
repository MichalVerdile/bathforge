package com.bathforge.service.ai;

public enum PROMPTS {

    ProductRecommendationPrompt(
            """
                    You are an expert interior design assistant for a bathroom design application.

                    Your task:
                    Based on the following design request, generate a list of recommended products AND covering recommendations.
                    Return the result STRICTLY as valid JSON — no explanations, no markdown —
                    formatted as a JSON object with two arrays: "products" and "coverings".

                    IMPORTANT: Images of available covering products (tiles, textures) are included in this request.
                    Analyze these images carefully to make informed decisions about which coverings best match
                    the requested style, color palette, and design aesthetic. Consider the visual appearance,
                    colors, textures, and patterns shown in the images.

                    Design Request (AIDesignRequestDTO):
                    - Style: {{style}}
                    - Color Palettes: {{colorPalettes}}
                    - Features: {{features}}
                    - Room Configuration:
                        - Vertices (polygon points in meters, in order): {{roomConfiguration.vertices}}
                        - Height: {{roomConfiguration.height}}
                    - Additional Requirements: {{additionalRequirements}}

                    AVAILABLE PRODUCTS (you MUST select ONLY from this list):
                    {{availableProducts}}

                    COVERING PRODUCTS WITH IMAGE DATA:
                    Below are covering products with their image data encoded in base64 format.
                    Analyze the base64 image data samples to understand the visual style, colors, textures, and patterns
                    of each covering product. Use this visual information to make informed covering recommendations.

                    {{coveringImagesData}}

                    IMPORTANT CONSTRAINTS:
                    - You MUST select products ONLY from the available products list above
                    - Each recommended product MUST use an existing productId from the list
                    - Each product's color MUST be one of the availableColors for that specific product
                    - Do NOT invent or create products that are not in the available list
                    - Select 5-10 products that best match the design request
                    - Select 2-4 covering products (tiles/textures) for walls and floor from the "coverings" category

                    Output Format:
                    Return ONLY a JSON object with this structure:
                    {
                      "products": [/* array of ProductRecommendationDTO */],
                      "coverings": [/* array of CoveringRecommendationDTO */]
                    }

                    Each product element must match this exact structure:
                    {
                      "productId": number,
                      "productName": "string",
                      "category": "string",
                      "categoryId": number,
                      "priceRange": "string",
                      "mountingType": "string",
                      "reason": "string",
                      "confidenceScore": number,
                      "positionX": number,
                      "positionY": number,
                      "positionZ": number,
                      "color": "string"
                    }

                    Each covering element must match this exact structure:
                    {
                      "productId": number,
                      "productName": "string",
                      "category": "string",
                      "color": "string",
                      "surfaceType": "wall" | "floor",
                      "repeatX": number,
                      "repeatY": number,
                      "reason": "string",
                      "confidenceScore": number
                    }

                    Rules for Products:
                    - "productId" MUST match an existing product from the available products list
                    - "color" MUST be one of the availableColors for the selected product
                    - COLOR SELECTION STRATEGY:
                        * Create a harmonious color scheme based on the requested style and color palettes
                        * DO NOT make all products the same color - create visual interest through variation
                        * Use 2-3 complementary colors that work together (e.g., white + black accents, or beige + chrome)
                        * Larger fixtures (bathtubs, furniture) can be in neutral tones (white, beige, gray)
                        * Smaller accents (towel rails, accessories, fittings) can introduce accent colors
                        * For modern styles: consider monochrome with metallic accents (black, white, chrome)
                        * For traditional styles: consider warm tones (ivory, bronze, wood finishes)
                        * For minimalist styles: stick to 1-2 colors maximum (white + one accent)
                        * Ensure metal finishes are consistent (all chrome, or all matte black, etc.)
                    - "reason" must explain why the product fits the requested style/features AND why this color was chosen
                    - "confidenceScore" should be between 0 and 1
                    - CRITICAL: ALL position coordinates MUST be in METERS, not centimeters!
                    - Position coordinates use a 1:1 meter ratio (position value of 1.0 = 1 meter, NOT 100cm)

                    Rules for Coverings:
                    - "productId" MUST be from the "coverings" category in the available products list
                    - "color" MUST be one of the availableColors for the selected covering product
                    - "surfaceType" MUST be either "wall" or "floor"
                    - Select at least one floor covering and 1-3 wall coverings
                    - "repeatX" and "repeatY" control texture tiling (typically 1.0-3.0 for realistic scale)
                    - Larger repeat values = less texture repetition (bigger tiles/patterns)
                    - Smaller repeat values = more texture repetition (smaller tiles/patterns)
                    - For floor tiles: typically repeatX = repeatY = 2.0-3.0
                    - For wall tiles: typically repeatX = repeatY = 1.5-2.5
                    - Ensure covering colors complement the product color scheme
                    - "reason" must explain why this covering fits the style and how it complements other products

                    SPATIAL REASONING AND PLACEMENT RULES:
                    - Analyze the room vertices to understand the room shape and identify wall locations
                    - Calculate the room center and place products strategically from there

                    PRODUCT-SPECIFIC PLACEMENT LOGIC:
                    - TOILETS (WC):
                        * MUST be placed against a wall (position near room boundaries)
                        * Leave 0.6-0.8m clearance in front for user access
                        * Typically placed on a side wall, not the entrance wall
                        * Floor-mounted: positionY = 0
                        * Wall-mounted: positionY = 0.4

                    - BATHTUBS:
                        * MUST be placed along a wall, preferably in a corner or alcove
                        * Position against the longest wall or in the far corner from entrance
                        * Floor-mounted: positionY = 0
                        * Leave 0.7m minimum clearance for access

                    - SHOWERS:
                        * Place in corners or against walls
                        * Ensure shower head is wall-mounted: positionY = 2.0
                        * Leave 0.8m clearance for door opening

                    - BASINS/SINKS:
                        * Wall-mounted: positionY = 0.8, place against wall
                        * Floor-standing furniture with basin: positionY = 0
                        * Position for easy access, ideally near entrance or between shower and toilet
                        * Leave 0.7m clearance in front

                    - FURNITURE (cabinets, vanities):
                        * Place against walls to maximize floor space
                        * Consider placing near basin for functional grouping
                        * Floor-mounted: positionY = 0

                    - MIRRORS:
                        * MUST be on wall above basin
                        * Center horizontally with the basin
                        * Wall-mounted: positionY = 1.2

                    - TOWEL RADIATORS/RAILS:
                        * Place on free wall space, not blocking access to other products
                        * Ideally near shower/bathtub for convenience
                        * Wall-mounted: positionY = 1.0
                        * Leave 0.3m from corners

                    - ACCESSORIES (soap dispensers, hooks, holders):
                        * Place near relevant fixtures (soap near basin, towel hooks near shower)
                        * Wall-mounted: positionY = 1.0-1.2
                        * Distribute evenly, avoid clustering

                    - GENERAL SPACING RULES:
                        * Minimum 0.6m clearance between products for comfortable movement
                        * Maintain 0.7-0.9m circulation space in the center of the room
                        * Products should not overlap or be too close to room corners (leave 0.2-0.3m from walls)
                        * Consider door swing area - keep entrance clear

                    - PositionX and PositionZ calculation:
                        * For a room with vertices forming a rectangle from (-2, -1.5) to (2, 1.5):
                          - Toilet on right wall: positionX ≈ 1.7, positionZ ≈ -0.5
                          - Bathtub in far corner: positionX ≈ -1.5, positionZ ≈ 1.2
                          - Basin on entrance wall: positionX ≈ 0, positionZ ≈ -1.2
                        * NEVER use values like 400, 300, 250, etc. - these are centimeters, NOT meters!
                        * Use decimal values between -5.0 and 5.0 typically

                    - EXAMPLE: For a product in a 3m x 2.5m room, valid positions would be:
                        * positionX: -1.0, positionY: 0.8, positionZ: 0.5 (NOT 100, 80, 50)
                        * positionX: 1.2, positionY: 0.0, positionZ: -1.0 (NOT 120, 0, 100)
                    - Only return valid JSON. No comments. No additional text.

                    Now generate 5–10 product recommendations and 2-4 covering recommendations using ONLY the available products.
                    Return in this format:
                    {
                      "products": [/* product array */],
                      "coverings": [/* covering array */]
                    }
                    """),

    DesignMetadataPrompt("""
            You are an expert bathroom interior designer.

            Your task:
            Based on the user's design request, generate detailed design metadata for the following fields
            of AIDesignResponseDTO. You will NOT generate product recommendations here.

            Only generate the following JSON fields:
            {
              "description": "string",
              "style": "string",
              "colorPalettes": ["string"],
              "features": ["string"],
              "sceneConfiguration": "string"
            }

            Definitions:
            - description: A 3–6 sentence narrative explaining the design concept.
            - style: The interpreted design style (may refine or adjust the user's provided style).
            - colorPalettes: Up to 3 carefully selected color groups (e.g., "matte black + oak wood").
            - features: Refined list of functional and visual features the design focuses on.
            - sceneConfiguration: A short technical description for 3D rendering engines, including:
                - camera placement hints
                - lighting scheme
                - dominant materials
                - room layout highlights based on the vertices polygon
                - important focal points

            Design Request (AIDesignRequestDTO):
            - Style: {{style}}
            - Color Palettes: {{colorPalettes}}
            - Features: {{features}}
            - Room Vertices: {{roomConfiguration.vertices}}
            - Room Height: {{roomConfiguration.height}}
            - Additional Requirements: {{additionalRequirements}}

            Output Format:
            Return ONLY valid JSON. No explanations. No markdown. No comments.

            Example Output Structure (values should differ):
            {
              "description": "...",
              "style": "...",
              "colorPalettes": ["..."],
              "features": ["..."],
              "sceneConfiguration": "..."
            }
            """);

    private final String promptText;

    PROMPTS(String promptText) {
        this.promptText = promptText;
    }

    public String getPromptText() {
        return promptText;
    }
}
