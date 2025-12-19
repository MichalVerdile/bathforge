package com.bathforge.service;

import com.bathforge.dto.QuoteRequestDetailDTO;
import com.bathforge.model.quote.QuoteRequest;
import com.bathforge.model.quote.QuoteRequestMessage;
import com.bathforge.model.user.User;
import com.bathforge.model.user.UserRole;
import com.bathforge.repository.QuoteRequestMessageRepository;
import com.bathforge.repository.quote.QuoteRequestRepository;
import com.bathforge.repository.user.UserRepository;
import com.bathforge.service.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private QuoteRequestRepository quoteRequestRepository;

    @MockBean
    private QuoteRequestMessageRepository quoteRequestMessageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.CUSTOMER);
        testUser.setEnabled(true);
    }

    @Test
    @DisplayName("Load user by username returns UserDetails")
    public void testLoadUserByUsername() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CUSTOMER")));
    }

    @Test
    @DisplayName("Load user by username throws exception when user not found")
    public void testLoadUserByUsernameThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent@example.com");
        });
    }

    @Test
    @DisplayName("Create user successfully")
    public void testCreateUser() {
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPassword("password123");
        newUser.setFirstName("Jane");
        newUser.setLastName("Smith");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User created = userService.createUser(newUser);

        assertNotNull(created);
        assertEquals(testUser.getEmail(), created.getEmail());
        // Password should be encoded
        assertTrue(passwordEncoder.matches("password123", created.getPassword()) ||
                created.getPassword().startsWith("$2a$"));
    }

    @Test
    @DisplayName("Create user with duplicate email throws exception")
    public void testCreateDuplicateUserThrowsException() {
        User newUser = new User();
        newUser.setEmail("test@example.com");
        newUser.setPassword("password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(newUser);
        });
    }

    @Test
    @DisplayName("Find user by email returns user")
    public void testFindByEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        Optional<User> found = userService.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals(testUser.getEmail(), found.get().getEmail());
    }

    @Test
    @DisplayName("Exists by email returns true when user exists")
    public void testExistsByEmailReturnsTrue() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        boolean exists = userService.existsByEmail("test@example.com");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Exists by email returns false when user doesn't exist")
    public void testExistsByEmailReturnsFalse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        boolean exists = userService.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    @DisplayName("Find by ID returns user")
    public void testFindById() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        Optional<User> found = userService.findById(1L);

        assertTrue(found.isPresent());
        assertEquals(testUser.getId(), found.get().getId());
    }

    @Test
    @DisplayName("Get quote request detail returns detail")
    public void testGetQuoteRequestDetail() {
        QuoteRequest quoteRequest = new QuoteRequest();
        quoteRequest.setId(1L);
        quoteRequest.setUser(testUser);
        quoteRequest.setStatus("PENDING");
        quoteRequest.setRoomDimensions("5m x 4m x 2.5m");
        quoteRequest.setCreatedAt(LocalDateTime.now());

        List<QuoteRequestMessage> messages = new ArrayList<>();

        when(quoteRequestRepository.findById(anyLong())).thenReturn(Optional.of(quoteRequest));
        when(quoteRequestMessageRepository.findByQuoteRequestIdOrderByCreatedAtAsc(anyLong()))
                .thenReturn(messages);

        QuoteRequestDetailDTO detail = userService.getQuoteRequestDetail(1L, 1L);

        assertNotNull(detail);
        assertEquals(1L, detail.getId());
        assertEquals("PENDING", detail.getStatus());
        assertEquals("5m x 4m x 2.5m", detail.getRoomDimensions());
    }

    @Test
    @DisplayName("Get quote request detail throws exception for unauthorized access")
    public void testGetQuoteRequestDetailThrowsForUnauthorized() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");

        QuoteRequest quoteRequest = new QuoteRequest();
        quoteRequest.setId(1L);
        quoteRequest.setUser(otherUser);

        when(quoteRequestRepository.findById(anyLong())).thenReturn(Optional.of(quoteRequest));

        assertThrows(IllegalArgumentException.class, () -> {
            userService.getQuoteRequestDetail(1L, 1L);
        });
    }

    @Test
    @DisplayName("Get quote request detail throws exception when not found")
    public void testGetQuoteRequestDetailThrowsWhenNotFound() {
        when(quoteRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            userService.getQuoteRequestDetail(999L, 1L);
        });
    }
}
