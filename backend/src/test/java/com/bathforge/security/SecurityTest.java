package com.bathforge.security;

import com.bathforge.model.user.User;
import com.bathforge.model.user.UserRole;
import com.bathforge.repository.user.UserRepository;
import com.bathforge.service.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Security and Authentication
 * Tests password hashing, authentication, and security constraints
 */
@SpringBootTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class SecurityTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // ===== Password Hashing Tests =====

    @Test
    @DisplayName("Security: Passwords are stored as BCrypt hashes")
    public void testPasswordsStoredAsBCryptHashes() {
        String plainPassword = "SecurePassword123!";

        User user = new User();
        user.setEmail("security@test.com");
        user.setPassword(plainPassword);
        user.setFirstName("Security");
        user.setLastName("Test");
        user.setRole(UserRole.CUSTOMER);

        User savedUser = userService.createUser(user);

        // Verify password was hashed
        assertNotEquals(plainPassword, savedUser.getPassword());

        // Verify it's a BCrypt hash (starts with $2a$, $2b$, or $2y$)
        assertTrue(savedUser.getPassword().startsWith("$2"),
                "Password should be BCrypt hashed");

        // Verify BCrypt hash length (approximately 60 characters)
        assertEquals(60, savedUser.getPassword().length(),
                "BCrypt hash should be 60 characters");
    }

    @Test
    @DisplayName("Security: Password verification works correctly")
    public void testPasswordVerification() {
        String plainPassword = "MySecretPassword456";

        User user = new User();
        user.setEmail("verify@test.com");
        user.setPassword(plainPassword);
        user.setFirstName("Verify");
        user.setLastName("Test");
        user.setRole(UserRole.CUSTOMER);

        User savedUser = userService.createUser(user);

        // Verify password matches
        assertTrue(passwordEncoder.matches(plainPassword, savedUser.getPassword()),
                "Plain password should match hashed password");

        // Verify wrong password doesn't match
        assertFalse(passwordEncoder.matches("WrongPassword", savedUser.getPassword()),
                "Wrong password should not match");
    }

    @Test
    @DisplayName("Security: Passwords never stored in plaintext")
    public void testPasswordsNeverPlaintext() {
        String plainPassword = "PlainTextPassword789";

        User user = new User();
        user.setEmail("plaintext@test.com");
        user.setPassword(plainPassword);
        user.setFirstName("Plain");
        user.setLastName("Text");
        user.setRole(UserRole.CUSTOMER);

        User savedUser = userService.createUser(user);

        // Retrieve from database
        Optional<User> retrieved = userRepository.findByEmail("plaintext@test.com");
        assertTrue(retrieved.isPresent());

        // Verify stored password is NOT plaintext
        assertNotEquals(plainPassword, retrieved.get().getPassword());

        // Verify it doesn't contain the plaintext anywhere
        assertFalse(retrieved.get().getPassword().contains(plainPassword),
                "Hashed password should not contain plaintext");
    }

    @Test
    @DisplayName("Security: Different users get different password hashes")
    public void testUniqueSaltsPerUser() {
        String commonPassword = "CommonPassword123";

        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setPassword(commonPassword);
        user1.setFirstName("User");
        user1.setLastName("One");
        user1.setRole(UserRole.CUSTOMER);
        User savedUser1 = userService.createUser(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setPassword(commonPassword);
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setRole(UserRole.CUSTOMER);
        User savedUser2 = userService.createUser(user2);

        // Even with same password, hashes should be different (due to salt)
        assertNotEquals(savedUser1.getPassword(), savedUser2.getPassword(),
                "Same password should produce different hashes due to unique salts");
    }

    // ===== JWT Token Tests =====

    @Test
    @DisplayName("JWT: Token generation works correctly")
    public void testJWTTokenGeneration() {
        String username = "test@example.com";
        Long userId = 123L;

        String token = jwtUtil.generateToken(username, userId);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3, "JWT should have 3 parts separated by dots");
    }

    @Test
    @DisplayName("JWT: Can extract username from token")
    public void testExtractUsernameFromToken() {
        String username = "extract@example.com";
        Long userId = 456L;

        String token = jwtUtil.generateToken(username, userId);
        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("JWT: Token validation works for valid tokens")
    public void testValidTokenValidation() {
        String username = "valid@example.com";
        Long userId = 789L;

        String token = jwtUtil.generateToken(username, userId);
        Boolean isValid = jwtUtil.validateToken(token, username);

        assertTrue(isValid, "Valid token should pass validation");
    }

    @Test
    @DisplayName("JWT: Token validation fails for wrong username")
    public void testInvalidTokenValidation() {
        String username = "correct@example.com";
        Long userId = 101L;

        String token = jwtUtil.generateToken(username, userId);
        Boolean isValid = jwtUtil.validateToken(token, "wrong@example.com");

        assertFalse(isValid, "Token should be invalid for different username");
    }

    @Test
    @DisplayName("JWT: Token validation fails for tampered token")
    public void testTamperedTokenValidation() {
        String username = "tamper@example.com";
        Long userId = 202L;

        String token = jwtUtil.generateToken(username, userId);

        // Tamper with the token
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        assertThrows(Exception.class, () -> {
            jwtUtil.validateToken(tamperedToken, username);
        }, "Tampered token should throw exception");
    }

    // ===== User Authentication Tests =====

    @Test
    @DisplayName("Auth: User can be loaded by username")
    public void testLoadUserByUsername() {
        User user = new User();
        user.setEmail("loaduser@test.com");
        user.setPassword("Password123");
        user.setFirstName("Load");
        user.setLastName("User");
        user.setRole(UserRole.CUSTOMER);
        userService.createUser(user);

        var userDetails = userService.loadUserByUsername("loaduser@test.com");

        assertNotNull(userDetails);
        assertEquals("loaduser@test.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    @DisplayName("Auth: Loading non-existent user throws exception")
    public void testLoadNonExistentUser() {
        assertThrows(Exception.class, () -> {
            userService.loadUserByUsername("nonexistent@test.com");
        }, "Loading non-existent user should throw exception");
    }

    @Test
    @DisplayName("Auth: User role is correctly assigned")
    public void testUserRoleAssignment() {
        User regularUser = new User();
        regularUser.setEmail("regular@test.com");
        regularUser.setPassword("Password123");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setRole(UserRole.CUSTOMER);
        User savedRegular = userService.createUser(regularUser);

        assertEquals(UserRole.CUSTOMER, savedRegular.getRole());

        User adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("AdminPass123");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(UserRole.ADMIN);
        User savedAdmin = userService.createUser(adminUser);

        assertEquals(UserRole.ADMIN, savedAdmin.getRole());
    }

    // ===== Security Constraints Tests =====

    @Test
    @DisplayName("Constraints: Email must be unique")
    public void testUniqueEmailConstraint() {
        User user1 = new User();
        user1.setEmail("duplicate@test.com");
        user1.setPassword("Password123");
        user1.setFirstName("First");
        user1.setLastName("User");
        user1.setRole(UserRole.CUSTOMER);
        userService.createUser(user1);

        User user2 = new User();
        user2.setEmail("duplicate@test.com");
        user2.setPassword("Password456");
        user2.setFirstName("Second");
        user2.setLastName("User");
        user2.setRole(UserRole.CUSTOMER);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(user2);
        }, "Duplicate email should throw exception");
    }

    @Test
    @DisplayName("Constraints: User must have password")
    public void testPasswordRequired() {
        User user = new User();
        user.setEmail("nopass@test.com");
        // No password set
        user.setFirstName("No");
        user.setLastName("Pass");
        user.setRole(UserRole.CUSTOMER);

        assertThrows(Exception.class, () -> {
            userService.createUser(user);
        });
    }

    @Test
    @DisplayName("Constraints: User must have email")
    public void testEmailRequired() {
        User user = new User();
        // No email set
        user.setPassword("Password123");
        user.setFirstName("No");
        user.setLastName("Email");
        user.setRole(UserRole.CUSTOMER);

        assertThrows(Exception.class, () -> {
            userService.createUser(user);
        });
    }

    // ===== Password Security Best Practices =====

    @Test
    @DisplayName("Password: BCrypt uses appropriate cost factor")
    public void testBCryptCostFactor() {
        String password = "TestPassword123";
        String hashed = passwordEncoder.encode(password);

        // BCrypt hash format: $2a$10$...
        // Cost factor should be at least 10
        String[] parts = hashed.split("\\$");
        int costFactor = Integer.parseInt(parts[2]);

        assertTrue(costFactor >= 10,
                "BCrypt cost factor should be at least 10 for security");
    }

    @Test
    @DisplayName("Password: Encoding is consistent with encoder")
    public void testPasswordEncodingConsistency() {
        String password = "ConsistencyTest123";

        String encoded1 = passwordEncoder.encode(password);
        String encoded2 = passwordEncoder.encode(password);

        // Different salts mean different hashes
        assertNotEquals(encoded1, encoded2,
                "Same password should produce different hashes");

        // But both should match the original
        assertTrue(passwordEncoder.matches(password, encoded1));
        assertTrue(passwordEncoder.matches(password, encoded2));
    }
}
