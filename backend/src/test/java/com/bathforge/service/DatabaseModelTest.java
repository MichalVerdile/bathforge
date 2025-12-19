package com.bathforge.service;

import com.bathforge.dto.products.CategoryDTO;
import com.bathforge.dto.products.ColorDTO;
import com.bathforge.dto.products.ProductDTO;
import com.bathforge.model.products.Product.MountingType;
import com.bathforge.model.products.Product.PriceRange;
import com.bathforge.service.products.CategoryService;
import com.bathforge.service.products.ColorService;
import com.bathforge.service.products.ProductService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class DatabaseModelTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ColorService colorService;

    @Autowired
    private ProductService productService;

    @Test
    public void testCreateCategoryAndColors() {
        CategoryDTO categoryDTO = new CategoryDTO("test_category", "Test category for unit testing");
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);

        assertNotNull(createdCategory.getId());
        assertEquals("test_category", createdCategory.getName());
        assertEquals("Test category for unit testing", createdCategory.getDescription());

        ColorDTO colorDTO1 = new ColorDTO("Test White", "#ffffff", createdCategory.getId());
        ColorDTO createdColor1 = colorService.createColor(colorDTO1);

        assertNotNull(createdColor1.getId());
        assertEquals("Test White", createdColor1.getName());
        assertEquals("#ffffff", createdColor1.getHexCode());
        assertEquals(createdCategory.getId(), createdColor1.getCategoryId());

        ColorDTO colorDTO2 = new ColorDTO("Test Black", "#000000", createdCategory.getId());
        ColorDTO createdColor2 = colorService.createColor(colorDTO2);

        assertNotNull(createdColor2.getId());
        assertEquals("Test Black", createdColor2.getName());
        assertEquals("#000000", createdColor2.getHexCode());
    }

    @Test
    public void testCreateProductWithColors() {
        CategoryDTO categoryDTO = new CategoryDTO("test_basins", "Test basins category");
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);

        ColorDTO colorDTO1 = new ColorDTO("White", "#ffffff", createdCategory.getId());
        ColorDTO createdColor1 = colorService.createColor(colorDTO1);

        ColorDTO colorDTO2 = new ColorDTO("Black", "#000000", createdCategory.getId());
        ColorDTO createdColor2 = colorService.createColor(colorDTO2);

        ProductDTO productDTO = new ProductDTO(
                "Test Basin Model",
                "A test basin for unit testing",
                PriceRange.MEDIUM,
                "assets/test/test_basin.glb",
                MountingType.WALL,
                createdCategory.getId());

        ProductDTO createdProduct = productService.createProduct(productDTO);

        assertNotNull(createdProduct.getId());
        assertEquals("Test Basin Model", createdProduct.getName());
        assertEquals(PriceRange.MEDIUM, createdProduct.getPriceRange());
        assertEquals(MountingType.WALL, createdProduct.getMountingType());
        assertEquals(createdCategory.getId(), createdProduct.getCategoryId());

        productService.addColorToProduct(createdProduct.getId(), createdColor1.getId());
        productService.addColorToProduct(createdProduct.getId(), createdColor2.getId());

        var productColors = productService.getColorsForProduct(createdProduct.getId());
        assertEquals(2, productColors.size());

        var retrievedProduct = productService.getProductById(createdProduct.getId());
        assertTrue(retrievedProduct.isPresent());
        assertEquals(2, retrievedProduct.get().getAvailableColors().size());
    }

    @Test
    public void testDataValidation() {
        CategoryDTO validCategory = new CategoryDTO("duplicate_test", "Valid category");
        CategoryDTO createdCategory = categoryService.createCategory(validCategory);

        CategoryDTO duplicateCategory = new CategoryDTO("duplicate_test", "Another category with same name");

        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.createCategory(duplicateCategory);
        });

        ColorDTO validColor = new ColorDTO("Valid Color", "#123456", createdCategory.getId());
        colorService.createColor(validColor);

        ColorDTO duplicateColor = new ColorDTO("Valid Color", "#654321", createdCategory.getId());

        assertThrows(IllegalArgumentException.class, () -> {
            colorService.createColor(duplicateColor);
        });
    }

    @Test
    public void testRepositoryQueries() {
        CategoryDTO categoryDTO = new CategoryDTO("query_test", "Query test category");
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);

        var foundCategory = categoryService.getCategoryByName("query_test");
        assertTrue(foundCategory.isPresent());
        assertEquals(createdCategory.getId(), foundCategory.get().getId());

        assertTrue(categoryService.existsByName("query_test"));
        assertFalse(categoryService.existsByName("non_existent_category"));

        ColorDTO colorDTO = new ColorDTO("Query Color", "#123456", createdCategory.getId());
        ColorDTO createdColor = colorService.createColor(colorDTO);

        var colorsByCategory = colorService.getColorsByCategoryId(createdCategory.getId());
        assertEquals(1, colorsByCategory.size());
        assertEquals(createdColor.getId(), colorsByCategory.get(0).getId());

        var colorsByCategoryName = colorService.getColorsByCategoryName("query_test");
        assertEquals(1, colorsByCategoryName.size());
    }
}