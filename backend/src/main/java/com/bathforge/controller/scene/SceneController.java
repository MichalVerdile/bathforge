package com.bathforge.controller.scene;

import com.bathforge.dto.scene.*;
import com.bathforge.service.scene.SceneService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scenes")
@CrossOrigin(origins = "*")
public class SceneController {

    private final SceneService sceneService;

    @Autowired
    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;
    }

    /**
     * Get all scenes
     */
    @GetMapping
    public ResponseEntity<List<SceneDTO>> getAllScenes() {
        List<SceneDTO> scenes = sceneService.getAllScenes();
        return ResponseEntity.ok(scenes);
    }

    /**
     * Get scene by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SceneDTO> getSceneById(@PathVariable Long id) {
        Optional<SceneDTO> scene = sceneService.getSceneById(id);
        return scene.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get scenes by user
     */
    @GetMapping("/user/{user}")
    public ResponseEntity<List<SceneDTO>> getScenesByUser(@PathVariable String user) {
        List<SceneDTO> scenes = sceneService.getScenesByUser(user);
        return ResponseEntity.ok(scenes);
    }

    /**
     * Get public scenes
     */
    @GetMapping("/public")
    public ResponseEntity<List<SceneDTO>> getPublicScenes() {
        List<SceneDTO> scenes = sceneService.getPublicScenes();
        return ResponseEntity.ok(scenes);
    }

    /**
     * Search scenes by name or description
     */
    @GetMapping("/search")
    public ResponseEntity<List<SceneDTO>> searchScenes(@RequestParam String query) {
        List<SceneDTO> scenes = sceneService.searchScenes(query);
        return ResponseEntity.ok(scenes);
    }

    /**
     * Get recent scenes by user
     */
    @GetMapping("/user/{user}/recent")
    public ResponseEntity<List<SceneDTO>> getRecentScenesByUser(
            @PathVariable String user,
            @RequestParam(defaultValue = "10") int limit) {
        List<SceneDTO> scenes = sceneService.getRecentScenesByUser(user, limit);
        return ResponseEntity.ok(scenes);
    }

    /**
     * Count scenes by user
     */
    @GetMapping("/user/{user}/count")
    public ResponseEntity<Long> countScenesByUser(@PathVariable String user) {
        long count = sceneService.countScenesByUser(user);
        return ResponseEntity.ok(count);
    }

    /**
     * Create new scene
     */
    @PostMapping
    public ResponseEntity<SceneDTO> createScene(@Valid @RequestBody CreateSceneDTO createSceneDTO) {
        try {
            SceneDTO createdScene = sceneService.createScene(createSceneDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdScene);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update existing scene
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateScene(@PathVariable Long id,
            @Valid @RequestBody UpdateSceneDTO updateSceneDTO) {
        try {
            sceneService.updateScene(id, updateSceneDTO);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete scene
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteScene(@PathVariable Long id) {
        try {
            sceneService.deleteScene(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get products in scene
     */
    @GetMapping("/{id}/products")
    public ResponseEntity<List<SceneProductDTO>> getProductsInScene(@PathVariable Long id) {
        try {
            List<SceneProductDTO> products = sceneService.getProductsInScene(id);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add product to scene
     */
    @PostMapping("/{id}/products")
    public ResponseEntity<SceneProductDTO> addProductToScene(@PathVariable Long id,
            @Valid @RequestBody CreateSceneProductDTO productDTO) {
        try {
            SceneProductDTO addedProduct = sceneService.addProductToScene(id, productDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update product in scene
     */
    @PutMapping("/{sceneId}/products/{sceneProductId}")
    public ResponseEntity<SceneProductDTO> updateSceneProduct(
            @PathVariable Long sceneId,
            @PathVariable Long sceneProductId,
            @Valid @RequestBody CreateSceneProductDTO updateDTO) {
        try {
            SceneProductDTO updatedProduct = sceneService.updateSceneProduct(sceneProductId, updateDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove product from scene
     */
    @DeleteMapping("/{sceneId}/products/{sceneProductId}")
    public ResponseEntity<Void> removeProductFromScene(@PathVariable Long sceneId,
            @PathVariable Long sceneProductId) {
        try {
            sceneService.removeProductFromScene(sceneId, sceneProductId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create or update room model for a scene
     */
    @PostMapping("/{sceneId}/room-model")
    public ResponseEntity<SceneRoomModelDTO> createOrUpdateRoomModel(@PathVariable Long sceneId,
            @Valid @RequestBody CreateSceneRoomModelDTO roomModelDTO) {
        try {
            SceneRoomModelDTO savedRoomModel = sceneService.createOrUpdateRoomModel(sceneId, roomModelDTO);
            return ResponseEntity.ok(savedRoomModel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get room model for a scene
     */
    @GetMapping("/{sceneId}/room-model")
    public ResponseEntity<SceneRoomModelDTO> getRoomModel(@PathVariable Long sceneId) {
        SceneRoomModelDTO roomModel = sceneService.getRoomModelBySceneId(sceneId);
        return roomModel != null ? ResponseEntity.ok(roomModel) : ResponseEntity.notFound().build();
    }

    /**
     * Delete room model from scene
     */
    @DeleteMapping("/{sceneId}/room-model")
    public ResponseEntity<Void> deleteRoomModel(@PathVariable Long sceneId) {
        try {
            sceneService.deleteRoomModel(sceneId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create or update covering for a scene
     */
    @PostMapping("/{sceneId}/coverings")
    public ResponseEntity<SceneCoveringDTO> createOrUpdateCovering(@PathVariable Long sceneId,
            @Valid @RequestBody CreateSceneCoveringDTO coveringDTO) {
        try {
            SceneCoveringDTO savedCovering = sceneService.createOrUpdateCovering(sceneId, coveringDTO);
            return ResponseEntity.ok(savedCovering);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all coverings for a scene
     */
    @GetMapping("/{sceneId}/coverings")
    public ResponseEntity<List<SceneCoveringDTO>> getCoverings(@PathVariable Long sceneId) {
        List<SceneCoveringDTO> coverings = sceneService.getCoveringsBySceneId(sceneId);
        return ResponseEntity.ok(coverings);
    }

    /**
     * Delete covering from scene
     */
    @DeleteMapping("/{sceneId}/coverings/{coveringId}")
    public ResponseEntity<Void> deleteCovering(@PathVariable Long sceneId,
            @PathVariable Long coveringId) {
        try {
            sceneService.deleteCovering(coveringId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete all coverings from scene
     */
    @DeleteMapping("/{sceneId}/coverings")
    public ResponseEntity<Void> deleteAllCoverings(@PathVariable Long sceneId) {
        try {
            sceneService.deleteCoveringsBySceneId(sceneId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}