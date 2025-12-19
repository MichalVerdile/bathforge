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

/**
 * REST controller for managing bathroom scenes and their components.
 */
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
     * Retrieves all scenes.
     *
     * @return response entity with list of all scenes
     */
    @GetMapping
    public ResponseEntity<List<SceneDTO>> getAllScenes() {
        List<SceneDTO> scenes = sceneService.getAllScenes();
        return ResponseEntity.ok(scenes);
    }

    /**
     * Retrieves a scene by its ID.
     *
     * @param id the scene ID
     * @return response entity with the scene if found, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<SceneDTO> getSceneById(@PathVariable Long id) {
        Optional<SceneDTO> scene = sceneService.getSceneById(id);
        return scene.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves scenes by user.
     *
     * @param user the username
     * @return response entity with list of scenes for the user
     */
    @GetMapping("/user/{user}")
    public ResponseEntity<List<SceneDTO>> getScenesByUser(@PathVariable String user) {
        List<SceneDTO> scenes = sceneService.getScenesByUser(user);
        return ResponseEntity.ok(scenes);
    }

    /**
     * Retrieves public scenes.
     *
     * @return response entity with list of public scenes
     */
    @GetMapping("/public")
    public ResponseEntity<List<SceneDTO>> getPublicScenes() {
        List<SceneDTO> scenes = sceneService.getPublicScenes();
        return ResponseEntity.ok(scenes);
    }

    /**
     * Searches scenes by name or description.
     *
     * @param query the search query
     * @return response entity with list of matching scenes
     */
    @GetMapping("/search")
    public ResponseEntity<List<SceneDTO>> searchScenes(@RequestParam String query) {
        List<SceneDTO> scenes = sceneService.searchScenes(query);
        return ResponseEntity.ok(scenes);
    }

    /**
     * Retrieves recent scenes by user.
     *
     * @param user  the username
     * @param limit the maximum number of scenes to return (default: 10)
     * @return response entity with list of recent scenes
     */
    @GetMapping("/user/{user}/recent")
    public ResponseEntity<List<SceneDTO>> getRecentScenesByUser(
            @PathVariable String user,
            @RequestParam(defaultValue = "10") int limit) {
        List<SceneDTO> scenes = sceneService.getRecentScenesByUser(user, limit);
        return ResponseEntity.ok(scenes);
    }

    /**
     * Counts scenes by user.
     *
     * @param user the username
     * @return response entity with the count of user's scenes
     */
    @GetMapping("/user/{user}/count")
    public ResponseEntity<Long> countScenesByUser(@PathVariable String user) {
        long count = sceneService.countScenesByUser(user);
        return ResponseEntity.ok(count);
    }

    /**
     * Creates a new scene.
     *
     * @param createSceneDTO the scene data to create
     * @return response entity with the created scene and 201 status, or 400 if
     *         invalid
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
     * Updates an existing scene.
     *
     * @param id             the scene ID to update
     * @param updateSceneDTO the updated scene data
     * @return response entity with 200 status if successful, or 404 if not found
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
     * Deletes a scene by its ID.
     *
     * @param id the scene ID to delete
     * @return response entity with 204 status if successful, or 404 if not found
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
     * Retrieves products in a scene.
     *
     * @param id the scene ID
     * @return response entity with list of products in the scene, or 404 if scene
     *         not found
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
     * Adds a product to a scene.
     *
     * @param id         the scene ID
     * @param productDTO the product data to add
     * @return response entity with the added product and 201 status, or 400 if
     *         invalid
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
     * Updates a product in a scene.
     *
     * @param sceneId        the scene ID
     * @param sceneProductId the scene product ID to update
     * @param updateDTO      the updated product data
     * @return response entity with the updated product, or 404 if not found
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
     * Removes a product from a scene.
     *
     * @param sceneId        the scene ID
     * @param sceneProductId the scene product ID to remove
     * @return response entity with 204 status if successful, or 404 if not found
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
     * Creates or updates the room model for a scene.
     *
     * @param sceneId      the scene ID
     * @param roomModelDTO the room model data
     * @return response entity with the saved room model, or 400 if invalid
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
     * Retrieves the room model for a scene.
     *
     * @param sceneId the scene ID
     * @return response entity with the room model if found, or 404 if not found
     */
    @GetMapping("/{sceneId}/room-model")
    public ResponseEntity<SceneRoomModelDTO> getRoomModel(@PathVariable Long sceneId) {
        SceneRoomModelDTO roomModel = sceneService.getRoomModelBySceneId(sceneId);
        return roomModel != null ? ResponseEntity.ok(roomModel) : ResponseEntity.notFound().build();
    }

    /**
     * Deletes the room model from a scene.
     *
     * @param sceneId the scene ID
     * @return response entity with 204 status if successful, or 404 if not found
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
     * Creates or updates a covering for a scene.
     *
     * @param sceneId     the scene ID
     * @param coveringDTO the covering data
     * @return response entity with the saved covering, or 400 if invalid
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
     * Retrieves all coverings for a scene.
     *
     * @param sceneId the scene ID
     * @return response entity with list of coverings for the scene
     */
    @GetMapping("/{sceneId}/coverings")
    public ResponseEntity<List<SceneCoveringDTO>> getCoverings(@PathVariable Long sceneId) {
        List<SceneCoveringDTO> coverings = sceneService.getCoveringsBySceneId(sceneId);
        return ResponseEntity.ok(coverings);
    }

    /**
     * Deletes a covering from a scene.
     *
     * @param sceneId    the scene ID
     * @param coveringId the covering ID to delete
     * @return response entity with 204 status if successful, or 404 if not found
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
     * Deletes all coverings from a scene.
     *
     * @param sceneId the scene ID
     * @return response entity with 204 status if successful, or 404 if not found
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