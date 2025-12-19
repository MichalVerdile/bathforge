package com.bathforge.service.products;

import com.bathforge.dto.products.CategoryDTO;
import com.bathforge.model.products.Category;
import com.bathforge.repository.products.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing product categories.
 * Provides CRUD operations and conversion between Category entities and DTOs.
 */
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Retrieves all categories ordered alphabetically by name.
     *
     * @return list of all categories as DTOs
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a category by its unique identifier.
     *
     * @param id the category ID
     * @return an Optional containing the category DTO if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Retrieves a category by its name (case-insensitive).
     *
     * @param name the category name to search for
     * @return an Optional containing the category DTO if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> getCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .map(this::convertToDTO);
    }

    /**
     * Creates a new category.
     *
     * @param categoryDTO the category data to create
     * @return the created category as a DTO
     * @throws IllegalArgumentException if a category with the same name already
     *                                  exists
     */
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDTO.getName() + "' already exists");
        }

        Category category = convertToEntity(categoryDTO);
        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    /**
     * Updates an existing category.
     *
     * @param id          the ID of the category to update
     * @param categoryDTO the updated category data
     * @return the updated category as a DTO
     * @throws IllegalArgumentException if the category is not found or if the new
     *                                  name already exists
     */
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        if (!existingCategory.getName().equalsIgnoreCase(categoryDTO.getName()) &&
                categoryRepository.existsByNameIgnoreCase(categoryDTO.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDTO.getName() + "' already exists");
        }

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setDescription(categoryDTO.getDescription());

        Category updatedCategory = categoryRepository.save(existingCategory);
        return convertToDTO(updatedCategory);
    }

    /**
     * Deletes a category by its ID.
     *
     * @param id the ID of the category to delete
     * @throws IllegalArgumentException if the category is not found
     */
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found with ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    /**
     * Checks if a category exists with the given name (case-insensitive).
     *
     * @param name the category name to check
     * @return true if a category with this name exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    /**
     * Retrieves a category entity by its ID for internal service use.
     *
     * @param id the category ID
     * @return the category entity
     * @throws IllegalArgumentException if the category is not found
     */
    @Transactional(readOnly = true)
    public Category getCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
    }

    /**
     * Converts a Category entity to a CategoryDTO.
     *
     * @param category the category entity
     * @return the category DTO
     */
    private CategoryDTO convertToDTO(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription());
    }

    /**
     * Converts a CategoryDTO to a Category entity.
     *
     * @param categoryDTO the category DTO
     * @return the category entity
     */
    private Category convertToEntity(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        return category;
    }
}