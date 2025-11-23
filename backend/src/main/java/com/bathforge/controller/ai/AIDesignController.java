package com.bathforge.controller.ai;

import com.bathforge.dto.ai.AIDesignRequestDTO;
import com.bathforge.dto.ai.AIDesignResponseDTO;
import com.bathforge.service.ai.AIDesignService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/design")
@CrossOrigin(origins = "*")
public class AIDesignController {

    private static final Logger logger = LoggerFactory.getLogger(AIDesignController.class);

    private final AIDesignService aiDesignService;

    @Autowired
    public AIDesignController(AIDesignService aiDesignService) {
        this.aiDesignService = aiDesignService;
    }

    /**
     * Generate a new bathroom design based on AI preferences
     */
    @PostMapping("/generate")
    public ResponseEntity<AIDesignResponseDTO> generateDesign(@Valid @RequestBody AIDesignRequestDTO request) {
        logger.info("Received AI design generation request: {}", request);

        try {
            // Validate the request
            if (!aiDesignService.validateRequest(request)) {
                logger.warn("Invalid design request received: {}", request);
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid request parameters"));
            }

            // Generate the design
            AIDesignResponseDTO response = aiDesignService.generateDesign(request);

            if (response.getStatus() == AIDesignResponseDTO.GenerationStatus.FAILED) {
                logger.error("Design generation failed for request: {}", request);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            logger.info("Successfully generated design with ID: {}", response.getDesignId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing design generation request: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Get available style options for the frontend
     */
    @GetMapping("/styles")
    public ResponseEntity<String[]> getAvailableStyles() {
        String[] styles = {
                "modern", "traditional", "minimalist",
                "luxury", "industrial", "scandinavian"
        };
        return ResponseEntity.ok(styles);
    }

    /**
     * Get available color palette options for the frontend
     */
    @GetMapping("/color-palettes")
    public ResponseEntity<String[]> getAvailableColorPalettes() {
        String[] colorPalettes = {
                "spa-serenity", "modern-monochrome", "natural-warmth",
                "coastal-fresh", "luxe-dark", "sage-stone"
        };
        return ResponseEntity.ok(colorPalettes);
    }

    /**
     * Get available feature options for the frontend
     */
    @GetMapping("/features")
    public ResponseEntity<String[]> getAvailableFeatures() {
        String[] features = {
                "bathtub", "shower", "sink",
                "toilet", "storage", "mirror"
        };
        return ResponseEntity.ok(features);
    }

    /**
     * Health check endpoint for AI service
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Design service is running");
    }

    /**
     * Test OpenAI integration
     */
    @PostMapping("/test-openai")
    public ResponseEntity<String> testOpenAI(@RequestBody String testPrompt) {
        logger.info("Testing OpenAI integration with prompt: {}", testPrompt);

        try {
            // Test basic OpenAI connectivity
            String response = aiDesignService.testOpenAIConnection(testPrompt);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("OpenAI test failed: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("OpenAI test failed: " + e.getMessage());
        }
    }

    /**
     * Create error response
     */
    private AIDesignResponseDTO createErrorResponse(String message) {
        AIDesignResponseDTO errorResponse = new AIDesignResponseDTO();
        errorResponse.setStatus(AIDesignResponseDTO.GenerationStatus.FAILED);
        errorResponse.setDescription(message);
        return errorResponse;
    }
}