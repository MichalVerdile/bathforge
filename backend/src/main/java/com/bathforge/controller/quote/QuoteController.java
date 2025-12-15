package com.bathforge.controller.quote;

import com.bathforge.dto.quote.QuoteRequestDTO;
import com.bathforge.dto.quote.QuoteResponseDTO;
import com.bathforge.model.user.User;
import com.bathforge.service.quote.QuoteService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quote")
@CrossOrigin(origins = "http://localhost:3000")
public class QuoteController {

    private static final Logger logger = LoggerFactory.getLogger(QuoteController.class);

    private final QuoteService quoteService;

    @Autowired
    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping("/request")
    public ResponseEntity<QuoteResponseDTO> submitQuoteRequest(@Valid @RequestBody QuoteRequestDTO quoteRequest) {
        try {
            User user = quoteService.processQuoteRequest(quoteRequest);
            String token = quoteService.generateTokenForUser(user);

            QuoteResponseDTO response = new QuoteResponseDTO(
                    true,
                    "Quote request submitted successfully. An account has been created and the request has been sent to our industry partners.",
                    user.getId(),
                    user.getEmail(),
                    token);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new QuoteResponseDTO(false, e.getMessage()));

        } catch (MessagingException e) {
            logger.error("Email sending failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new QuoteResponseDTO(false, "Failed to send quote request email. Please try again later."));

        } catch (Exception e) {
            logger.error("Unexpected error processing quote request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new QuoteResponseDTO(false, "An unexpected error occurred. Please try again later."));
        }
    }
}
