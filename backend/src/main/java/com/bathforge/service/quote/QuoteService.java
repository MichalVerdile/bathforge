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

    @Transactional
    public User processQuoteRequest(QuoteRequestDTO quoteRequest) throws MessagingException {
        // Create or get user
        User user;
        if (userService.existsByEmail(quoteRequest.getEmail())) {
            // User already exists, fetch from database
            user = userService.findByEmail(quoteRequest.getEmail())
                    .orElseThrow(() -> new IllegalStateException("User exists but could not be retrieved"));
            logger.info("Existing user found: {}", user.getEmail());

            // Update phone and company if provided in the quote request
            boolean needsUpdate = false;
            if (quoteRequest.getPhone() != null && !quoteRequest.getPhone().isEmpty()) {
                user.setPhone(quoteRequest.getPhone());
                needsUpdate = true;
            }
            if (quoteRequest.getCompany() != null && !quoteRequest.getCompany().isEmpty()) {
                user.setCompany(quoteRequest.getCompany());
                needsUpdate = true;
            }
            // Note: User entity will be saved automatically due to @Transactional on method
        } else {
            // Create new user
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

        // Save quote request to database
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

        // Create a scene for the user so they can load it later
        try {
            CreateSceneDTO sceneDTO = new CreateSceneDTO();
            sceneDTO.setName("Quote Request - " + user.getFirstName() + " " + user.getLastName());
            sceneDTO.setDescription("Scene from quote request on " + java.time.LocalDateTime.now());
            sceneDTO.setUser(user.getEmail());
            sceneDTO.setIsPublic(false);

            // Store the quote request data as scene data for reference
            try {
                sceneDTO.setSceneData(objectMapper.writeValueAsString(quoteRequest));
            } catch (JsonProcessingException e) {
                logger.warn("Failed to serialize scene data", e);
            }

            // Convert room data to CreateSceneRoomModelDTO
            if (quoteRequest.getRoomData() != null) {
                CreateSceneRoomModelDTO roomModelDTO = new CreateSceneRoomModelDTO();
                roomModelDTO.setVerticesData(quoteRequest.getRoomData().getVerticesData());
                roomModelDTO.setRoomHeight(quoteRequest.getRoomData().getRoomHeight());
                roomModelDTO.setRoomProperties(quoteRequest.getRoomData().getRoomProperties());
                sceneDTO.setRoomModel(roomModelDTO);
            }

            // Convert products to CreateSceneProductDTO
            if (quoteRequest.getProducts() != null && !quoteRequest.getProducts().isEmpty()) {
                sceneDTO.setProducts(quoteRequest.getProducts().stream()
                        .filter(p -> p.getProductId() != null) // Only include products with IDs
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

            // Convert coverings to CreateSceneCoveringDTO
            if (quoteRequest.getCoverings() != null && !quoteRequest.getCoverings().isEmpty()) {
                sceneDTO.setCoverings(quoteRequest.getCoverings().stream()
                        .filter(c -> c.getProductId() != null) // Only include coverings with IDs
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
            // Log error but don't fail the entire request if scene creation fails
            logger.error("Failed to create scene for quote request user {}: {}", user.getEmail(), e.getMessage(), e);
        }

        // Prepare email data
        QuoteRequestEmailData emailData = new QuoteRequestEmailData();
        emailData.setUserFullName(user.getFirstName() + " " + user.getLastName());
        emailData.setUserEmail(user.getEmail());
        emailData.setUserPhone(user.getPhone());
        emailData.setUserCompany(user.getCompany());
        emailData.setRoomDimensions(quoteRequest.getRoomDimensions());

        // Convert wall lengths
        if (quoteRequest.getWallLengths() != null) {
            emailData.setWallLengths(quoteRequest.getWallLengths().stream()
                    .map(w -> new QuoteRequestEmailData.WallLength(w.getWall(), w.getLength()))
                    .collect(Collectors.toList()));
        }

        emailData.setSceneSnapshot(quoteRequest.getSceneSnapshot());
        emailData.setAdditionalNotes(quoteRequest.getAdditionalNotes());

        // Convert products
        if (quoteRequest.getProducts() != null) {
            emailData.setProducts(quoteRequest.getProducts().stream()
                    .map(p -> new QuoteRequestEmailData.ProductDetail(
                            p.getName(), p.getCategory(), p.getColor(), p.getPosition()))
                    .collect(Collectors.toList()));
        }

        // Convert coverings
        if (quoteRequest.getCoverings() != null) {
            emailData.setCoverings(quoteRequest.getCoverings().stream()
                    .map(c -> new QuoteRequestEmailData.CoveringDetail(
                            c.getType(), c.getName(), c.getColor()))
                    .collect(Collectors.toList()));
        }

        // Send email to industry
        emailService.sendQuoteRequest(emailData);
        logger.info("Quote request email sent to industry for user: {}", user.getEmail());

        // Send confirmation email to user
        try {
            emailService.sendUserConfirmation(emailData);
            logger.info("Confirmation email sent to user: {}", user.getEmail());
        } catch (MessagingException e) {
            // Log the error but don't fail the entire request if confirmation email fails
            logger.error("Failed to send confirmation email to user {}: {}", user.getEmail(), e.getMessage());
        }

        return user;
    }

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
     * Generate a JWT token for the given user.
     * This is used to automatically log in new users after they submit a quote
     * request.
     */
    public String generateTokenForUser(User user) {
        return jwtUtil.generateToken(user.getEmail(), user.getId());
    }
}
