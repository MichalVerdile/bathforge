package com.bathforge.controller;

import com.bathforge.dto.auth.LoginRequestDTO;
import com.bathforge.dto.auth.LoginResponseDTO;
import com.bathforge.model.user.User;
import com.bathforge.security.JwtUtil;
import com.bathforge.service.user.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param loginRequest the login credentials
     * @return response entity with authentication token and user details, or error
     *         message
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            User user = userService.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtUtil.generateToken(user.getEmail(), user.getId());

            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    user.getEmail(),
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole().name());

            logger.info("User logged in successfully: {}", user.getEmail());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        } catch (Exception e) {
            logger.error("Login error for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during login");
        }
    }

    /**
     * Validates a JWT token and returns user information.
     *
     * @param authHeader the Authorization header containing the Bearer token
     * @return response entity with user details if token is valid, or error message
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);

                if (jwtUtil.validateToken(token, username)) {
                    User user = userService.findByEmail(username)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    LoginResponseDTO response = new LoginResponseDTO(
                            token,
                            user.getEmail(),
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getRole().name());

                    return ResponseEntity.ok(response);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (Exception e) {
            logger.error("Token validation error", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }
}
