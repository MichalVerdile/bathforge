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
     * Get all products
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Optional<ProductDTO> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get products by category ID
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategoryId(@PathVariable Long categoryId) {
        List<ProductDTO> products = productService.getProductsByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by category name
     */
    @GetMapping("/category/name/{categoryName}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategoryName(@PathVariable String categoryName) {
        List<ProductDTO> products = productService.getProductsByCategoryName(categoryName);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by price range
     */
    @GetMapping("/price/{priceRange}")
    public ResponseEntity<List<ProductDTO>> getProductsByPriceRange(@PathVariable PriceRange priceRange) {
        List<ProductDTO> products = productService.getProductsByPriceRange(priceRange);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by mounting type
     */
    @GetMapping("/mounting/{mountingType}")
    public ResponseEntity<List<ProductDTO>> getProductsByMountingType(@PathVariable MountingType mountingType) {
        List<ProductDTO> products = productService.getProductsByMountingType(mountingType);
        return ResponseEntity.ok(products);
    }

    /**
     * Search products by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProductsByName(@RequestParam String name) {
        List<ProductDTO> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products with filters
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
     * Create new product
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
     * Update existing product
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
     * Delete product
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
     * Add color to product
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
     * Remove color from product
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
     * Get colors for a product
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