package com.bathforge.service;

import com.bathforge.dto.products.CategoryDTO;
import com.bathforge.dto.products.ColorDTO;
import com.bathforge.service.products.CategoryService;
import com.bathforge.service.products.ColorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class ColorServiceTest {

    @Autowired
    private ColorService colorService;

    @Autowired
    private CategoryService categoryService;

    private CategoryDTO testCategory;

    @BeforeEach
    public void setup() {
        testCategory = categoryService.createCategory(
                new CategoryDTO("test_category", "Test category for colors"));
    }

    @Test
    @DisplayName("Create color successfully")
    public void testCreateColor() {
        ColorDTO colorDTO = new ColorDTO("Test Color", "#ff0000", testCategory.getId());
        ColorDTO created = colorService.createColor(colorDTO);

        assertNotNull(created.getId());
        assertEquals("Test Color", created.getName());
        assertEquals("#ff0000", created.getHexCode());
        assertEquals(testCategory.getId(), created.getCategoryId());
    }

    @Test
    @DisplayName("Create duplicate color in same category throws exception")
    public void testCreateDuplicateColorThrowsException() {
        colorService.createColor(new ColorDTO("Duplicate", "#000000", testCategory.getId()));

        assertThrows(IllegalArgumentException.class, () -> {
            colorService.createColor(new ColorDTO("Duplicate", "#111111", testCategory.getId()));
        });
    }

    @Test
    @DisplayName("Create color with invalid category throws exception")
    public void testCreateColorWithInvalidCategoryThrowsException() {
        ColorDTO colorDTO = new ColorDTO("Invalid", "#000000", 999999L);

        assertThrows(IllegalArgumentException.class, () -> {
            colorService.createColor(colorDTO);
        });
    }

    @Test
    @DisplayName("Get all colors returns list")
    public void testGetAllColors() {
        colorService.createColor(new ColorDTO("Color1", "#111111", testCategory.getId()));
        colorService.createColor(new ColorDTO("Color2", "#222222", testCategory.getId()));

        List<ColorDTO> colors = colorService.getAllColors();

        assertTrue(colors.size() >= 2);
    }

    @Test
    @DisplayName("Get color by ID returns correct color")
    public void testGetColorById() {
        ColorDTO created = colorService.createColor(
                new ColorDTO("Find By ID", "#abcdef", testCategory.getId()));

        Optional<ColorDTO> found = colorService.getColorById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("Find By ID", found.get().getName());
    }

    @Test
    @DisplayName("Get colors by category ID returns correct colors")
    public void testGetColorsByCategoryId() {
        colorService.createColor(new ColorDTO("Red", "#ff0000", testCategory.getId()));
        colorService.createColor(new ColorDTO("Blue", "#0000ff", testCategory.getId()));

        // Create another category with colors
        CategoryDTO otherCategory = categoryService.createCategory(
                new CategoryDTO("other_category", "Other category"));
        colorService.createColor(new ColorDTO("Green", "#00ff00", otherCategory.getId()));

        List<ColorDTO> categoryColors = colorService.getColorsByCategoryId(testCategory.getId());

        assertEquals(2, categoryColors.size());
        assertTrue(categoryColors.stream().allMatch(c -> c.getCategoryId().equals(testCategory.getId())));
    }

    @Test
    @DisplayName("Get colors by category name returns correct colors")
    public void testGetColorsByCategoryName() {
        colorService.createColor(new ColorDTO("Yellow", "#ffff00", testCategory.getId()));

        List<ColorDTO> colors = colorService.getColorsByCategoryName(testCategory.getName());

        assertTrue(colors.size() >= 1);
        assertTrue(colors.stream().anyMatch(c -> c.getName().equals("Yellow")));
    }

    @Test
    @DisplayName("Update color successfully")
    public void testUpdateColor() {
        ColorDTO created = colorService.createColor(
                new ColorDTO("Original", "#111111", testCategory.getId()));

        ColorDTO updateDTO = new ColorDTO("Updated", "#222222", testCategory.getId());
        ColorDTO updated = colorService.updateColor(created.getId(), updateDTO);

        assertEquals("Updated", updated.getName());
        assertEquals("#222222", updated.getHexCode());
    }

    @Test
    @DisplayName("Update non-existent color throws exception")
    public void testUpdateNonExistentColorThrowsException() {
        ColorDTO updateDTO = new ColorDTO("Nonexistent", "#000000", testCategory.getId());

        assertThrows(IllegalArgumentException.class, () -> {
            colorService.updateColor(999999L, updateDTO);
        });
    }

    @Test
    @DisplayName("Update color to duplicate name in same category throws exception")
    public void testUpdateToDuplicateNameThrowsException() {
        colorService.createColor(new ColorDTO("Existing", "#111111", testCategory.getId()));
        ColorDTO toUpdate = colorService.createColor(new ColorDTO("ToUpdate", "#222222", testCategory.getId()));

        ColorDTO updateDTO = new ColorDTO("Existing", "#333333", testCategory.getId());

        assertThrows(IllegalArgumentException.class, () -> {
            colorService.updateColor(toUpdate.getId(), updateDTO);
        });
    }

    @Test
    @DisplayName("Delete color successfully")
    public void testDeleteColor() {
        ColorDTO created = colorService.createColor(
                new ColorDTO("ToDelete", "#abcdef", testCategory.getId()));

        colorService.deleteColor(created.getId());

        Optional<ColorDTO> found = colorService.getColorById(created.getId());
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Delete non-existent color throws exception")
    public void testDeleteNonExistentColorThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            colorService.deleteColor(999999L);
        });
    }

    @Test
    @DisplayName("Same color name allowed in different categories")
    public void testSameColorNameInDifferentCategories() {
        CategoryDTO category2 = categoryService.createCategory(
                new CategoryDTO("category2", "Second category"));

        ColorDTO color1 = colorService.createColor(
                new ColorDTO("SameName", "#111111", testCategory.getId()));
        ColorDTO color2 = colorService.createColor(
                new ColorDTO("SameName", "#222222", category2.getId()));

        assertNotEquals(color1.getId(), color2.getId());
        assertEquals("SameName", color1.getName());
        assertEquals("SameName", color2.getName());
    }

    @Test
    @DisplayName("Hex code validation accepts valid hex codes")
    public void testValidHexCodes() {
        assertDoesNotThrow(() -> {
            colorService.createColor(new ColorDTO("Color1", "#000000", testCategory.getId()));
            colorService.createColor(new ColorDTO("Color2", "#FFFFFF", testCategory.getId()));
            colorService.createColor(new ColorDTO("Color3", "#abc123", testCategory.getId()));
        });
    }
}
