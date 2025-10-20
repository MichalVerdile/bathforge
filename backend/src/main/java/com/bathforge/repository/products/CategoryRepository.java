package com.bathforge.repository.products;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bathforge.model.products.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find category by name (case-insensitive)
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Check if category exists by name (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find all categories ordered by name
     */
    List<Category> findAllByOrderByNameAsc();

    /**
     * Find categories with their associated colors
     */
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.colors")
    List<Category> findAllWithColors();

    /**
     * Find categories with their associated products
     */
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.products")
    List<Category> findAllWithProducts();
}