package com.bathforge.controller.admin;

import com.bathforge.dto.admin.QuoteRequestAdminDTO;
import com.bathforge.dto.admin.UpdateQuoteRequestDTO;
import com.bathforge.dto.admin.UserDTO;
import com.bathforge.dto.admin.UserSceneDTO;
import com.bathforge.service.admin.AdminService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<UserDTO> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error getting all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get user by ID
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        try {
            UserDTO user = adminService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting user by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all scenes grouped by user
     */
    @GetMapping("/scenes")
    public ResponseEntity<List<UserSceneDTO>> getAllScenes() {
        try {
            List<UserSceneDTO> scenes = adminService.getAllScenesByUser();
            return ResponseEntity.ok(scenes);
        } catch (Exception e) {
            logger.error("Error getting all scenes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get scenes by user ID
     */
    @GetMapping("/users/{userId}/scenes")
    public ResponseEntity<List<UserSceneDTO>> getScenesByUserId(@PathVariable Long userId) {
        try {
            List<UserSceneDTO> scenes = adminService.getScenesByUserId(userId);
            return ResponseEntity.ok(scenes);
        } catch (Exception e) {
            logger.error("Error getting scenes for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all quote requests
     */
    @GetMapping("/quote-requests")
    public ResponseEntity<List<QuoteRequestAdminDTO>> getAllQuoteRequests() {
        try {
            List<QuoteRequestAdminDTO> requests = adminService.getAllQuoteRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            logger.error("Error getting all quote requests", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get quote request by ID
     */
    @GetMapping("/quote-requests/{requestId}")
    public ResponseEntity<QuoteRequestAdminDTO> getQuoteRequestById(@PathVariable Long requestId) {
        try {
            QuoteRequestAdminDTO request = adminService.getQuoteRequestById(requestId);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error getting quote request by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update quote request (status and admin response)
     */
    @PutMapping("/quote-requests/{requestId}")
    public ResponseEntity<?> updateQuoteRequest(@PathVariable Long requestId,
            @RequestBody UpdateQuoteRequestDTO updateDTO) {
        try {
            QuoteRequestAdminDTO updated = adminService.updateQuoteRequest(requestId, updateDTO);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (MessagingException e) {
            logger.error("Error sending email for quote request update", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Quote request updated but email notification failed");
        } catch (Exception e) {
            logger.error("Error updating quote request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Upload document for quote request
     */
    @PostMapping("/quote-requests/{requestId}/upload")
    public ResponseEntity<?> uploadDocument(@PathVariable Long requestId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            QuoteRequestAdminDTO updated = adminService.uploadDocument(requestId, file);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            logger.error("Error uploading document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload document");
        } catch (Exception e) {
            logger.error("Error uploading document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
