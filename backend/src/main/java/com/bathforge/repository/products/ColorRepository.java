package com.bathforge.repository.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bathforge.model.products.Category;
import com.bathforge.model.products.Color;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {

    /**
     * Find colors by category
     */
    List<Color> findByCategory(Category category);

    /**
     * Find colors by category ID
     */
    List<Color> findByCategoryId(Long categoryId);

    /**
     * Find color by name and category (case-insensitive)
     */
    Optional<Color> findByNameIgnoreCaseAndCategory(String name, Category category);

    /**
     * Find color by hex code and category
     */
    Optional<Color> findByHexCodeAndCategory(String hexCode, Category category);

    /**
     * Check if color exists by name and category (case-insensitive)
     */
    boolean existsByNameIgnoreCaseAndCategory(String name, Category category);

    /**
     * Find all colors ordered by category name and then by color name
     */
    @Query("SELECT c FROM Color c JOIN FETCH c.category cat ORDER BY cat.name ASC, c.name ASC")
    List<Color> findAllOrderedByCategoryAndName();

    /**
     * Find colors by category name (case-insensitive)
     */
    @Query("SELECT c FROM Color c WHERE c.category.name = :categoryName")
    List<Color> findByCategoryName(@Param("categoryName") String categoryName);
}