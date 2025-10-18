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
     * Get all colors
     */
    @GetMapping
    public ResponseEntity<List<ColorDTO>> getAllColors() {
        List<ColorDTO> colors = colorService.getAllColors();
        return ResponseEntity.ok(colors);
    }

    /**
     * Get color by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ColorDTO> getColorById(@PathVariable Long id) {
        Optional<ColorDTO> color = colorService.getColorById(id);
        return color.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get colors by category ID
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ColorDTO>> getColorsByCategoryId(@PathVariable Long categoryId) {
        List<ColorDTO> colors = colorService.getColorsByCategoryId(categoryId);
        return ResponseEntity.ok(colors);
    }

    /**
     * Get colors by category name
     */
    @GetMapping("/category/name/{categoryName}")
    public ResponseEntity<List<ColorDTO>> getColorsByCategoryName(@PathVariable String categoryName) {
        List<ColorDTO> colors = colorService.getColorsByCategoryName(categoryName);
        return ResponseEntity.ok(colors);
    }

    /**
     * Create new color
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
     * Update existing color
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
     * Delete color
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