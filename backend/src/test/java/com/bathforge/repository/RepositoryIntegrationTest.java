package com.bathforge.repository;

import com.bathforge.dto.products.CategoryDTO;
import com.bathforge.model.products.Category;
import com.bathforge.model.products.Color;
import com.bathforge.model.quote.QuoteRequest;
import com.bathforge.model.quote.QuoteRequestMessage;
import com.bathforge.model.user.User;
import com.bathforge.model.user.UserRole;
import com.bathforge.repository.products.CategoryRepository;
import com.bathforge.repository.products.ColorRepository;
import com.bathforge.repository.quote.QuoteRequestRepository;
import com.bathforge.repository.user.UserRepository;
import com.bathforge.service.products.CategoryService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive repository tests covering all data persistence operations.
 * Tests repository-specific queries, constraints, and relationships.
 */
@SpringBootTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class RepositoryIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuoteRequestRepository quoteRequestRepository;

    @Autowired
    private QuoteRequestMessageRepository quoteRequestMessageRepository;

    @Autowired
    private CategoryService categoryService;

    @Test
    @DisplayName("CategoryRepository: Find by name returns category")
    public void testCategoryFindByName() {
        categoryService.createCategory(
                new CategoryDTO("test_category", "Test Category"));

        Optional<Category> found = categoryRepository.findByNameIgnoreCase("test_category");

        assertTrue(found.isPresent());
        assertEquals("test_category", found.get().getName());
    }

    @Test
    @DisplayName("CategoryRepository: Find by name ignores case")
    public void testCategoryFindByNameIgnoreCase() {
        categoryService.createCategory(new CategoryDTO("lowercase_cat", "Test"));

        Optional<Category> found = categoryRepository.findByNameIgnoreCase("LOWERCASE_CAT");

        assertTrue(found.isPresent());
        assertEquals("lowercase_cat", found.get().getName());
    }

    @Test
    @DisplayName("CategoryRepository: Find all ordered by name")
    public void testCategoryFindAllOrderedByName() {
        categoryService.createCategory(new CategoryDTO("zebra", "Zebra"));
        categoryService.createCategory(new CategoryDTO("alpha", "Alpha"));
        categoryService.createCategory(new CategoryDTO("middle", "Middle"));

        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();

        assertTrue(categories.size() >= 3);

        List<String> testCategories = categories.stream()
                .map(Category::getName)
                .filter(name -> name.equals("alpha") || name.equals("middle") || name.equals("zebra"))
                .toList();

        assertEquals(3, testCategories.size());
        assertEquals("alpha", testCategories.get(0));
        assertEquals("middle", testCategories.get(1));
        assertEquals("zebra", testCategories.get(2));
    }

    @Test
    @DisplayName("CategoryRepository: Exists by name returns true")
    public void testCategoryExistsByName() {
        categoryService.createCategory(new CategoryDTO("exists_test", "Exists"));

        boolean exists = categoryRepository.existsByNameIgnoreCase("exists_test");

        assertTrue(exists);
    }

    @Test
    @DisplayName("CategoryRepository: Exists by name ignores case")
    public void testCategoryExistsByNameIgnoreCase() {
        categoryService.createCategory(new CategoryDTO("casesensitive", "Test"));

        boolean exists = categoryRepository.existsByNameIgnoreCase("CASESENSITIVE");

        assertTrue(exists);
    }

    @Test
    @DisplayName("ColorRepository: Find by name returns color")
    public void testColorFindByName() {
        CategoryDTO category = categoryService.createCategory(new CategoryDTO("cat1", "Category 1"));

        Color color = new Color();
        color.setName("Red");
        color.setHexCode("#ff0000");
        Category cat = categoryRepository.findById(category.getId()).orElseThrow();
        color.setCategory(cat);
        colorRepository.save(color);

        Optional<Color> found = colorRepository.findByNameIgnoreCaseAndCategory("Red", cat);

        assertTrue(found.isPresent());
        assertEquals("Red", found.get().getName());
    }

    @Test
    @DisplayName("ColorRepository: Find by category ID returns colors")
    public void testColorFindByCategoryId() {
        CategoryDTO category = categoryService.createCategory(new CategoryDTO("color_cat", "Color Category"));

        Category cat = categoryRepository.findById(category.getId()).orElseThrow();

        Color color1 = new Color();
        color1.setName("Blue");
        color1.setHexCode("#0000ff");
        color1.setCategory(cat);
        colorRepository.save(color1);

        Color color2 = new Color();
        color2.setName("Green");
        color2.setHexCode("#00ff00");
        color2.setCategory(cat);
        colorRepository.save(color2);

        List<Color> colors = colorRepository.findByCategoryId(category.getId());

        assertEquals(2, colors.size());
    }

    @Test
    @DisplayName("ColorRepository: Find by name and category ID")
    public void testColorFindByNameAndCategoryId() {
        CategoryDTO category = categoryService.createCategory(new CategoryDTO("unique_cat", "Unique"));

        Category cat = categoryRepository.findById(category.getId()).orElseThrow();

        Color color = new Color();
        color.setName("Yellow");
        color.setHexCode("#ffff00");
        color.setCategory(cat);
        colorRepository.save(color);

        Optional<Color> found = colorRepository.findByNameIgnoreCaseAndCategory("Yellow", cat);

        assertTrue(found.isPresent());
        assertEquals("Yellow", found.get().getName());
    }

    @Test
    @DisplayName("UserRepository: Find by email returns user")
    public void testUserFindByEmail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(UserRole.CUSTOMER);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    @DisplayName("UserRepository: Exists by email returns true")
    public void testUserExistsByEmail() {
        User user = new User();
        user.setEmail("exists@example.com");
        user.setPassword("password");
        user.setFirstName("Exists");
        user.setLastName("Test");
        user.setRole(UserRole.CUSTOMER);
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("exists@example.com");

        assertTrue(exists);
    }

    @Test
    @DisplayName("UserRepository: Email is unique constraint")
    public void testUserEmailUniqueConstraint() {
        User user1 = new User();
        user1.setEmail("unique@example.com");
        user1.setPassword("password1");
        user1.setFirstName("User");
        user1.setLastName("One");
        user1.setRole(UserRole.CUSTOMER);
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("unique@example.com");
        user2.setPassword("password2");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setRole(UserRole.CUSTOMER);

        assertThrows(Exception.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }

    @Test
    @DisplayName("QuoteRequestRepository: Find by user ID ordered by created date")
    public void testQuoteRequestFindByUserId() {
        User user = new User();
        user.setEmail("quote@example.com");
        user.setPassword("password");
        user.setFirstName("Quote");
        user.setLastName("User");
        user.setRole(UserRole.CUSTOMER);
        userRepository.save(user);

        QuoteRequest qr1 = new QuoteRequest();
        qr1.setUser(user);
        qr1.setRoomDimensions("5m x 4m");
        qr1.setCreatedAt(LocalDateTime.now().minusDays(1));
        quoteRequestRepository.save(qr1);

        QuoteRequest qr2 = new QuoteRequest();
        qr2.setUser(user);
        qr2.setRoomDimensions("6m x 5m");
        qr2.setCreatedAt(LocalDateTime.now());
        quoteRequestRepository.save(qr2);

        List<QuoteRequest> requests = quoteRequestRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertEquals(2, requests.size());
        assertEquals("6m x 5m", requests.get(0).getRoomDimensions());
    }

    @Test
    @DisplayName("QuoteRequestMessageRepository: Find by quote request ID ordered by created date")
    public void testQuoteRequestMessageFindByQuoteRequestId() {
        User user = new User();
        user.setEmail("message@example.com");
        user.setPassword("password");
        user.setFirstName("Message");
        user.setLastName("User");
        user.setRole(UserRole.CUSTOMER);
        userRepository.save(user);

        QuoteRequest qr = new QuoteRequest();
        qr.setUser(user);
        qr.setRoomDimensions("5m x 4m");
        quoteRequestRepository.save(qr);

        QuoteRequestMessage msg1 = new QuoteRequestMessage(qr, "First message", "USER");
        msg1.setCreatedAt(LocalDateTime.now().minusHours(1));
        quoteRequestMessageRepository.save(msg1);

        QuoteRequestMessage msg2 = new QuoteRequestMessage(qr, "Second message", "ADMIN");
        msg2.setCreatedAt(LocalDateTime.now());
        quoteRequestMessageRepository.save(msg2);

        List<QuoteRequestMessage> messages = quoteRequestMessageRepository
                .findByQuoteRequestIdOrderByCreatedAtAsc(qr.getId());

        assertEquals(2, messages.size());
        assertEquals("First message", messages.get(0).getMessage());
        assertEquals("Second message", messages.get(1).getMessage());
    }
}
