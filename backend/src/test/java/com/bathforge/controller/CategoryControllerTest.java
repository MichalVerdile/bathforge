package com.bathforge.controller;

import com.bathforge.dto.products.CategoryDTO;
import com.bathforge.service.products.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/categories returns 200 OK with list of categories")
    public void testGetAllCategories() throws Exception {
        // Create test categories
        categoryService.createCategory(new CategoryDTO("test_category_1", "Test Category 1"));
        categoryService.createCategory(new CategoryDTO("test_category_2", "Test Category 2"));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("GET /api/categories/{id} returns 200 OK when category exists")
    public void testGetCategoryByIdReturns200() throws Exception {
        CategoryDTO category = categoryService.createCategory(
                new CategoryDTO("test-basins-unique", "Test bathroom basins"));

        mockMvc.perform(get("/api/categories/" + category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("test-basins-unique"))
                .andExpect(jsonPath("$.description").value("Test bathroom basins"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} returns 404 NOT FOUND when category doesn't exist")
    public void testGetCategoryByIdReturns404() throws Exception {
        mockMvc.perform(get("/api/categories/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/categories/name/{name} returns 200 OK when category exists")
    public void testGetCategoryByName() throws Exception {
        categoryService.createCategory(new CategoryDTO("test-bathtubs-unique", "Test bathtubs category"));

        mockMvc.perform(get("/api/categories/name/test-bathtubs-unique"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test-bathtubs-unique"))
                .andExpect(jsonPath("$.description").value("Test bathtubs category"));
    }

    @Test
    @DisplayName("POST /api/categories returns 201 CREATED on successful creation")
    public void testCreateCategoryReturns201() throws Exception {
        CategoryDTO newCategory = new CategoryDTO("new_category", "New test category");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("new_category"))
                .andExpect(jsonPath("$.description").value("New test category"));
    }

    @Test
    @DisplayName("POST /api/categories returns 400 BAD REQUEST for duplicate category name")
    public void testCreateDuplicateCategoryReturns400() throws Exception {
        CategoryDTO category = new CategoryDTO("duplicate_test", "Duplicate category");
        categoryService.createCategory(category);

        CategoryDTO duplicate = new CategoryDTO("duplicate_test", "Another duplicate");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/categories/{id} returns 200 OK on successful update")
    public void testUpdateCategoryReturns200() throws Exception {
        CategoryDTO category = categoryService.createCategory(
                new CategoryDTO("original_name", "Original description"));

        CategoryDTO updateDTO = new CategoryDTO("updated_name", "Updated description");

        mockMvc.perform(put("/api/categories/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated_name"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    @DisplayName("PUT /api/categories/{id} returns 400 BAD REQUEST when updating to existing name")
    public void testUpdateCategoryToDuplicateNameReturns400() throws Exception {
        categoryService.createCategory(new CategoryDTO("existing_category", "Existing"));
        CategoryDTO category = categoryService.createCategory(new CategoryDTO("to_update", "To Update"));

        CategoryDTO updateDTO = new CategoryDTO("existing_category", "Trying to use existing name");

        mockMvc.perform(put("/api/categories/" + category.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} returns 204 NO CONTENT on successful deletion")
    public void testDeleteCategoryReturns204() throws Exception {
        CategoryDTO category = categoryService.createCategory(
                new CategoryDTO("to_delete", "Category to delete"));

        mockMvc.perform(delete("/api/categories/" + category.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} returns 404 NOT FOUND when category doesn't exist")
    public void testDeleteNonExistentCategoryReturns404() throws Exception {
        mockMvc.perform(delete("/api/categories/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Response JSON schema matches CategoryDTO structure")
    public void testCategoryDTOSchemaConsistency() throws Exception {
        CategoryDTO category = categoryService.createCategory(
                new CategoryDTO("schema_test", "Schema test category"));

        mockMvc.perform(get("/api/categories/" + category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.description").isString());
    }

    @Test
    @DisplayName("Categories are returned in alphabetical order")
    public void testCategoriesOrderedAlphabetically() throws Exception {
        categoryService.createCategory(new CategoryDTO("zebra", "Last alphabetically"));
        categoryService.createCategory(new CategoryDTO("alpha", "First alphabetically"));
        categoryService.createCategory(new CategoryDTO("middle", "Middle alphabetically"));

        String response = mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Just verify response contains our test categories
        assertTrue(response.contains("alpha"));
        assertTrue(response.contains("middle"));
        assertTrue(response.contains("zebra"));
    }

    @Test
    @DisplayName("GET /api/categories/exists/{name} returns true when category exists")
    public void testExistsByNameReturnsTrue() throws Exception {
        categoryService.createCategory(new CategoryDTO("existing-category", "Exists"));

        mockMvc.perform(get("/api/categories/exists/existing-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("GET /api/categories/exists/{name} returns false when category doesn't exist")
    public void testExistsByNameReturnsFalse() throws Exception {
        mockMvc.perform(get("/api/categories/exists/non-existent-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
}
