package com.bathforge.controller.products;

import com.bathforge.dto.products.ColorDTO;
import com.bathforge.dto.products.ProductDTO;
import com.bathforge.model.products.Product.MountingType;
import com.bathforge.model.products.Product.PriceRange;
import com.bathforge.service.products.ProductService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing products.
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Retrieves all products.
     *
     * @return response entity with list of all products
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the product ID
     * @return response entity with the product if found, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Optional<ProductDTO> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves products by category ID.
     *
     * @param categoryId the category ID
     * @return response entity with list of products in the category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategoryId(@PathVariable Long categoryId) {
        List<ProductDTO> products = productService.getProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves products by category name.
     *
     * @param categoryName the category name
     * @return response entity with list of products in the category
     */
    @GetMapping("/category/name/{categoryName}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategoryName(@PathVariable String categoryName) {
        List<ProductDTO> products = productService.getProductsByCategoryName(categoryName);
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves products by price range.
     *
     * @param priceRange the price range filter
     * @return response entity with list of products in the price range
     */
    @GetMapping("/price/{priceRange}")
    public ResponseEntity<List<ProductDTO>> getProductsByPriceRange(@PathVariable PriceRange priceRange) {
        List<ProductDTO> products = productService.getProductsByPriceRange(priceRange);
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves products by mounting type.
     *
     * @param mountingType the mounting type filter
     * @return response entity with list of products with the mounting type
     */
    @GetMapping("/mounting/{mountingType}")
    public ResponseEntity<List<ProductDTO>> getProductsByMountingType(@PathVariable MountingType mountingType) {
        List<ProductDTO> products = productService.getProductsByMountingType(mountingType);
        return ResponseEntity.ok(products);
    }

    /**
     * Searches products by name.
     *
     * @param name the search term for product name
     * @return response entity with list of matching products
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProductsByName(@RequestParam String name) {
        List<ProductDTO> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves products with multiple filters.
     *
     * @param categoryId   the category ID filter (optional)
     * @param priceRange   the price range filter (optional)
     * @param mountingType the mounting type filter (optional)
     * @return response entity with list of products matching the filters
     */
    @GetMapping("/filter")
    public ResponseEntity<List<ProductDTO>> getProductsWithFilters(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) PriceRange priceRange,
            @RequestParam(required = false) MountingType mountingType) {
        List<ProductDTO> products = productService.getProductsWithFilters(categoryId, priceRange, mountingType);
        return ResponseEntity.ok(products);
    }

    /**
     * Creates a new product.
     *
     * @param productDTO the product data to create
     * @return response entity with the created product and 201 status, or 400 if
     *         invalid
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO createdProduct = productService.createProduct(productDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Updates an existing product.
     *
     * @param id         the product ID to update
     * @param productDTO the updated product data
     * @return response entity with the updated product, or 400 if invalid
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id the product ID to delete
     * @return response entity with 204 status if successful, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Adds a color to a product.
     *
     * @param productId the product ID
     * @param colorId   the color ID to add
     * @return response entity with 200 status if successful, or 400 if invalid
     */
    @PostMapping("/{productId}/colors/{colorId}")
    public ResponseEntity<Void> addColorToProduct(@PathVariable Long productId, @PathVariable Long colorId) {
        try {
            productService.addColorToProduct(productId, colorId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Removes a color from a product.
     *
     * @param productId the product ID
     * @param colorId   the color ID to remove
     * @return response entity with 200 status if successful, or 400 if invalid
     */
    @DeleteMapping("/{productId}/colors/{colorId}")
    public ResponseEntity<Void> removeColorFromProduct(@PathVariable Long productId, @PathVariable Long colorId) {
        try {
            productService.removeColorFromProduct(productId, colorId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Retrieves colors available for a product.
     *
     * @param productId the product ID
     * @return response entity with list of colors for the product, or 404 if
     *         product not found
     */
    @GetMapping("/{productId}/colors")
    public ResponseEntity<List<ColorDTO>> getColorsForProduct(@PathVariable Long productId) {
        try {
            List<ColorDTO> colors = productService.getColorsForProduct(productId);
            return ResponseEntity.ok(colors);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}