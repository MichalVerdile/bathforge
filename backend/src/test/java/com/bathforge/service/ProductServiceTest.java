package com.bathforge.service;

import com.bathforge.dto.products.CategoryDTO;
import com.bathforge.dto.products.ColorDTO;
import com.bathforge.dto.products.ProductDTO;
import com.bathforge.model.products.Product.MountingType;
import com.bathforge.model.products.Product.PriceRange;
import com.bathforge.service.products.CategoryService;
import com.bathforge.service.products.ColorService;
import com.bathforge.service.products.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Service layer tests for Product filtering algorithms
 * Tests category-based filtering, price ranges, mounting types, and text search
 */
@SpringBootTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ColorService colorService;

    private CategoryDTO basinCategory;
    private CategoryDTO bathtubCategory;
    private ProductDTO expensiveWallBasin;
    private ProductDTO cheapFloorBasin;
    private ProductDTO mediumBathtub;

    @BeforeEach
    public void setup() {
        basinCategory = categoryService.createCategory(
                new CategoryDTO("test_basins", "Test basins category"));

        bathtubCategory = categoryService.createCategory(
                new CategoryDTO("test_bathtubs", "Test bathtubs category"));

        expensiveWallBasin = productService.createProduct(new ProductDTO(
                "Expensive Wall Basin",
                "A high-end wall-mounted basin",
                PriceRange.HIGH,
                "assets/test/expensive_wall_basin.glb",
                MountingType.WALL,
                basinCategory.getId()));

        cheapFloorBasin = productService.createProduct(new ProductDTO(
                "Cheap Floor Basin",
                "A budget floor-mounted basin",
                PriceRange.LOW,
                "assets/test/cheap_floor_basin.glb",
                MountingType.FLOOR,
                basinCategory.getId()));

        mediumBathtub = productService.createProduct(new ProductDTO(
                "Medium Bathtub",
                "A mid-range bathtub",
                PriceRange.MEDIUM,
                "assets/test/medium_bathtub.glb",
                MountingType.FLOOR,
                bathtubCategory.getId()));
    }

    @Test
    @DisplayName("Product filtering - Filter by category ID returns correct products")
    public void testFilterByCategoryId() {
        List<ProductDTO> basinProducts = productService.getProductsByCategoryId(basinCategory.getId());
        List<ProductDTO> bathtubProducts = productService.getProductsByCategoryId(bathtubCategory.getId());

        assertEquals(2, basinProducts.size());
        assertEquals(1, bathtubProducts.size());

        assertTrue(basinProducts.stream().allMatch(p -> p.getCategoryId().equals(basinCategory.getId())));
        assertTrue(bathtubProducts.stream().allMatch(p -> p.getCategoryId().equals(bathtubCategory.getId())));
    }

    @Test
    @DisplayName("Product filtering - Filter by category name returns correct products")
    public void testFilterByCategoryName() {
        List<ProductDTO> basinProducts = productService.getProductsByCategoryName("test_basins");
        List<ProductDTO> bathtubProducts = productService.getProductsByCategoryName("test_bathtubs");

        assertEquals(2, basinProducts.size());
        assertEquals(1, bathtubProducts.size());

        assertTrue(basinProducts.stream().anyMatch(p -> p.getName().equals("Expensive Wall Basin")));
        assertTrue(basinProducts.stream().anyMatch(p -> p.getName().equals("Cheap Floor Basin")));
        assertTrue(bathtubProducts.stream().anyMatch(p -> p.getName().equals("Medium Bathtub")));
    }

    @Test
    @DisplayName("Product filtering - Filter by price range returns correct products")
    public void testFilterByPriceRange() {
        List<ProductDTO> cheapProducts = productService.getProductsByPriceRange(PriceRange.LOW);
        List<ProductDTO> mediumProducts = productService.getProductsByPriceRange(PriceRange.MEDIUM);
        List<ProductDTO> expensiveProducts = productService.getProductsByPriceRange(PriceRange.HIGH);

        assertTrue(cheapProducts.size() >= 1);
        assertTrue(mediumProducts.size() >= 1);
        assertTrue(expensiveProducts.size() >= 1);

        assertTrue(cheapProducts.stream().allMatch(p -> p.getPriceRange() == PriceRange.LOW));
        assertTrue(mediumProducts.stream().allMatch(p -> p.getPriceRange() == PriceRange.MEDIUM));
        assertTrue(expensiveProducts.stream().allMatch(p -> p.getPriceRange() == PriceRange.HIGH));
    }

    @Test
    @DisplayName("Product filtering - Filter by mounting type returns correct products")
    public void testFilterByMountingType() {
        List<ProductDTO> wallProducts = productService.getProductsByMountingType(MountingType.WALL);
        List<ProductDTO> floorProducts = productService.getProductsByMountingType(MountingType.FLOOR);

        assertTrue(wallProducts.size() >= 1);
        assertTrue(floorProducts.size() >= 2);

        assertTrue(wallProducts.stream().allMatch(p -> p.getMountingType() == MountingType.WALL));
        assertTrue(floorProducts.stream().allMatch(p -> p.getMountingType() == MountingType.FLOOR));

        assertTrue(wallProducts.stream().anyMatch(p -> p.getName().equals("Expensive Wall Basin")));
    }

    @Test
    @DisplayName("Product filtering - Text search by name finds products")
    public void testTextSearchByName() {
        List<ProductDTO> basinResults = productService.searchProductsByName("Basin");
        List<ProductDTO> bathtubResults = productService.searchProductsByName("Bathtub");
        List<ProductDTO> expensiveResults = productService.searchProductsByName("Expensive");

        assertTrue(basinResults.size() >= 2);
        assertTrue(bathtubResults.size() >= 1);
        assertTrue(expensiveResults.size() >= 1);

        assertTrue(basinResults.stream().anyMatch(p -> p.getName().contains("Basin")));
        assertTrue(bathtubResults.stream().anyMatch(p -> p.getName().contains("Bathtub")));
        assertTrue(expensiveResults.stream().anyMatch(p -> p.getName().contains("Expensive")));
    }

    @Test
    @DisplayName("Product filtering - Combined filters work correctly")
    public void testCombinedFilters() {
        List<ProductDTO> cheapBasins = productService.getProductsWithFilters(
                basinCategory.getId(),
                PriceRange.LOW,
                null);

        assertEquals(1, cheapBasins.size());
        assertEquals("Cheap Floor Basin", cheapBasins.get(0).getName());

        List<ProductDTO> wallBasins = productService.getProductsWithFilters(
                basinCategory.getId(),
                null,
                MountingType.WALL);

        assertTrue(wallBasins.size() >= 1);
        assertTrue(wallBasins.stream().anyMatch(p -> p.getName().equals("Expensive Wall Basin")));

        List<ProductDTO> expensiveWallProducts = productService.getProductsWithFilters(
                null,
                PriceRange.HIGH,
                MountingType.WALL);

        assertTrue(expensiveWallProducts.size() >= 1);
        assertTrue(expensiveWallProducts.stream().anyMatch(p -> p.getName().equals("Expensive Wall Basin")));

        List<ProductDTO> specificProduct = productService.getProductsWithFilters(
                basinCategory.getId(),
                PriceRange.HIGH,
                MountingType.WALL);

        assertTrue(specificProduct.size() >= 1);
        assertTrue(specificProduct.stream().anyMatch(p -> p.getName().equals("Expensive Wall Basin")));
    }

    @Test
    @DisplayName("Product filtering - Empty results when no matches")
    public void testEmptyResults() {
        List<ProductDTO> noResults = productService.searchProductsByName("NonExistentProduct");
        assertEquals(0, noResults.size());

        List<ProductDTO> impossibleFilter = productService.getProductsWithFilters(
                basinCategory.getId(),
                PriceRange.LOW,
                MountingType.WALL);

        assertEquals(0, impossibleFilter.size());
    }

    @Test
    @DisplayName("Product filtering - Edge case with multiple price ranges")
    public void testMultiplePriceRanges() {
        List<ProductDTO> allProducts = productService.getAllProducts();

        long cheapCount = allProducts.stream()
                .filter(p -> p.getPriceRange() == PriceRange.LOW)
                .count();
        long mediumCount = allProducts.stream()
                .filter(p -> p.getPriceRange() == PriceRange.MEDIUM)
                .count();
        long expensiveCount = allProducts.stream()
                .filter(p -> p.getPriceRange() == PriceRange.HIGH)
                .count();

        assertTrue(cheapCount >= 1);
        assertTrue(mediumCount >= 1);
        assertTrue(expensiveCount >= 1);
    }

    @Test
    @DisplayName("Product color association - Add and retrieve colors for product")
    public void testProductColorAssociation() {
        ColorDTO whiteColor = colorService.createColor(
                new ColorDTO("White", "#ffffff", basinCategory.getId()));
        ColorDTO blackColor = colorService.createColor(
                new ColorDTO("Black", "#000000", basinCategory.getId()));

        productService.addColorToProduct(expensiveWallBasin.getId(), whiteColor.getId());
        productService.addColorToProduct(expensiveWallBasin.getId(), blackColor.getId());

        List<ColorDTO> productColors = productService.getColorsForProduct(expensiveWallBasin.getId());
        assertEquals(2, productColors.size());

        assertTrue(productColors.stream().anyMatch(c -> c.getName().equals("White")));
        assertTrue(productColors.stream().anyMatch(c -> c.getName().equals("Black")));
    }

    @Test
    @DisplayName("Product color association - Remove color from product")
    public void testRemoveColorFromProduct() {
        ColorDTO testColor = colorService.createColor(
                new ColorDTO("Test Color", "#123456", basinCategory.getId()));

        productService.addColorToProduct(cheapFloorBasin.getId(), testColor.getId());

        assertEquals(1, productService.getColorsForProduct(cheapFloorBasin.getId()).size());

        productService.removeColorFromProduct(cheapFloorBasin.getId(), testColor.getId());

        assertEquals(0, productService.getColorsForProduct(cheapFloorBasin.getId()).size());
    }

    @Test
    @DisplayName("Product CRUD - Create product with all attributes")
    public void testCreateProduct() {
        ProductDTO newProduct = new ProductDTO(
                "New Test Product",
                "A product created in test",
                PriceRange.MEDIUM,
                "assets/test/new_product.glb",
                MountingType.WALL,
                basinCategory.getId());

        ProductDTO created = productService.createProduct(newProduct);

        assertNotNull(created.getId());
        assertEquals("New Test Product", created.getName());
        assertEquals("A product created in test", created.getDescription());
        assertEquals(PriceRange.MEDIUM, created.getPriceRange());
        assertEquals(MountingType.WALL, created.getMountingType());
        assertEquals(basinCategory.getId(), created.getCategoryId());
    }

    @Test
    @DisplayName("Product CRUD - Update product attributes")
    public void testUpdateProduct() {
        ProductDTO updateDTO = new ProductDTO(
                "Updated Name",
                "Updated description",
                PriceRange.HIGH,
                "assets/test/updated_model.glb",
                MountingType.FLOOR,
                basinCategory.getId());

        ProductDTO updated = productService.updateProduct(cheapFloorBasin.getId(), updateDTO);

        assertEquals("Updated Name", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(PriceRange.HIGH, updated.getPriceRange());
        assertEquals(MountingType.FLOOR, updated.getMountingType());
    }

    @Test
    @DisplayName("Product CRUD - Delete product")
    public void testDeleteProduct() {
        Long productId = mediumBathtub.getId();

        assertTrue(productService.getProductById(productId).isPresent());

        productService.deleteProduct(productId);

        assertFalse(productService.getProductById(productId).isPresent());
    }

    @Test
    @DisplayName("Product validation - Find by name returns correct product")
    public void testFindByName() {
        ProductDTO found = productService.findByName("Expensive Wall Basin");

        assertNotNull(found);
        assertEquals(expensiveWallBasin.getId(), found.getId());
        assertEquals("Expensive Wall Basin", found.getName());
    }

    @Test
    @DisplayName("Product filtering - Case-insensitive search works")
    public void testCaseInsensitiveSearch() {
        List<ProductDTO> lowerCaseResults = productService.searchProductsByName("basin");
        List<ProductDTO> upperCaseResults = productService.searchProductsByName("BASIN");
        List<ProductDTO> mixedCaseResults = productService.searchProductsByName("BaSiN");

        assertTrue(lowerCaseResults.size() >= 2);
        assertEquals(lowerCaseResults.size(), upperCaseResults.size());
        assertEquals(lowerCaseResults.size(), mixedCaseResults.size());
    }

    @Test
    @DisplayName("Product filtering - Partial name match works")
    public void testPartialNameMatch() {
        List<ProductDTO> wallResults = productService.searchProductsByName("Wall");
        List<ProductDTO> floorResults = productService.searchProductsByName("Floor");

        assertTrue(wallResults.size() >= 1);
        assertTrue(floorResults.size() >= 1);

        assertTrue(wallResults.stream().anyMatch(p -> p.getName().contains("Wall")));
        assertTrue(floorResults.stream().anyMatch(p -> p.getName().contains("Floor")));
    }

    @Test
    @DisplayName("Product filtering - Get products for AI selection returns all products")
    public void testGetProductsForAISelection() {
        List<ProductDTO> aiProducts = productService.getProductsForAISelection();

        assertNotNull(aiProducts);
        assertTrue(aiProducts.size() >= 3);
        assertTrue(aiProducts.stream().anyMatch(p -> p.getName().equals("Expensive Wall Basin")));
        assertTrue(aiProducts.stream().anyMatch(p -> p.getName().equals("Cheap Floor Basin")));
        assertTrue(aiProducts.stream().anyMatch(p -> p.getName().equals("Medium Bathtub")));
    }
}
