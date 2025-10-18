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
     * Get all colors
     */
    @Transactional(readOnly = true)
    public List<ColorDTO> getAllColors() {
        return colorRepository.findAllOrderedByCategoryAndName()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get color by ID
     */
    @Transactional(readOnly = true)
    public Optional<ColorDTO> getColorById(Long id) {
        return colorRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Get colors by category ID
     */
    @Transactional(readOnly = true)
    public List<ColorDTO> getColorsByCategoryId(Long categoryId) {
        return colorRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get colors by category name
     */
    @Transactional(readOnly = true)
    public List<ColorDTO> getColorsByCategoryName(String categoryName) {
        return colorRepository.findByCategoryName(categoryName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create new color
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
     * Update existing color
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
     * Delete color
     */
    public void deleteColor(Long id) {
        if (!colorRepository.existsById(id)) {
            throw new IllegalArgumentException("Color not found with ID: " + id);
        }
        colorRepository.deleteById(id);
    }

    /**
     * Get color entity by ID (for internal use)
     */
    @Transactional(readOnly = true)
    public Color getColorEntityById(Long id) {
        return colorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Color not found with ID: " + id));
    }

    // Helper methods for conversion
    private ColorDTO convertToDTO(Color color) {
        return new ColorDTO(
                color.getId(),
                color.getName(),
                color.getHexCode(),
                color.getCategory().getId(),
                color.getCategory().getName());
    }

    private Color convertToEntity(ColorDTO colorDTO, Category category) {
        Color color = new Color();
        color.setName(colorDTO.getName());
        color.setHexCode(colorDTO.getHexCode());
        color.setCategory(category);
        return color;
    }
}