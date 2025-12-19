package com.bathforge.repository.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bathforge.model.products.Category;
import com.bathforge.model.products.Product;
import com.bathforge.model.products.Product.MountingType;
import com.bathforge.model.products.Product.PriceRange;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

        /**
         * Find products by category
         */
        List<Product> findByCategory(Category category);

        /**
         * Find products by category ID
         */
        List<Product> findByCategoryId(Long categoryId);

        /**
         * Find products by price range
         */
        List<Product> findByPriceRange(PriceRange priceRange);

        /**
         * Find products by mounting type
         */
        List<Product> findByMountingType(MountingType mountingType);

        /**
         * Find products by category and price range
         */
        List<Product> findByCategoryAndPriceRange(Category category, PriceRange priceRange);

        /**
         * Find products by category and mounting type
         */
        List<Product> findByCategoryAndMountingType(Category category, MountingType mountingType);

        /**
         * Find product by name (case-insensitive)
         */
        Optional<Product> findByNameIgnoreCase(String name);

        /**
         * Search products by name containing text (case-insensitive)
         */
        List<Product> findByNameContainingIgnoreCase(String name);

        /**
         * Find all products with their colors
         */
        @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.productColors pc LEFT JOIN FETCH pc.color")
        List<Product> findAllWithColors();

        /**
         * Find products by category with their colors
         */
        @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.productColors pc LEFT JOIN FETCH pc.color WHERE p.category = :category")
        List<Product> findByCategoryWithColors(@Param("category") Category category);

        /**
         * Find products by category name (case-insensitive)
         */
        @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName")
        List<Product> findByCategoryName(@Param("categoryName") String categoryName);

        /**
         * Find products with filters
         */
        @Query("SELECT p FROM Product p WHERE " +
                        "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
                        "(:priceRange IS NULL OR p.priceRange = :priceRange) AND " +
                        "(:mountingType IS NULL OR p.mountingType = :mountingType)")
        List<Product> findWithFilters(@Param("categoryId") Long categoryId,
                        @Param("priceRange") PriceRange priceRange,
                        @Param("mountingType") MountingType mountingType);

        /**
         * Check if product exists by name and category
         */
        boolean existsByNameAndCategoryId(String name, Long categoryId);

        /**
         * Count products by category ID
         */
        long countByCategoryId(Long categoryId);

        /**
         * Find products for AI selection
         * Returns: Products with non-empty descriptions (for bathroom fixtures) + ALL
         * covering products
         */
        @Query("SELECT p FROM Product p WHERE " +
                        "(p.description IS NOT NULL AND p.description != '' AND p.category.name != 'coverings') " +
                        "OR (p.category.name = 'coverings')")
        List<Product> findProductsForAISelection();
}