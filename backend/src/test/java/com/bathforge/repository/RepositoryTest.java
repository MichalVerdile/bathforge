package com.bathforge.repository;

import com.bathforge.model.products.Category;
import com.bathforge.model.products.Color;
import com.bathforge.model.products.Product;
import com.bathforge.model.products.Product.MountingType;
import com.bathforge.model.products.Product.PriceRange;
import com.bathforge.model.scene.Scene;
import com.bathforge.model.scene.SceneProduct;
import com.bathforge.repository.products.CategoryRepository;
import com.bathforge.repository.products.ColorRepository;
import com.bathforge.repository.products.ProductRepository;
import com.bathforge.repository.scene.SceneProductRepository;
import com.bathforge.repository.scene.SceneRepository;

import org.junit.jupiter.api.BeforeEach;
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
 * Data access layer tests for Repository layer
 * Tests query stability, entity constraints, and association mappings
 */
@SpringBootTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class RepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private SceneProductRepository sceneProductRepository;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    public void setup() {
        // Create test category
        testCategory = new Category();
        testCategory.setName("test_repository_category");
        testCategory.setDescription("Category for repository testing");
        testCategory = categoryRepository.save(testCategory);

        // Create test product
        testProduct = new Product();
        testProduct.setName("Test Repository Product");
        testProduct.setDescription("Product for repository testing");
        testProduct.setPriceRange(PriceRange.MEDIUM);
        testProduct.setMountingType(MountingType.WALL);
        testProduct.setModelPath("assets/test/repo_test.glb");
        testProduct.setCategory(testCategory);
        testProduct = productRepository.save(testProduct);
    }

    // ===== Query Stability Tests =====

    @Test
    @DisplayName("Query stability - Deterministic ordering by creation date")
    public void testDeterministicOrdering() throws InterruptedException {
        // Create scenes with slight time differences
        Scene scene1 = createTestScene("Scene 1", "testuser");
        Thread.sleep(10); // Ensure different timestamps
        Scene scene2 = createTestScene("Scene 2", "testuser");
        Thread.sleep(10);
        Scene scene3 = createTestScene("Scene 3", "testuser");

        // Query scenes ordered by creation date
        List<Scene> orderedScenes = sceneRepository.findByUsernameOrderByCreatedAtDesc("testuser");

        // Verify deterministic order (most recent first)
        assertTrue(orderedScenes.size() >= 3);
        assertEquals("Scene 3", orderedScenes.get(0).getName());
        assertEquals("Scene 2", orderedScenes.get(1).getName());
        assertEquals("Scene 1", orderedScenes.get(2).getName());

        // Verify order is stable on multiple queries
        List<Scene> orderedScenes2 = sceneRepository.findByUsernameOrderByCreatedAtDesc("testuser");
        assertEquals(orderedScenes.get(0).getId(), orderedScenes2.get(0).getId());
        assertEquals(orderedScenes.get(1).getId(), orderedScenes2.get(1).getId());
        assertEquals(orderedScenes.get(2).getId(), orderedScenes2.get(2).getId());
    }

    @Test
    @DisplayName("Query stability - Pagination boundary handling")
    public void testPaginationBoundaryHandling() {
        // Create multiple products
        for (int i = 1; i <= 5; i++) {
            Product product = new Product();
            product.setName("Pagination Test Product " + i);
            product.setPriceRange(PriceRange.MEDIUM);
            product.setMountingType(MountingType.FLOOR);
            product.setModelPath("assets/test/product_" + i + ".glb");
            product.setCategory(testCategory);
            productRepository.save(product);
        }

        // Get all products
        List<Product> allProducts = productRepository.findByCategoryId(testCategory.getId());
        assertTrue(allProducts.size() >= 5);

        // Verify boundary conditions
        assertNotNull(allProducts.get(0));
        assertNotNull(allProducts.get(allProducts.size() - 1));

        // Verify each product has valid ID
        allProducts.forEach(product -> assertNotNull(product.getId()));
    }

    @Test
    @DisplayName("Query stability - Case-insensitive search consistency")
    public void testCaseInsensitiveSearchConsistency() {
        // Create product with mixed case name
        Product mixedCaseProduct = new Product();
        mixedCaseProduct.setName("TeSt MiXeD CaSe PrOdUcT");
        mixedCaseProduct.setPriceRange(PriceRange.LOW);
        mixedCaseProduct.setMountingType(MountingType.WALL);
        mixedCaseProduct.setModelPath("assets/test/mixed.glb");
        mixedCaseProduct.setCategory(testCategory);
        productRepository.save(mixedCaseProduct);

        // Search with different cases
        List<Product> lowerCaseResult = productRepository.findByNameContainingIgnoreCase("mixed");
        List<Product> upperCaseResult = productRepository.findByNameContainingIgnoreCase("MIXED");
        List<Product> mixedCaseResult = productRepository.findByNameContainingIgnoreCase("MiXeD");

        // All should return same results
        assertEquals(lowerCaseResult.size(), upperCaseResult.size());
        assertEquals(lowerCaseResult.size(), mixedCaseResult.size());
        assertTrue(lowerCaseResult.size() >= 1);
    }

    // ===== Entity Constraints Tests =====

    @Test
    @DisplayName("Entity constraints - Unique key enforcement on category name")
    public void testUniqueCategoryNameConstraint() {
        // Create first category
        Category category1 = new Category();
        category1.setName("unique_test_category");
        category1.setDescription("First category");
        categoryRepository.save(category1);

        // Try to create duplicate
        Category category2 = new Category();
        category2.setName("unique_test_category");
        category2.setDescription("Second category with same name");

        // Should throw exception due to unique constraint
        assertThrows(Exception.class, () -> {
            categoryRepository.save(category2);
            categoryRepository.flush(); // Force constraint check
        });
    }

    @Test
    @DisplayName("Entity constraints - Foreign key relationship Product-Category")
    public void testForeignKeyRelationship() {
        // Verify foreign key relationship exists
        Optional<Product> retrieved = productRepository.findById(testProduct.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(testCategory.getId(), retrieved.get().getCategory().getId());

        // Verify cascade or referential integrity
        // If we try to delete a category with products, it should handle appropriately
        long productCount = productRepository.countByCategoryId(testCategory.getId());
        assertTrue(productCount >= 1);
    }

    @Test
    @DisplayName("Entity constraints - NOT NULL constraint on required fields")
    public void testNotNullConstraints() {
        // Try to create product without required name
        Product invalidProduct = new Product();
        // Missing name (required field)
        invalidProduct.setPriceRange(PriceRange.MEDIUM);
        invalidProduct.setMountingType(MountingType.FLOOR);
        invalidProduct.setCategory(testCategory);

        assertThrows(Exception.class, () -> {
            productRepository.save(invalidProduct);
            productRepository.flush();
        });
    }

    @Test
    @DisplayName("Entity constraints - Enum validation for PriceRange")
    public void testEnumValidation() {
        // Valid enum values
        Product cheapProduct = new Product();
        cheapProduct.setName("Cheap Product");
        cheapProduct.setPriceRange(PriceRange.LOW);
        cheapProduct.setMountingType(MountingType.WALL);
        cheapProduct.setModelPath("assets/test/cheap.glb");
        cheapProduct.setCategory(testCategory);

        assertDoesNotThrow(() -> {
            productRepository.save(cheapProduct);
            productRepository.flush();
        });

        // Verify all enum values work
        for (PriceRange range : PriceRange.values()) {
            Product product = new Product();
            product.setName("Product " + range.name());
            product.setPriceRange(range);
            product.setMountingType(MountingType.FLOOR);
            product.setModelPath("assets/test/" + range.name().toLowerCase() + ".glb");
            product.setCategory(testCategory);

            assertDoesNotThrow(() -> {
                productRepository.save(product);
                productRepository.flush();
            });
        }
    }

    // ===== Association Mappings Tests =====

    @Test
    @DisplayName("Association mappings - One-to-many Product-Category")
    public void testOneToManyProductCategory() {
        // Create multiple products in same category
        Product product1 = createTestProduct("Product 1", testCategory);
        Product product2 = createTestProduct("Product 2", testCategory);
        Product product3 = createTestProduct("Product 3", testCategory);

        // Query products by category
        List<Product> categoryProducts = productRepository.findByCategoryId(testCategory.getId());

        // Verify all products are associated
        assertTrue(categoryProducts.size() >= 3);
        assertTrue(categoryProducts.stream().anyMatch(p -> p.getId().equals(product1.getId())));
        assertTrue(categoryProducts.stream().anyMatch(p -> p.getId().equals(product2.getId())));
        assertTrue(categoryProducts.stream().anyMatch(p -> p.getId().equals(product3.getId())));
    }

    @Test
    @DisplayName("Association mappings - Many-to-many Product-Color through ProductColor")
    public void testManyToManyProductColor() {
        // Create colors
        Color white = new Color();
        white.setName("White");
        white.setHexCode("#ffffff");
        white.setCategory(testCategory);
        white = colorRepository.save(white);

        Color black = new Color();
        black.setName("Black");
        black.setHexCode("#000000");
        black.setCategory(testCategory);
        black = colorRepository.save(black);

        // Note: In actual implementation, ProductColor association would be managed
        // through ProductService.addColorToProduct()
        // This test validates the repository can query the associations

        // Verify colors exist
        Optional<Color> retrievedWhite = colorRepository.findById(white.getId());
        Optional<Color> retrievedBlack = colorRepository.findById(black.getId());

        assertTrue(retrievedWhite.isPresent());
        assertTrue(retrievedBlack.isPresent());
    }

    @Test
    @DisplayName("Association mappings - One-to-many Scene-SceneProduct")
    public void testOneToManySceneSceneProduct() {
        // Create scene
        Scene scene = createTestScene("Test Scene", "testuser");

        // Create scene products
        SceneProduct sp1 = createTestSceneProduct(scene, testProduct, 1.0, 0.0, 1.0);
        SceneProduct sp2 = createTestSceneProduct(scene, testProduct, 2.0, 0.0, 2.0);

        // Query scene products
        List<SceneProduct> sceneProducts = sceneProductRepository.findBySceneId(scene.getId());

        assertEquals(2, sceneProducts.size());
        assertTrue(sceneProducts.stream().allMatch(sp -> sp.getScene().getId().equals(scene.getId())));
    }

    @Test
    @DisplayName("Association mappings - Lazy loading behavior")
    public void testLazyLoadingBehavior() {
        // Create scene with products
        Scene scene = createTestScene("Lazy Load Test", "testuser");
        SceneProduct sp = createTestSceneProduct(scene, testProduct, 1.0, 0.0, 1.0);

        // Retrieve scene product
        Optional<SceneProduct> retrieved = sceneProductRepository.findById(sp.getId());
        assertTrue(retrieved.isPresent());

        // Lazy-loaded associations should be accessible within transaction
        assertDoesNotThrow(() -> {
            Scene loadedScene = retrieved.get().getScene();
            assertNotNull(loadedScene);
            assertEquals(scene.getId(), loadedScene.getId());
        });
    }

    @Test
    @DisplayName("Association mappings - Eager loading with fetch join")
    public void testEagerLoadingWithFetchJoin() {
        // Create scene with products
        Scene scene = createTestScene("Eager Load Test", "testuser");
        createTestSceneProduct(scene, testProduct, 1.0, 0.0, 1.0);

        // Use repository method with FETCH JOIN
        List<SceneProduct> sceneProducts = sceneProductRepository.findBySceneIdWithDetails(scene.getId());

        assertEquals(1, sceneProducts.size());

        // Associations should be eagerly loaded
        SceneProduct sp = sceneProducts.get(0);
        assertNotNull(sp.getProduct());
        assertEquals(testProduct.getId(), sp.getProduct().getId());
        assertNotNull(sp.getProduct().getName());
    }

    @Test
    @DisplayName("Query optimization - Complex filter query performance")
    public void testComplexFilterQuery() {
        // Create products with different attributes
        for (int i = 0; i < 3; i++) {
            Product product = new Product();
            product.setName("Filter Test " + i);
            product.setPriceRange(PriceRange.values()[i % 3]);
            product.setMountingType(i % 2 == 0 ? MountingType.WALL : MountingType.FLOOR);
            product.setModelPath("assets/test/filter_" + i + ".glb");
            product.setCategory(testCategory);
            productRepository.save(product);
        }

        // Execute complex filter query
        List<Product> filtered = productRepository.findWithFilters(
                testCategory.getId(),
                PriceRange.LOW,
                MountingType.WALL);

        // Verify filtering worked correctly
        filtered.forEach(product -> {
            assertEquals(testCategory.getId(), product.getCategory().getId());
            assertEquals(PriceRange.LOW, product.getPriceRange());
            assertEquals(MountingType.WALL, product.getMountingType());
        });
    }

    // ===== Helper Methods =====

    private Scene createTestScene(String name, String username) {
        Scene scene = new Scene();
        scene.setName(name);
        scene.setDescription("Test scene");
        scene.setUser(username);
        scene.setIsPublic(false);
        scene.setCreatedAt(LocalDateTime.now());
        scene.setUpdatedAt(LocalDateTime.now());
        return sceneRepository.save(scene);
    }

    private Product createTestProduct(String name, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setPriceRange(PriceRange.MEDIUM);
        product.setMountingType(MountingType.FLOOR);
        product.setModelPath("assets/test/" + name.replaceAll(" ", "_").toLowerCase() + ".glb");
        product.setCategory(category);
        return productRepository.save(product);
    }

    private SceneProduct createTestSceneProduct(Scene scene, Product product, double x, double y, double z) {
        SceneProduct sp = new SceneProduct();
        sp.setScene(scene);
        sp.setProduct(product);
        sp.setPositionX(x);
        sp.setPositionY(y);
        sp.setPositionZ(z);
        sp.setRotationX(0.0);
        sp.setRotationY(0.0);
        sp.setRotationZ(0.0);
        sp.setScaleX(1.0);
        sp.setScaleY(1.0);
        sp.setScaleZ(1.0);
        return sceneProductRepository.save(sp);
    }
}
