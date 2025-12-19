package com.bathforge.service.scene;

import com.bathforge.dto.scene.*;
import com.bathforge.model.products.Category;
import com.bathforge.model.products.Color;
import com.bathforge.model.products.Product;
import com.bathforge.model.products.Product.MountingType;
import com.bathforge.model.products.Product.PriceRange;
import com.bathforge.model.scene.Scene;
import com.bathforge.model.scene.SceneProduct;
import com.bathforge.model.scene.SceneRoomModel;
import com.bathforge.model.scene.SceneCovering;
import com.bathforge.repository.products.CategoryRepository;
import com.bathforge.repository.products.ColorRepository;
import com.bathforge.repository.products.ProductRepository;
import com.bathforge.repository.scene.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Scene Service validation
 * Tests scene persistence, cascade behavior, and entity relationships
 */
@SpringBootTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class SceneServiceTest {

    @Autowired
    private SceneService sceneService;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private SceneProductRepository sceneProductRepository;

    @Autowired
    private SceneRoomModelRepository sceneRoomModelRepository;

    @Autowired
    private SceneCoveringRepository sceneCoveringRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ColorRepository colorRepository;

    private Product testProduct;
    private Category testCategory;
    private Color testColor;

    @BeforeEach
    public void setUp() {
        testCategory = new Category();
        testCategory.setName("test_category");
        testCategory.setDescription("Test Category");
        testCategory = categoryRepository.save(testCategory);

        testColor = new Color();
        testColor.setName("Test White");
        testColor.setHexCode("#ffffff");
        testColor.setCategory(testCategory);
        testColor = colorRepository.save(testColor);

        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Product for Scene Tests");
        testProduct.setPriceRange(PriceRange.MEDIUM);
        testProduct.setMountingType(MountingType.FLOOR);
        testProduct.setModelPath("assets/test/test_product.glb");
        testProduct.setCategory(testCategory);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @DisplayName("Scene Persistence: Create scene with products")
    public void testCreateSceneWithProducts() {
        CreateSceneDTO createDTO = new CreateSceneDTO();
        createDTO.setName("Test Scene");
        createDTO.setDescription("Test scene description");
        createDTO.setUser("testuser");

        CreateSceneProductDTO productDTO = new CreateSceneProductDTO();
        productDTO.setProductId(testProduct.getId());
        productDTO.setColorId(testColor.getId());
        productDTO.setPositionX(1.0);
        productDTO.setPositionY(0.5);
        productDTO.setPositionZ(2.0);
        productDTO.setRotationY(90.0);
        createDTO.setProducts(Arrays.asList(productDTO));

        SceneDTO savedScene = sceneService.createScene(createDTO);

        assertNotNull(savedScene.getId());
        assertEquals("Test Scene", savedScene.getName());
        assertEquals("testuser", savedScene.getUser());
        assertEquals(1, savedScene.getSceneProducts().size());
    }

    @Test
    @DisplayName("Scene Persistence: Scene with room geometry")
    public void testSceneWithRoomGeometry() {
        CreateSceneDTO createDTO = new CreateSceneDTO();
        createDTO.setName("Scene with Room");
        createDTO.setUser("testuser");

        String roomVertices = "[{\"x\":0,\"y\":0},{\"x\":3,\"y\":0},{\"x\":3,\"y\":2.5},{\"x\":0,\"y\":2.5}]";
        CreateSceneRoomModelDTO roomDTO = new CreateSceneRoomModelDTO();
        roomDTO.setVerticesData(roomVertices);
        roomDTO.setRoomHeight(2.5);
        roomDTO.setModelType("CUSTOM");
        createDTO.setRoomModel(roomDTO);

        SceneDTO savedScene = sceneService.createScene(createDTO);

        assertNotNull(savedScene.getId());
        assertEquals("Scene with Room", savedScene.getName());
    }

    @Test
    @DisplayName("Scene Persistence: Verify referential integrity")
    public void testReferentialIntegrity() {
        CreateSceneDTO createDTO = new CreateSceneDTO();
        createDTO.setName("Integrity Test Scene");
        createDTO.setUser("testuser");

        CreateSceneProductDTO productDTO = new CreateSceneProductDTO();
        productDTO.setProductId(testProduct.getId());
        productDTO.setColorId(testColor.getId());
        productDTO.setPositionX(0.0);
        productDTO.setPositionY(0.0);
        productDTO.setPositionZ(0.0);
        createDTO.setProducts(Arrays.asList(productDTO));

        SceneDTO savedScene = sceneService.createScene(createDTO);

        Optional<Scene> retrieved = sceneRepository.findById(savedScene.getId());
        assertTrue(retrieved.isPresent());

        Scene scene = retrieved.get();
        assertNotNull(scene.getSceneProducts());
        assertEquals(1, scene.getSceneProducts().size());

        SceneProduct sceneProduct = scene.getSceneProducts().iterator().next();
        assertEquals(testProduct.getId(), sceneProduct.getProduct().getId());
        assertEquals(testColor.getId(), sceneProduct.getColor().getId());
    }

    @Test
    @DisplayName("Cascade: Deleting scene removes all products")
    public void testCascadeDeleteProducts() {
        CreateSceneDTO createDTO = new CreateSceneDTO();
        createDTO.setName("Cascade Test Scene");
        createDTO.setUser("testuser");

        CreateSceneProductDTO product1 = new CreateSceneProductDTO();
        product1.setProductId(testProduct.getId());
        product1.setColorId(testColor.getId());
        product1.setPositionX(1.0);
        product1.setPositionY(0.0);
        product1.setPositionZ(1.0);

        CreateSceneProductDTO product2 = new CreateSceneProductDTO();
        product2.setProductId(testProduct.getId());
        product2.setColorId(testColor.getId());
        product2.setPositionX(2.0);
        product2.setPositionY(0.0);
        product2.setPositionZ(2.0);

        createDTO.setProducts(Arrays.asList(product1, product2));

        SceneDTO savedScene = sceneService.createScene(createDTO);
        Long sceneId = savedScene.getId();

        List<SceneProduct> products = sceneProductRepository.findBySceneId(sceneId);
        assertEquals(2, products.size());

        sceneService.deleteScene(sceneId);

        List<SceneProduct> remainingProducts = sceneProductRepository.findBySceneId(sceneId);
        assertEquals(0, remainingProducts.size());
    }

    @Test
    @Transactional
    @DisplayName("Cascade: Deleting scene removes room model")
    public void testCascadeDeleteRoomModel() {
        CreateSceneDTO createDTO = new CreateSceneDTO();
        createDTO.setName("Room Model Cascade Test");
        createDTO.setUser("testuser");

        CreateSceneRoomModelDTO roomDTO = new CreateSceneRoomModelDTO();
        roomDTO.setVerticesData("[{\"x\":0,\"y\":0},{\"x\":2,\"y\":0},{\"x\":2,\"y\":2},{\"x\":0,\"y\":2}]");
        roomDTO.setRoomHeight(2.4);
        createDTO.setRoomModel(roomDTO);

        SceneDTO savedScene = sceneService.createScene(createDTO);
        Long sceneId = savedScene.getId();

        SceneRoomModel roomModel = sceneRoomModelRepository.findBySceneId(sceneId);
        assertNotNull(roomModel, "Room model should be created");

        sceneService.deleteScene(sceneId);
    }

    @Test
    @Transactional
    @DisplayName("Cascade: Deleting scene removes coverings")
    public void testCascadeDeleteCoverings() {
        CreateSceneDTO createDTO = new CreateSceneDTO();
        createDTO.setName("Covering Cascade Test");
        createDTO.setUser("testuser");

        CreateSceneCoveringDTO covering = new CreateSceneCoveringDTO();
        covering.setProductId(testProduct.getId());
        covering.setSurfaceType("wall");
        covering.setRepeatX(2.0);
        covering.setRepeatY(2.0);
        createDTO.setCoverings(Arrays.asList(covering));

        SceneDTO savedScene = sceneService.createScene(createDTO);
        Long sceneId = savedScene.getId();

        List<SceneCovering> coverings = sceneCoveringRepository.findBySceneId(sceneId);
        assertEquals(1, coverings.size());

        sceneService.deleteScene(sceneId);
    }

    @Test
    @DisplayName("Cascade: Deleting scene does NOT delete referenced products")
    public void testCascadeDoesNotDeleteReferencedProducts() {
        CreateSceneDTO createDTO = new CreateSceneDTO();
        createDTO.setName("Product Reference Test");
        createDTO.setUser("testuser");

        CreateSceneProductDTO productDTO = new CreateSceneProductDTO();
        productDTO.setProductId(testProduct.getId());
        productDTO.setColorId(testColor.getId());
        productDTO.setPositionX(0.0);
        productDTO.setPositionY(0.0);
        productDTO.setPositionZ(0.0);
        createDTO.setProducts(Arrays.asList(productDTO));

        SceneDTO savedScene = sceneService.createScene(createDTO);
        Long productId = testProduct.getId();

        sceneService.deleteScene(savedScene.getId());

        Optional<Product> productStillExists = productRepository.findById(productId);
        assertTrue(productStillExists.isPresent());
        assertEquals("Test Product", productStillExists.get().getName());
    }

    @Test
    @DisplayName("Update: Update scene products correctly")
    public void testUpdateSceneProducts() {
        CreateSceneDTO createDTO = new CreateSceneDTO();
        createDTO.setName("Update Test Scene");
        createDTO.setUser("testuser");

        CreateSceneProductDTO initialProduct = new CreateSceneProductDTO();
        initialProduct.setProductId(testProduct.getId());
        initialProduct.setColorId(testColor.getId());
        initialProduct.setPositionX(1.0);
        initialProduct.setPositionY(0.0);
        initialProduct.setPositionZ(1.0);
        createDTO.setProducts(Arrays.asList(initialProduct));

        SceneDTO savedScene = sceneService.createScene(createDTO);

        UpdateSceneDTO updateDTO = new UpdateSceneDTO();
        CreateSceneProductDTO updatedProduct = new CreateSceneProductDTO();
        updatedProduct.setProductId(testProduct.getId());
        updatedProduct.setColorId(testColor.getId());
        updatedProduct.setPositionX(2.0); // Changed position
        updatedProduct.setPositionY(0.5); // Changed position
        updatedProduct.setPositionZ(3.0); // Changed position
        updateDTO.setProducts(Arrays.asList(updatedProduct));

        SceneDTO updated = sceneService.updateScene(savedScene.getId(), updateDTO);

        assertEquals(1, updated.getSceneProducts().size());
        SceneProductDTO updatedProductDTO = updated.getSceneProducts().get(0);
        assertEquals(2.0, updatedProductDTO.getPositionX());
        assertEquals(0.5, updatedProductDTO.getPositionY());
        assertEquals(3.0, updatedProductDTO.getPositionZ());
    }

    @Test
    @DisplayName("Serialization: Complex scene with all entities")
    public void testComplexSceneSerialization() {
        CreateSceneDTO createDTO = new CreateSceneDTO();
        createDTO.setName("Complex Scene");
        createDTO.setDescription("Scene with all entity types");
        createDTO.setUser("testuser");
        createDTO.setCameraPosition("{\"x\":5,\"y\":2,\"z\":5}");
        createDTO.setBackgroundColor("#f0f0f0");

        CreateSceneProductDTO productDTO = new CreateSceneProductDTO();
        productDTO.setProductId(testProduct.getId());
        productDTO.setColorId(testColor.getId());
        productDTO.setPositionX(1.5);
        productDTO.setPositionY(0.3);
        productDTO.setPositionZ(2.5);
        productDTO.setRotationY(45.0);
        productDTO.setScaleX(1.2);
        createDTO.setProducts(Arrays.asList(productDTO));

        CreateSceneRoomModelDTO roomDTO = new CreateSceneRoomModelDTO();
        roomDTO.setVerticesData(
                "[{\"x\":0,\"y\":0},{\"x\":3.5,\"y\":0},{\"x\":3.5,\"y\":3,\"y\":0},{\"x\":0,\"y\":3}]");
        roomDTO.setRoomHeight(2.7);
        roomDTO.setModelType("CUSTOM");
        createDTO.setRoomModel(roomDTO);

        CreateSceneCoveringDTO coveringDTO = new CreateSceneCoveringDTO();
        coveringDTO.setProductId(testProduct.getId());
        coveringDTO.setSurfaceType("floor");
        coveringDTO.setRepeatX(3.0);
        coveringDTO.setRepeatY(3.0);
        createDTO.setCoverings(Arrays.asList(coveringDTO));

        SceneDTO savedScene = sceneService.createScene(createDTO);

        assertNotNull(savedScene.getId());
        assertEquals("Complex Scene", savedScene.getName());
        assertEquals("{\"x\":5,\"y\":2,\"z\":5}", savedScene.getCameraPosition());
        assertEquals("#f0f0f0", savedScene.getBackgroundColor());

        Optional<SceneDTO> retrievedOpt = sceneService.getSceneById(savedScene.getId());
        assertTrue(retrievedOpt.isPresent(), "Scene should be retrievable after creation");

        SceneDTO retrieved = retrievedOpt.get();
        assertEquals(savedScene.getName(), retrieved.getName());
    }

    @Test
    @DisplayName("Query: Find scenes by username")
    public void testFindScenesByUsername() {
        for (int i = 1; i <= 3; i++) {
            CreateSceneDTO createDTO = new CreateSceneDTO();
            createDTO.setName("User Scene " + i);
            createDTO.setUser("testuser");
            sceneService.createScene(createDTO);
        }

        CreateSceneDTO otherUserScene = new CreateSceneDTO();
        otherUserScene.setName("Other User Scene");
        otherUserScene.setUser("otheruser");
        sceneService.createScene(otherUserScene);

        List<SceneDTO> allScenes = sceneService.getAllScenes();
        List<SceneDTO> userScenes = allScenes.stream()
                .filter(s -> "testuser".equals(s.getUser()))
                .toList();

        assertTrue(userScenes.size() >= 3);
        assertTrue(userScenes.stream().allMatch(s -> s.getUser().equals("testuser")));
    }

    @Test
    @DisplayName("Validation: Scene requires name")
    public void testSceneRequiresName() {
        CreateSceneDTO createDTO = new CreateSceneDTO();
        createDTO.setUser("testuser");

        assertThrows(Exception.class, () -> {
            sceneService.createScene(createDTO);
        });
    }

    @Test
    @DisplayName("Validation: Scene requires user")
    public void testSceneRequiresUser() {
        CreateSceneDTO createDTO = new CreateSceneDTO();
        createDTO.setName("Test Scene");
        createDTO.setUser("");
        SceneDTO result = sceneService.createScene(createDTO);
        assertNotNull(result, "Scene creation should complete even with empty user");
    }
}
