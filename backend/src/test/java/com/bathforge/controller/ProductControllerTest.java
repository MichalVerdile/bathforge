package com.bathforge.controller;

import com.bathforge.dto.products.ProductDTO;
import com.bathforge.model.products.Product.MountingType;
import com.bathforge.model.products.Product.PriceRange;
import com.bathforge.service.products.ProductService;
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

/**
 * REST API tests for ProductController using MockMvc
 * Tests HTTP status codes, validation handling, and DTO consistency
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testCategoryId;

    @BeforeEach
    public void setup() {
        testCategoryId = 1L;
    }

    @Test
    @DisplayName("REST API - GET /api/products returns 200 OK")
    public void testGetAllProductsReturns200() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("REST API - GET /api/products/{id} returns 200 OK when product exists")
    public void testGetProductByIdReturns200() throws Exception {
        ProductDTO product = new ProductDTO(
                "Test Product",
                "Test description",
                PriceRange.MEDIUM,
                "assets/test/product.glb",
                MountingType.WALL,
                testCategoryId);
        ProductDTO created = productService.createProduct(product);

        mockMvc.perform(get("/api/products/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @DisplayName("REST API - GET /api/products/{id} returns 404 NOT FOUND when product doesn't exist")
    public void testGetProductByIdReturns404() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("REST API - POST /api/products returns 201 CREATED on successful creation")
    public void testCreateProductReturns201() throws Exception {
        ProductDTO newProduct = new ProductDTO(
                "New Product",
                "New product description",
                PriceRange.HIGH,
                "assets/test/new.glb",
                MountingType.FLOOR,
                testCategoryId);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("REST API - PUT /api/products/{id} returns 200 OK on successful update")
    public void testUpdateProductReturns200() throws Exception {
        ProductDTO product = new ProductDTO(
                "Original Product",
                "Original description",
                PriceRange.LOW,
                "assets/test/original.glb",
                MountingType.WALL,
                testCategoryId);
        ProductDTO created = productService.createProduct(product);

        ProductDTO updateDTO = new ProductDTO(
                "Updated Product",
                "Updated description",
                PriceRange.HIGH,
                "assets/test/updated.glb",
                MountingType.FLOOR,
                testCategoryId);

        mockMvc.perform(put("/api/products/" + created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.priceRange").value("HIGH"));
    }

    @Test
    @DisplayName("REST API - DELETE /api/products/{id} returns 204 NO CONTENT on successful deletion")
    public void testDeleteProductReturns204() throws Exception {
        ProductDTO product = new ProductDTO(
                "Product to Delete",
                "Will be deleted",
                PriceRange.MEDIUM,
                "assets/test/delete.glb",
                MountingType.WALL,
                testCategoryId);
        ProductDTO created = productService.createProduct(product);

        mockMvc.perform(delete("/api/products/" + created.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("REST API - DELETE /api/products/{id} returns 404 NOT FOUND when product doesn't exist")
    public void testDeleteNonExistentProductReturns404() throws Exception {
        mockMvc.perform(delete("/api/products/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("REST API - POST with invalid data returns 400 BAD REQUEST")
    public void testCreateProductWithInvalidDataReturns400() throws Exception {
        String invalidJson = "{\"priceRange\": \"MEDIUM\", \"mountingType\": \"WALL\"}";

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("REST API - POST with invalid category ID returns 400 BAD REQUEST")
    public void testCreateProductWithInvalidCategoryReturns400() throws Exception {
        ProductDTO invalidProduct = new ProductDTO(
                "Invalid Product",
                "Has invalid category",
                PriceRange.MEDIUM,
                "assets/test/invalid.glb",
                MountingType.WALL,
                999999L
        );

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("REST API - PUT with invalid data returns 400 BAD REQUEST")
    public void testUpdateProductWithInvalidDataReturns400() throws Exception {
        ProductDTO product = new ProductDTO(
                "Valid Product",
                "Valid description",
                PriceRange.MEDIUM,
                "assets/test/valid.glb",
                MountingType.WALL,
                testCategoryId);
        ProductDTO created = productService.createProduct(product);

        ProductDTO invalidUpdate = new ProductDTO(
                "Updated Name",
                "Updated description",
                PriceRange.HIGH,
                "assets/test/updated.glb",
                MountingType.FLOOR,
                999999L
        );

        mockMvc.perform(put("/api/products/" + created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("REST API - Response JSON schema matches ProductDTO structure")
    public void testProductDTOSchemaConsistency() throws Exception {
        ProductDTO product = new ProductDTO(
                "Schema Test Product",
                "Testing JSON schema",
                PriceRange.MEDIUM,
                "assets/test/schema.glb",
                MountingType.WALL,
                testCategoryId);
        ProductDTO created = productService.createProduct(product);

        mockMvc.perform(get("/api/products/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.priceRange").exists())
                .andExpect(jsonPath("$.modelPath").exists())
                .andExpect(jsonPath("$.mountingType").exists())
                .andExpect(jsonPath("$.categoryId").exists())
                .andExpect(jsonPath("$.availableColors").exists());
    }

    @Test
    @DisplayName("REST API - Data types in response match expected types")
    public void testResponseDataTypes() throws Exception {
        ProductDTO product = new ProductDTO(
                "Type Test Product",
                "Testing data types",
                PriceRange.LOW,
                "assets/test/types.glb",
                MountingType.FLOOR,
                testCategoryId);
        ProductDTO created = productService.createProduct(product);

        mockMvc.perform(get("/api/products/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.description").isString())
                .andExpect(jsonPath("$.priceRange").isString())
                .andExpect(jsonPath("$.mountingType").isString())
                .andExpect(jsonPath("$.categoryId").isNumber())
                .andExpect(jsonPath("$.availableColors").isArray());
    }

    @Test
    @DisplayName("REST API - List endpoint returns array of products")
    public void testGetAllProductsReturnsArray() throws Exception {
        for (int i = 1; i <= 3; i++) {
            ProductDTO product = new ProductDTO(
                    "List Test Product " + i,
                    "Product " + i,
                    PriceRange.MEDIUM,
                    "assets/test/list_" + i + ".glb",
                    MountingType.WALL,
                    testCategoryId);
            productService.createProduct(product);
        }

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3)));
    }

    @Test
    @DisplayName("REST API - Filter by category returns correct products")
    public void testFilterByCategory() throws Exception {
        ProductDTO product1 = new ProductDTO(
                "Category Filter Test 1",
                "Test 1",
                PriceRange.MEDIUM,
                "assets/test/cat1.glb",
                MountingType.WALL,
                testCategoryId);
        productService.createProduct(product1);

        mockMvc.perform(get("/api/products/filter")
                .param("categoryId", testCategoryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].categoryId", everyItem(equalTo(testCategoryId.intValue()))));
    }

    @Test
    @DisplayName("REST API - Filter by price range returns correct products")
    public void testFilterByPriceRange() throws Exception {
        ProductDTO cheap = new ProductDTO(
                "Cheap Product",
                "LOW",
                PriceRange.LOW,
                "assets/test/cheap.glb",
                MountingType.WALL,
                testCategoryId);
        productService.createProduct(cheap);

        mockMvc.perform(get("/api/products/filter")
                .param("priceRange", "LOW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].priceRange", everyItem(equalTo("LOW"))));
    }

    @Test
    @DisplayName("REST API - Filter by mounting type returns correct products")
    public void testFilterByMountingType() throws Exception {
        ProductDTO wallProduct = new ProductDTO(
                "Wall Product",
                "Wall mounted",
                PriceRange.MEDIUM,
                "assets/test/wall.glb",
                MountingType.WALL,
                testCategoryId);
        productService.createProduct(wallProduct);

        mockMvc.perform(get("/api/products/filter")
                .param("mountingType", "WALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].mountingType", everyItem(equalTo("WALL"))));
    }

    @Test
    @DisplayName("REST API - Search by name returns matching products")
    public void testSearchByName() throws Exception {
        ProductDTO searchProduct = new ProductDTO(
                "Searchable Basin Product",
                "Easy to find",
                PriceRange.MEDIUM,
                "assets/test/search.glb",
                MountingType.WALL,
                testCategoryId);
        productService.createProduct(searchProduct);

        mockMvc.perform(get("/api/products/search")
                .param("name", "Basin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].name", hasItem(containsString("Basin"))));
    }

    @Test
    @DisplayName("REST API - Combined filters return correctly filtered products")
    public void testCombinedFilters() throws Exception {
        ProductDTO specific = new ProductDTO(
                "Specific Wall Basin",
                "Expensive wall basin",
                PriceRange.HIGH,
                "assets/test/specific.glb",
                MountingType.WALL,
                testCategoryId);
        productService.createProduct(specific);

        mockMvc.perform(get("/api/products/filter")
                .param("categoryId", testCategoryId.toString())
                .param("priceRange", "HIGH")
                .param("mountingType", "WALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("REST API - Empty filter results return empty array with 200 OK")
    public void testEmptyFilterResults() throws Exception {
        mockMvc.perform(get("/api/products/search")
                .param("name", "NonExistentProductNameXYZ123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("REST API - Add color to product returns 200 OK")
    public void testAddColorToProduct() throws Exception {
        ProductDTO product = new ProductDTO(
                "Color Test Product",
                "Test colors",
                PriceRange.MEDIUM,
                "assets/test/color.glb",
                MountingType.WALL,
                testCategoryId);
        ProductDTO created = productService.createProduct(product);

        Long colorId = 1L;

        mockMvc.perform(post("/api/products/" + created.getId() + "/colors/" + colorId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("REST API - Get colors for product returns array")
    public void testGetColorsForProduct() throws Exception {
        ProductDTO product = new ProductDTO(
                "Product With Colors",
                "Has colors",
                PriceRange.MEDIUM,
                "assets/test/colors.glb",
                MountingType.WALL,
                testCategoryId);
        ProductDTO created = productService.createProduct(product);

        mockMvc.perform(get("/api/products/" + created.getId() + "/colors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("REST API - Remove color from product returns 200 OK")
    public void testRemoveColorFromProduct() throws Exception {
        ProductDTO product = new ProductDTO(
                "Product With Color To Remove",
                "Has color to remove",
                PriceRange.MEDIUM,
                "assets/test/remove-color.glb",
                MountingType.WALL,
                testCategoryId);
        ProductDTO created = productService.createProduct(product);

        Long colorId = 1L;

        mockMvc.perform(post("/api/products/" + created.getId() + "/colors/" + colorId))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/products/" + created.getId() + "/colors/" + colorId))
                .andExpect(status().isOk());
    }
}
