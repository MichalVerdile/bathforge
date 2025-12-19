package com.bathforge.service.products;

import com.bathforge.dto.products.ColorDTO;
import com.bathforge.model.products.Category;
import com.bathforge.model.products.Color;
import com.bathforge.repository.products.ColorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing product colors.
 * Provides CRUD operations for colors and their associations with categories.
 */
@Service
@Transactional
public class ColorService {

    private final ColorRepository colorRepository;
    private final CategoryService categoryService;

    @Autowired
    public ColorService(ColorRepository colorRepository, CategoryService categoryService) {
        this.colorRepository = colorRepository;
        this.categoryService = categoryService;
    }

    /**
     * Retrieves all colors ordered by category and name.
     *
     * @return list of all colors as DTOs
     */
    @Transactional(readOnly = true)
    public List<ColorDTO> getAllColors() {
        return colorRepository.findAllOrderedByCategoryAndName()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a color by its unique identifier.
     *
     * @param id the color ID
     * @return an Optional containing the color DTO if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<ColorDTO> getColorById(Long id) {
        return colorRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Retrieves all colors associated with a specific category.
     *
     * @param categoryId the category ID
     * @return list of colors belonging to the category
     */
    @Transactional(readOnly = true)
    public List<ColorDTO> getColorsByCategoryId(Long categoryId) {
        return colorRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all colors associated with a category by category name.
     *
     * @param categoryName the category name
     * @return list of colors belonging to the category
     */
    @Transactional(readOnly = true)
    public List<ColorDTO> getColorsByCategoryName(String categoryName) {
        return colorRepository.findByCategoryName(categoryName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new color within a specific category.
     *
     * @param colorDTO the color data to create
     * @return the created color as a DTO
     * @throws IllegalArgumentException if a color with the same name already exists
     *                                  in the category
     */
    public ColorDTO createColor(ColorDTO colorDTO) {
        Category category = categoryService.getCategoryEntityById(colorDTO.getCategoryId());

        if (colorRepository.existsByNameIgnoreCaseAndCategory(colorDTO.getName(), category)) {
            throw new IllegalArgumentException("Color with name '" + colorDTO.getName() +
                    "' already exists in category '" + category.getName() + "'");
        }

        Color color = convertToEntity(colorDTO, category);
        Color savedColor = colorRepository.save(color);
        return convertToDTO(savedColor);
    }

    /**
     * Updates an existing color.
     *
     * @param id       the ID of the color to update
     * @param colorDTO the updated color data
     * @return the updated color as a DTO
     * @throws IllegalArgumentException if the color is not found or if the new name
     *                                  already exists in the category
     */
    public ColorDTO updateColor(Long id, ColorDTO colorDTO) {
        Color existingColor = colorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Color not found with ID: " + id));

        Category category = categoryService.getCategoryEntityById(colorDTO.getCategoryId());

        // Check if name is being changed and if new name already exists in the category
        if (!existingColor.getName().equalsIgnoreCase(colorDTO.getName()) &&
                colorRepository.existsByNameIgnoreCaseAndCategory(colorDTO.getName(), category)) {
            throw new IllegalArgumentException("Color with name '" + colorDTO.getName() +
                    "' already exists in category '" + category.getName() + "'");
        }

        existingColor.setName(colorDTO.getName());
        existingColor.setHexCode(colorDTO.getHexCode());
        existingColor.setCategory(category);

        Color updatedColor = colorRepository.save(existingColor);
        return convertToDTO(updatedColor);
    }

    /**
     * Deletes a color by its ID.
     *
     * @param id the ID of the color to delete
     * @throws IllegalArgumentException if the color is not found
     */
    public void deleteColor(Long id) {
        if (!colorRepository.existsById(id)) {
            throw new IllegalArgumentException("Color not found with ID: " + id);
        }
        colorRepository.deleteById(id);
    }

    /**
     * Retrieves a color entity by its ID for internal service use.
     *
     * @param id the color ID
     * @return the color entity
     * @throws IllegalArgumentException if the color is not found
     */
    @Transactional(readOnly = true)
    public Color getColorEntityById(Long id) {
        return colorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Color not found with ID: " + id));
    }

    /**
     * Converts a Color entity to a ColorDTO.
     *
     * @param color the color entity
     * @return the color DTO
     */
    private ColorDTO convertToDTO(Color color) {
        return new ColorDTO(
                color.getId(),
                color.getName(),
                color.getHexCode(),
                color.getCategory().getId(),
                color.getCategory().getName());
    }

    /**
     * Converts a ColorDTO to a Color entity.
     *
     * @param colorDTO the color DTO
     * @param category the category to associate the color with
     * @return the color entity
     */
    private Color convertToEntity(ColorDTO colorDTO, Category category) {
        Color color = new Color();
        color.setName(colorDTO.getName());
        color.setHexCode(colorDTO.getHexCode());
        color.setCategory(category);
        return color;
    }
}