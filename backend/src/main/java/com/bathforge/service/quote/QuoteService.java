package com.bathforge.service.quote;

import com.bathforge.dto.quote.QuoteRequestDTO;
import com.bathforge.dto.quote.QuoteRequestHistoryDTO;
import com.bathforge.dto.scene.CreateSceneDTO;
import com.bathforge.dto.scene.CreateSceneProductDTO;
import com.bathforge.dto.scene.CreateSceneRoomModelDTO;
import com.bathforge.dto.scene.CreateSceneCoveringDTO;
import com.bathforge.model.quote.QuoteRequest;
import com.bathforge.model.user.User;
import com.bathforge.repository.quote.QuoteRequestRepository;
import com.bathforge.security.JwtUtil;
import com.bathforge.service.email.EmailService;
import com.bathforge.service.email.QuoteRequestEmailData;
import com.bathforge.service.scene.SceneService;
import com.bathforge.service.user.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing quote requests from users.
 * Handles the complete quote request workflow including user creation/update,
 * quote persistence, scene creation, and email notifications to both industry
 * and users.
 */
@Service
public class QuoteService {

    private static final Logger logger = LoggerFactory.getLogger(QuoteService.class);

    private final UserService userService;
    private final EmailService emailService;
    private final QuoteRequestRepository quoteRequestRepository;
    private final SceneService sceneService;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    @Autowired
    public QuoteService(UserService userService, EmailService emailService,
            QuoteRequestRepository quoteRequestRepository, SceneService sceneService,
            ObjectMapper objectMapper, JwtUtil jwtUtil) {
        this.userService = userService;
        this.emailService = emailService;
        this.quoteRequestRepository = quoteRequestRepository;
        this.sceneService = sceneService;
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Processes a complete quote request workflow.
     * Creates or updates user account, saves quote request to database, creates a
     * scene from the request,
     * and sends notification emails to both industry contact and the user.
     *
     * @param quoteRequest the quote request data containing user info, room
     *                     dimensions, products, and coverings
     * @return the created or updated User entity
     * @throws MessagingException if there is an error sending emails
     */
    @Transactional
    public User processQuoteRequest(QuoteRequestDTO quoteRequest) throws MessagingException {
        User user;
        if (userService.existsByEmail(quoteRequest.getEmail())) {
            user = userService.findByEmail(quoteRequest.getEmail())
                    .orElseThrow(() -> new IllegalStateException("User exists but could not be retrieved"));
            logger.info("Existing user found: {}", user.getEmail());

            if (quoteRequest.getPhone() != null && !quoteRequest.getPhone().isEmpty()) {
                user.setPhone(quoteRequest.getPhone());
            }
            if (quoteRequest.getCompany() != null && !quoteRequest.getCompany().isEmpty()) {
                user.setCompany(quoteRequest.getCompany());
            }
        } else {
            user = new User();
            user.setEmail(quoteRequest.getEmail());
            user.setPassword(quoteRequest.getPassword());
            user.setFirstName(quoteRequest.getFirstName());
            user.setLastName(quoteRequest.getLastName());
            user.setPhone(quoteRequest.getPhone());
            user.setCompany(quoteRequest.getCompany());

            user = userService.createUser(user);
            logger.info("New user created: {}", user.getEmail());
        }

        try {
            String sceneDataJson = objectMapper.writeValueAsString(quoteRequest);
            QuoteRequest quoteRequestEntity = new QuoteRequest(
                    user,
                    quoteRequest.getRoomDimensions(),
                    quoteRequest.getAdditionalNotes(),
                    quoteRequest.getSceneSnapshot(),
                    sceneDataJson);
            quoteRequestRepository.save(quoteRequestEntity);
            logger.info("Quote request saved to database for user: {}", user.getEmail());
        } catch (JsonProcessingException e) {
            logger.error("Failed to save quote request to database", e);
        }

        try {
            CreateSceneDTO sceneDTO = new CreateSceneDTO();
            sceneDTO.setName("Quote Request - " + user.getFirstName() + " " + user.getLastName());
            sceneDTO.setDescription("Scene from quote request on " + java.time.LocalDateTime.now());
            sceneDTO.setUser(user.getEmail());
            sceneDTO.setIsPublic(false);

            try {
                sceneDTO.setSceneData(objectMapper.writeValueAsString(quoteRequest));
            } catch (JsonProcessingException e) {
                logger.warn("Failed to serialize scene data", e);
            }

            if (quoteRequest.getRoomData() != null) {
                CreateSceneRoomModelDTO roomModelDTO = new CreateSceneRoomModelDTO();
                roomModelDTO.setVerticesData(quoteRequest.getRoomData().getVerticesData());
                roomModelDTO.setRoomHeight(quoteRequest.getRoomData().getRoomHeight());
                roomModelDTO.setRoomProperties(quoteRequest.getRoomData().getRoomProperties());
                sceneDTO.setRoomModel(roomModelDTO);
            }

            if (quoteRequest.getProducts() != null && !quoteRequest.getProducts().isEmpty()) {
                sceneDTO.setProducts(quoteRequest.getProducts().stream()
                        .filter(p -> p.getProductId() != null)
                        .map(p -> {
                            CreateSceneProductDTO productDTO = new CreateSceneProductDTO();
                            productDTO.setProductId(p.getProductId());
                            productDTO.setColorId(p.getColorId());
                            productDTO.setPositionX(p.getPositionX());
                            productDTO.setPositionY(p.getPositionY());
                            productDTO.setPositionZ(p.getPositionZ());
                            productDTO.setRotationX(p.getRotationX());
                            productDTO.setRotationY(p.getRotationY());
                            productDTO.setRotationZ(p.getRotationZ());
                            productDTO.setScaleX(p.getScaleX());
                            productDTO.setScaleY(p.getScaleY());
                            productDTO.setScaleZ(p.getScaleZ());
                            return productDTO;
                        })
                        .collect(Collectors.toList()));
            }

            if (quoteRequest.getCoverings() != null && !quoteRequest.getCoverings().isEmpty()) {
                sceneDTO.setCoverings(quoteRequest.getCoverings().stream()
                        .filter(c -> c.getProductId() != null)
                        .map(c -> {
                            CreateSceneCoveringDTO coveringDTO = new CreateSceneCoveringDTO();
                            coveringDTO.setProductId(c.getProductId());
                            coveringDTO.setSurfaceType(c.getType().toLowerCase());
                            coveringDTO.setSurfaceIdentifier(c.getSurfaceIdentifier());
                            coveringDTO.setRepeatX(c.getRepeatX());
                            coveringDTO.setRepeatY(c.getRepeatY());
                            return coveringDTO;
                        })
                        .collect(Collectors.toList()));
            }

            sceneService.createSceneForUser(sceneDTO, user);
            logger.info("Scene created for quote request user: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to create scene for quote request user {}: {}", user.getEmail(), e.getMessage(), e);
        }

        QuoteRequestEmailData emailData = new QuoteRequestEmailData();
        emailData.setUserFullName(user.getFirstName() + " " + user.getLastName());
        emailData.setUserEmail(user.getEmail());
        emailData.setUserPhone(user.getPhone());
        emailData.setUserCompany(user.getCompany());
        emailData.setRoomDimensions(quoteRequest.getRoomDimensions());

        if (quoteRequest.getWallLengths() != null) {
            emailData.setWallLengths(quoteRequest.getWallLengths().stream()
                    .map(w -> new QuoteRequestEmailData.WallLength(w.getWall(), w.getLength()))
                    .collect(Collectors.toList()));
        }

        emailData.setSceneSnapshot(quoteRequest.getSceneSnapshot());
        emailData.setAdditionalNotes(quoteRequest.getAdditionalNotes());

        if (quoteRequest.getProducts() != null) {
            emailData.setProducts(quoteRequest.getProducts().stream()
                    .map(p -> new QuoteRequestEmailData.ProductDetail(
                            p.getName(), p.getCategory(), p.getColor(), p.getPosition()))
                    .collect(Collectors.toList()));
        }

        if (quoteRequest.getCoverings() != null) {
            emailData.setCoverings(quoteRequest.getCoverings().stream()
                    .map(c -> new QuoteRequestEmailData.CoveringDetail(
                            c.getType(), c.getName(), c.getColor()))
                    .collect(Collectors.toList()));
        }

        emailService.sendQuoteRequest(emailData);
        logger.info("Quote request email sent to industry for user: {}", user.getEmail());

        try {
            emailService.sendUserConfirmation(emailData);
            logger.info("Confirmation email sent to user: {}", user.getEmail());
        } catch (MessagingException e) {
            logger.error("Failed to send confirmation email to user {}: {}", user.getEmail(), e.getMessage());
        }

        return user;
    }

    /**
     * Retrieves all quote requests for a specific user, ordered by creation date
     * (newest first).
     *
     * @param userId the user ID
     * @return list of quote request history entries for the user
     */
    public List<QuoteRequestHistoryDTO> getUserQuoteRequests(Long userId) {
        return quoteRequestRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(qr -> new QuoteRequestHistoryDTO(
                        qr.getId(),
                        qr.getRoomDimensions(),
                        qr.getAdditionalNotes(),
                        qr.getSceneSnapshot(),
                        qr.getCreatedAt(),
                        qr.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Generates a JWT authentication token for a user.
     * Used to automatically log in new users after they submit a quote request.
     *
     * @param user the user to generate a token for
     * @return JWT token string for authentication
     */
    public String generateTokenForUser(User user) {
        return jwtUtil.generateToken(user.getEmail(), user.getId());
    }
}
