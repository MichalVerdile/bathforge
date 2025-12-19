package com.bathforge.service.admin;

import com.bathforge.dto.admin.QuoteRequestAdminDTO;
import com.bathforge.dto.admin.UpdateQuoteRequestDTO;
import com.bathforge.dto.admin.UserDTO;
import com.bathforge.dto.admin.UserSceneDTO;
import com.bathforge.model.quote.QuoteRequest;
import com.bathforge.model.quote.QuoteRequestMessage;
import com.bathforge.model.scene.Scene;
import com.bathforge.model.user.User;
import com.bathforge.repository.QuoteRequestMessageRepository;
import com.bathforge.repository.quote.QuoteRequestRepository;
import com.bathforge.repository.scene.SceneRepository;
import com.bathforge.repository.user.UserRepository;
import com.bathforge.service.email.EmailService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for admin operations.
 * Provides functionality for managing users, scenes, and quote requests from an
 * administrative perspective.
 */
@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private static final String UPLOAD_DIR = "uploads/documents/";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private QuoteRequestRepository quoteRequestRepository;

    @Autowired
    private QuoteRequestMessageRepository quoteRequestMessageRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Retrieves all users in the system.
     *
     * @return list of UserDTO objects representing all users
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific user by ID.
     *
     * @param userId the ID of the user to retrieve
     * @return UserDTO object representing the user
     * @throws IllegalArgumentException if user not found
     */
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return convertToUserDTO(user);
    }

    /**
     * Converts a User entity to a UserDTO.
     *
     * @param user the User entity to convert
     * @return UserDTO with user information and counts
     */
    private UserDTO convertToUserDTO(User user) {
        UserDTO dto = new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCompany(),
                user.getPhone(),
                user.getRole().name(),
                user.isEnabled(),
                user.getCreatedAt());

        // Count scenes and quote requests
        dto.setSceneCount(sceneRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).size());
        dto.setQuoteRequestCount(quoteRequestRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).size());

        return dto;
    }

    /**
     * Retrieves all scenes with user information.
     *
     * @return list of UserSceneDTO objects for all scenes
     */
    public List<UserSceneDTO> getAllScenesByUser() {
        List<Scene> scenes = sceneRepository.findAll();
        return scenes.stream()
                .filter(scene -> scene.getUserEntity() != null)
                .map(this::convertToUserSceneDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all scenes created by a specific user.
     *
     * @param userId the ID of the user
     * @return list of UserSceneDTO objects for the user's scenes
     */
    public List<UserSceneDTO> getScenesByUserId(Long userId) {
        List<Scene> scenes = sceneRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return scenes.stream()
                .map(this::convertToUserSceneDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converts a Scene entity to a UserSceneDTO.
     *
     * @param scene the Scene entity to convert
     * @return UserSceneDTO with scene and user information
     */
    private UserSceneDTO convertToUserSceneDTO(Scene scene) {
        UserSceneDTO dto = new UserSceneDTO();
        dto.setSceneId(scene.getId());
        dto.setSceneName(scene.getName());
        dto.setSceneDescription(scene.getDescription());
        dto.setIsPublic(scene.getIsPublic());
        dto.setCreatedAt(scene.getCreatedAt().toString());
        dto.setUpdatedAt(scene.getUpdatedAt().toString());

        if (scene.getUserEntity() != null) {
            dto.setUserId(scene.getUserEntity().getId());
            dto.setUserEmail(scene.getUserEntity().getEmail());
            dto.setUserFullName(scene.getUserEntity().getFirstName() + " " + scene.getUserEntity().getLastName());
        }

        return dto;
    }

    /**
     * Retrieves all quote requests in the system.
     *
     * @return list of QuoteRequestAdminDTO objects sorted by creation date (newest
     *         first)
     */
    public List<QuoteRequestAdminDTO> getAllQuoteRequests() {
        return quoteRequestRepository.findAll().stream()
                .map(this::convertToQuoteRequestAdminDTO)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific quote request by ID.
     *
     * @param requestId the ID of the quote request
     * @return QuoteRequestAdminDTO with full quote request details
     * @throws IllegalArgumentException if quote request not found
     */
    public QuoteRequestAdminDTO getQuoteRequestById(Long requestId) {
        QuoteRequest request = quoteRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Quote request not found"));
        return convertToQuoteRequestAdminDTO(request);
    }

    /**
     * Converts a QuoteRequest entity to a QuoteRequestAdminDTO.
     *
     * @param request the QuoteRequest entity to convert
     * @return QuoteRequestAdminDTO with request and user information
     */
    private QuoteRequestAdminDTO convertToQuoteRequestAdminDTO(QuoteRequest request) {
        User user = request.getUser();
        return new QuoteRequestAdminDTO(
                request.getId(),
                user.getId(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                user.getPhone(),
                user.getCompany(),
                request.getRoomDimensions(),
                request.getAdditionalNotes(),
                request.getSceneSnapshot(),
                request.getStatus(),
                request.getAdminResponse(),
                request.getDocumentUrl(),
                request.getCreatedAt(),
                request.getUpdatedAt());
    }

    /**
     * Updates a quote request with new status and/or admin response.
     * Creates system messages for status changes and admin responses.
     * Sends email notification if status changes.
     *
     * @param requestId the ID of the quote request to update
     * @param updateDTO DTO containing the updates to apply
     * @return updated QuoteRequestAdminDTO
     * @throws IllegalArgumentException if quote request not found
     * @throws MessagingException       if email notification fails
     */
    @Transactional
    public QuoteRequestAdminDTO updateQuoteRequest(Long requestId, UpdateQuoteRequestDTO updateDTO)
            throws MessagingException {
        QuoteRequest request = quoteRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Quote request not found"));

        String oldStatus = request.getStatus();

        if (updateDTO.getStatus() != null) {
            request.setStatus(updateDTO.getStatus());

            // Create status change message
            String statusMessage = "Status changed to: " + updateDTO.getStatus();
            QuoteRequestMessage statusMsg = new QuoteRequestMessage(request, statusMessage, "SYSTEM");
            quoteRequestMessageRepository.save(statusMsg);
        }

        if (updateDTO.getAdminResponse() != null && !updateDTO.getAdminResponse().trim().isEmpty()) {
            request.setAdminResponse(updateDTO.getAdminResponse());

            // Create admin response message
            QuoteRequestMessage adminMsg = new QuoteRequestMessage(request, updateDTO.getAdminResponse(), "ADMIN");
            quoteRequestMessageRepository.save(adminMsg);
        }

        request = quoteRequestRepository.save(request);

        // Send email if status changed
        if (updateDTO.getStatus() != null && !updateDTO.getStatus().equals(oldStatus)) {
            sendStatusChangeEmail(request);
        }

        return convertToQuoteRequestAdminDTO(request);
    }

    /**
     * Uploads a document file for a quote request.
     * Stores the file in the uploads directory and updates the quote request with
     * the document URL.
     *
     * @param requestId the ID of the quote request
     * @param file      the multipart file to upload
     * @return updated QuoteRequestAdminDTO with document URL
     * @throws IllegalArgumentException if quote request not found
     * @throws IOException              if file upload fails
     */
    @Transactional
    public QuoteRequestAdminDTO uploadDocument(Long requestId, MultipartFile file) throws IOException {
        QuoteRequest request = quoteRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Quote request not found"));

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update request with document URL
        request.setDocumentUrl("/uploads/documents/" + filename);
        request = quoteRequestRepository.save(request);

        // Create message about document upload
        String documentMessage = "Document uploaded: " + file.getOriginalFilename();
        QuoteRequestMessage docMsg = new QuoteRequestMessage(request, documentMessage, "SYSTEM");
        quoteRequestMessageRepository.save(docMsg);

        logger.info("Document uploaded for quote request {}: {}", requestId, filename);

        return convertToQuoteRequestAdminDTO(request);
    }

    /**
     * Sends an email notification to the user when their quote request status
     * changes.
     *
     * @param request the quote request with updated status
     * @throws MessagingException if email sending fails
     */
    private void sendStatusChangeEmail(QuoteRequest request) throws MessagingException {
        User user = request.getUser();
        String subject = "Quote Request Status Update - BathForge";

        StringBuilder emailBody = new StringBuilder();
        emailBody.append("<!DOCTYPE html>");
        emailBody.append("<html><head><style>");
        emailBody.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        emailBody.append("h1 { color: #0066cc; }");
        emailBody.append(".status { font-weight: bold; color: #0099cc; }");
        emailBody.append("</style></head><body>");
        emailBody.append("<h1>Quote Request Status Update</h1>");
        emailBody.append("<p>Dear ").append(user.getFirstName()).append(" ").append(user.getLastName()).append(",</p>");
        emailBody.append("<p>Your quote request status has been updated to: <span class='status'>")
                .append(request.getStatus()).append("</span></p>");

        if (request.getAdminResponse() != null && !request.getAdminResponse().isEmpty()) {
            emailBody.append("<h2>Message from BathForge:</h2>");
            emailBody.append("<p>").append(request.getAdminResponse()).append("</p>");
        }

        if (request.getDocumentUrl() != null && !request.getDocumentUrl().isEmpty()) {
            emailBody.append("<p>A document has been attached to your quote request.</p>");
        }

        emailBody.append("<p>You can view your quote request details by logging into your account at BathForge.</p>");
        emailBody.append("<p>Best regards,<br>The BathForge Team</p>");
        emailBody.append("</body></html>");

        emailService.sendEmail(user.getEmail(), subject, emailBody.toString());
        logger.info("Status change email sent to {} for quote request {}", user.getEmail(), request.getId());
    }
}
