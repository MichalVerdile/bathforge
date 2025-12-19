package com.bathforge.controller.products;

import com.bathforge.dto.products.ColorDTO;
import com.bathforge.service.products.ColorService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing product colors.
 */
@RestController
@RequestMapping("/api/colors")
@CrossOrigin(origins = "*")
public class ColorController {

    private final ColorService colorService;

    @Autowired
    public ColorController(ColorService colorService) {
        this.colorService = colorService;
    }

    /**
     * Retrieves all colors.
     *
     * @return response entity with list of all colors
     */
    @GetMapping
    public ResponseEntity<List<ColorDTO>> getAllColors() {
        List<ColorDTO> colors = colorService.getAllColors();
        return ResponseEntity.ok(colors);
    }

    /**
     * Retrieves a color by its ID.
     *
     * @param id the color ID
     * @return response entity with the color if found, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ColorDTO> getColorById(@PathVariable Long id) {
        Optional<ColorDTO> color = colorService.getColorById(id);
        return color.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves colors by category ID.
     *
     * @param categoryId the category ID
     * @return response entity with list of colors for the category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ColorDTO>> getColorsByCategoryId(@PathVariable Long categoryId) {
        List<ColorDTO> colors = colorService.getColorsByCategoryId(categoryId);
        return ResponseEntity.ok(colors);
    }

    /**
     * Retrieves colors by category name.
     *
     * @param categoryName the category name
     * @return response entity with list of colors for the category
     */
    @GetMapping("/category/name/{categoryName}")
    public ResponseEntity<List<ColorDTO>> getColorsByCategoryName(@PathVariable String categoryName) {
        List<ColorDTO> colors = colorService.getColorsByCategoryName(categoryName);
        return ResponseEntity.ok(colors);
    }

    /**
     * Creates a new color.
     *
     * @param colorDTO the color data to create
     * @return response entity with the created color and 201 status, or 400 if
     *         invalid
     */
    @PostMapping
    public ResponseEntity<ColorDTO> createColor(@Valid @RequestBody ColorDTO colorDTO) {
        try {
            ColorDTO createdColor = colorService.createColor(colorDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdColor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Updates an existing color.
     *
     * @param id       the color ID to update
     * @param colorDTO the updated color data
     * @return response entity with the updated color, or 400 if invalid
     */
    @PutMapping("/{id}")
    public ResponseEntity<ColorDTO> updateColor(@PathVariable Long id,
            @Valid @RequestBody ColorDTO colorDTO) {
        try {
            ColorDTO updatedColor = colorService.updateColor(id, colorDTO);
            return ResponseEntity.ok(updatedColor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deletes a color by its ID.
     *
     * @param id the color ID to delete
     * @return response entity with 204 status if successful, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteColor(@PathVariable Long id) {
        try {
            colorService.deleteColor(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}