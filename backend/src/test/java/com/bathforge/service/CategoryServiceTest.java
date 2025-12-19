package com.bathforge.service;

import com.bathforge.dto.products.CategoryDTO;
import com.bathforge.service.products.CategoryService;

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
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Test
    @DisplayName("Create category successfully")
    public void testCreateCategory() {
        CategoryDTO categoryDTO = new CategoryDTO("test_category", "Test Category Description");
        CategoryDTO created = categoryService.createCategory(categoryDTO);

        assertNotNull(created.getId());
        assertEquals("test_category", created.getName());
        assertEquals("Test Category Description", created.getDescription());
    }

    @Test
    @DisplayName("Create duplicate category throws exception")
    public void testCreateDuplicateCategoryThrowsException() {
        categoryService.createCategory(new CategoryDTO("duplicate", "First category"));

        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.createCategory(new CategoryDTO("duplicate", "Second category"));
        });
    }

    @Test
    @DisplayName("Get all categories returns list")
    public void testGetAllCategories() {
        categoryService.createCategory(new CategoryDTO("category1", "Category 1"));
        categoryService.createCategory(new CategoryDTO("category2", "Category 2"));

        List<CategoryDTO> categories = categoryService.getAllCategories();

        assertTrue(categories.size() >= 2);
    }

    @Test
    @DisplayName("Get category by ID returns correct category")
    public void testGetCategoryById() {
        CategoryDTO created = categoryService.createCategory(
                new CategoryDTO("find_by_id", "Find by ID test"));

        Optional<CategoryDTO> found = categoryService.getCategoryById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("find_by_id", found.get().getName());
    }

    @Test
    @DisplayName("Get category by name returns correct category")
    public void testGetCategoryByName() {
        categoryService.createCategory(new CategoryDTO("find_by_name", "Find by name test"));

        Optional<CategoryDTO> found = categoryService.getCategoryByName("find_by_name");

        assertTrue(found.isPresent());
        assertEquals("find_by_name", found.get().getName());
    }

    @Test
    @DisplayName("Get category by name is case-insensitive")
    public void testGetCategoryByNameCaseInsensitive() {
        categoryService.createCategory(new CategoryDTO("TestCategory", "Test"));

        Optional<CategoryDTO> found = categoryService.getCategoryByName("testcategory");

        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Update category successfully")
    public void testUpdateCategory() {
        CategoryDTO created = categoryService.createCategory(
                new CategoryDTO("original", "Original description"));

        CategoryDTO updateDTO = new CategoryDTO("updated", "Updated description");
        CategoryDTO updated = categoryService.updateCategory(created.getId(), updateDTO);

        assertEquals("updated", updated.getName());
        assertEquals("Updated description", updated.getDescription());
    }

    @Test
    @DisplayName("Update non-existent category throws exception")
    public void testUpdateNonExistentCategoryThrowsException() {
        CategoryDTO updateDTO = new CategoryDTO("nonexistent", "Description");

        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.updateCategory(999999L, updateDTO);
        });
    }

    @Test
    @DisplayName("Update category to duplicate name throws exception")
    public void testUpdateToDuplicateNameThrowsException() {
        categoryService.createCategory(new CategoryDTO("existing", "Existing"));
        CategoryDTO toUpdate = categoryService.createCategory(new CategoryDTO("to_update", "To update"));

        CategoryDTO updateDTO = new CategoryDTO("existing", "Trying to use existing name");

        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.updateCategory(toUpdate.getId(), updateDTO);
        });
    }

    @Test
    @DisplayName("Delete category successfully")
    public void testDeleteCategory() {
        CategoryDTO created = categoryService.createCategory(
                new CategoryDTO("to_delete", "To delete"));

        categoryService.deleteCategory(created.getId());

        Optional<CategoryDTO> found = categoryService.getCategoryById(created.getId());
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Delete non-existent category throws exception")
    public void testDeleteNonExistentCategoryThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.deleteCategory(999999L);
        });
    }

    @Test
    @DisplayName("Check if category exists by name")
    public void testExistsByName() {
        categoryService.createCategory(new CategoryDTO("exists_test", "Exists test"));

        assertTrue(categoryService.existsByName("exists_test"));
        assertFalse(categoryService.existsByName("does_not_exist"));
    }

    @Test
    @DisplayName("Categories are ordered alphabetically")
    public void testCategoriesOrderedAlphabetically() {
        categoryService.createCategory(new CategoryDTO("zebra", "Last"));
        categoryService.createCategory(new CategoryDTO("alpha", "First"));
        categoryService.createCategory(new CategoryDTO("middle", "Middle"));

        List<CategoryDTO> categories = categoryService.getAllCategories();

        assertTrue(categories.size() >= 3);

        List<String> createdCategories = categories.stream()
                .map(CategoryDTO::getName)
                .filter(name -> name.equals("alpha") || name.equals("middle") || name.equals("zebra"))
                .toList();

        assertEquals(3, createdCategories.size());
        assertEquals("alpha", createdCategories.get(0));
        assertEquals("middle", createdCategories.get(1));
        assertEquals("zebra", createdCategories.get(2));
    }
}
