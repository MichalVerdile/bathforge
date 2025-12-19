package com.bathforge.controller;

import com.bathforge.dto.products.CategoryDTO;
import com.bathforge.dto.products.ColorDTO;
import com.bathforge.service.products.CategoryService;
import com.bathforge.service.products.ColorService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class ColorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ColorService colorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDTO testCategory;

    @BeforeEach
    public void setup() {
        testCategory = categoryService.createCategory(
                new CategoryDTO("test_colors_category", "Test category for colors"));
    }

    @Test
    @DisplayName("GET /api/colors returns 200 OK with list of colors")
    public void testGetAllColors() throws Exception {
        colorService.createColor(new ColorDTO("White", "#ffffff", testCategory.getId()));
        colorService.createColor(new ColorDTO("Black", "#000000", testCategory.getId()));

        mockMvc.perform(get("/api/colors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("GET /api/colors/{id} returns 200 OK when color exists")
    public void testGetColorByIdReturns200() throws Exception {
        ColorDTO color = colorService.createColor(
                new ColorDTO("Test Color", "#ff0000", testCategory.getId()));

        mockMvc.perform(get("/api/colors/" + color.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(color.getId()))
                .andExpect(jsonPath("$.name").value("Test Color"))
                .andExpect(jsonPath("$.hexCode").value("#ff0000"));
    }

    @Test
    @DisplayName("GET /api/colors/{id} returns 404 NOT FOUND when color doesn't exist")
    public void testGetColorByIdReturns404() throws Exception {
        mockMvc.perform(get("/api/colors/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/colors/category/{categoryId} returns colors for category")
    public void testGetColorsByCategoryId() throws Exception {
        colorService.createColor(new ColorDTO("Red", "#ff0000", testCategory.getId()));
        colorService.createColor(new ColorDTO("Blue", "#0000ff", testCategory.getId()));

        mockMvc.perform(get("/api/colors/category/" + testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].categoryId", everyItem(equalTo(testCategory.getId().intValue()))));
    }

    @Test
    @DisplayName("GET /api/colors/category/name/{categoryName} returns colors for category")
    public void testGetColorsByCategoryName() throws Exception {
        colorService.createColor(new ColorDTO("Green", "#00ff00", testCategory.getId()));

        mockMvc.perform(get("/api/colors/category/name/" + testCategory.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("POST /api/colors returns 201 CREATED on successful creation")
    public void testCreateColorReturns201() throws Exception {
        ColorDTO newColor = new ColorDTO("New Color", "#123456", testCategory.getId());

        mockMvc.perform(post("/api/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newColor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Color"))
                .andExpect(jsonPath("$.hexCode").value("#123456"));
    }

    @Test
    @DisplayName("POST /api/colors returns 400 BAD REQUEST for duplicate color name in category")
    public void testCreateDuplicateColorReturns400() throws Exception {
        colorService.createColor(new ColorDTO("Duplicate Color", "#aabbcc", testCategory.getId()));

        ColorDTO duplicate = new ColorDTO("Duplicate Color", "#ddeeff", testCategory.getId());

        mockMvc.perform(post("/api/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/colors returns 400 BAD REQUEST for invalid category ID")
    public void testCreateColorWithInvalidCategoryReturns400() throws Exception {
        ColorDTO invalidColor = new ColorDTO("Invalid", "#000000", 999999L);

        mockMvc.perform(post("/api/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidColor)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/colors/{id} returns 200 OK on successful update")
    public void testUpdateColorReturns200() throws Exception {
        ColorDTO color = colorService.createColor(
                new ColorDTO("Original Color", "#111111", testCategory.getId()));

        ColorDTO updateDTO = new ColorDTO("Updated Color", "#222222", testCategory.getId());

        mockMvc.perform(put("/api/colors/" + color.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Color"))
                .andExpect(jsonPath("$.hexCode").value("#222222"));
    }

    @Test
    @DisplayName("DELETE /api/colors/{id} returns 204 NO CONTENT on successful deletion")
    public void testDeleteColorReturns204() throws Exception {
        ColorDTO color = colorService.createColor(
                new ColorDTO("To Delete", "#333333", testCategory.getId()));

        mockMvc.perform(delete("/api/colors/" + color.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/colors/{id} returns 404 NOT FOUND when color doesn't exist")
    public void testDeleteNonExistentColorReturns404() throws Exception {
        mockMvc.perform(delete("/api/colors/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Response JSON schema matches ColorDTO structure")
    public void testColorDTOSchemaConsistency() throws Exception {
        ColorDTO color = colorService.createColor(
                new ColorDTO("Schema Test", "#abcdef", testCategory.getId()));

        mockMvc.perform(get("/api/colors/" + color.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.hexCode").isString())
                .andExpect(jsonPath("$.categoryId").isNumber());
    }
}
