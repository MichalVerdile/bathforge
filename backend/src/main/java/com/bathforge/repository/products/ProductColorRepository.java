package com.bathforge.repository.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bathforge.model.products.Color;
import com.bathforge.model.products.Product;
import com.bathforge.model.products.ProductColor;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductColorRepository extends JpaRepository<ProductColor, Long> {

    /**
     * Find by product and color
     */
    Optional<ProductColor> findByProductAndColor(Product product, Color color);

    /**
     * Find all product colors by product
     */
    List<ProductColor> findByProduct(Product product);

    /**
     * Find all product colors by color
     */
    List<ProductColor> findByColor(Color color);

    /**
     * Find all product colors by product ID
     */
    List<ProductColor> findByProductId(Long productId);

    /**
     * Find all product colors by color ID
     */
    List<ProductColor> findByColorId(Long colorId);

    /**
     * Check if product-color combination exists
     */
    boolean existsByProductAndColor(Product product, Color color);

    /**
     * Check if product-color combination exists by IDs
     */
    boolean existsByProductIdAndColorId(Long productId, Long colorId);

    /**
     * Find product colors with product and color details
     */
    @Query("SELECT pc FROM ProductColor pc JOIN FETCH pc.product JOIN FETCH pc.color")
    List<ProductColor> findAllWithDetails();

    /**
     * Find colors available for a specific product
     */
    @Query("SELECT pc.color FROM ProductColor pc WHERE pc.product = :product")
    List<Color> findColorsByProduct(@Param("product") Product product);

    /**
     * Find products available in a specific color
     */
    @Query("SELECT pc.product FROM ProductColor pc WHERE pc.color = :color")
    List<Product> findProductsByColor(@Param("color") Color color);

    /**
     * Delete product colors by product
     */
    void deleteByProduct(Product product);

    /**
     * Delete product colors by color
     */
    void deleteByColor(Color color);
}