package com.bathforge.controller;

import com.bathforge.dto.QuoteRequestDetailDTO;
import com.bathforge.dto.quote.QuoteRequestHistoryDTO;
import com.bathforge.dto.scene.SceneDTO;
import com.bathforge.model.scene.Scene;
import com.bathforge.model.user.User;
import com.bathforge.repository.scene.SceneRepository;
import com.bathforge.service.quote.QuoteService;
import com.bathforge.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for user-specific operations and data retrieval.
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private QuoteService quoteService;

    /**
     * Retrieves all scenes for the authenticated user.
     *
     * @return response entity with list of user's scenes or error message
     */
    @GetMapping("/scenes")
    public ResponseEntity<?> getUserScenes() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Scene> scenes = sceneRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

            List<SceneDTO> sceneDTOs = scenes.stream()
                    .map(scene -> {
                        SceneDTO dto = new SceneDTO();
                        dto.setId(scene.getId());
                        dto.setName(scene.getName());
                        dto.setDescription(scene.getDescription());
                        dto.setUser(userEmail);
                        dto.setSceneData(scene.getSceneData());
                        dto.setCameraPosition(scene.getCameraPosition());
                        dto.setLightingSettings(scene.getLightingSettings());
                        dto.setBackgroundColor(scene.getBackgroundColor());
                        dto.setIsPublic(scene.getIsPublic());
                        dto.setCreatedAt(scene.getCreatedAt());
                        dto.setUpdatedAt(scene.getUpdatedAt());
                        return dto;
                    })
                    .collect(Collectors.toList());

            logger.info("Retrieved {} scenes for user: {}", sceneDTOs.size(), userEmail);
            return ResponseEntity.ok(sceneDTOs);

        } catch (Exception e) {
            logger.error("Error getting user scenes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve scenes");
        }
    }

    /**
     * Retrieves all quote requests for the authenticated user.
     *
     * @return response entity with list of user's quote requests or error message
     */
    @GetMapping("/quote-requests")
    public ResponseEntity<?> getUserQuoteRequests() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<QuoteRequestHistoryDTO> quoteRequests = quoteService.getUserQuoteRequests(user.getId());

            logger.info("Retrieved {} quote requests for user: {}", quoteRequests.size(), userEmail);
            return ResponseEntity.ok(quoteRequests);

        } catch (Exception e) {
            logger.error("Error getting user quote requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve quote requests");
        }
    }

    /**
     * Retrieves detailed information for a specific quote request.
     *
     * @param id the quote request ID
     * @return response entity with quote request details or error message
     */
    @GetMapping("/quote-requests/{id}")
    public ResponseEntity<?> getQuoteRequestDetail(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            User user = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            QuoteRequestDetailDTO detail = userService.getQuoteRequestDetail(id, user.getId());

            logger.info("Retrieved quote request {} details for user: {}", id, userEmail);
            return ResponseEntity.ok(detail);

        } catch (IllegalArgumentException e) {
            logger.error("Error getting quote request detail: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting quote request detail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve quote request details");
        }
    }
}
