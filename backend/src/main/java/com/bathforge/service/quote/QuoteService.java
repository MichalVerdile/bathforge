package com.bathforge.service.quote;

import com.bathforge.dto.quote.QuoteRequestDTO;
import com.bathforge.dto.quote.QuoteRequestHistoryDTO;
import com.bathforge.dto.scene.CreateSceneDTO;
import com.bathforge.model.quote.QuoteRequest;
import com.bathforge.model.user.User;
import com.bathforge.repository.quote.QuoteRequestRepository;
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

    @Autowired
    public QuoteService(UserService userService, EmailService emailService,
            QuoteRequestRepository quoteRequestRepository, SceneService sceneService,
            ObjectMapper objectMapper) {
        this.userService = userService;
        this.emailService = emailService;
        this.quoteRequestRepository = quoteRequestRepository;
        this.sceneService = sceneService;
        this.objectMapper = objectMapper;
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
            // Since we don't have proper product IDs from the frontend,
            // we store the raw data for now
            try {
                sceneDTO.setSceneData(objectMapper.writeValueAsString(quoteRequest));
            } catch (JsonProcessingException e) {
                logger.warn("Failed to serialize scene data", e);
            }

            sceneService.createScene(sceneDTO);
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
}
