package com.bathforge.service.scene;

import com.bathforge.dto.scene.*;
import com.bathforge.model.scene.Scene;
import com.bathforge.model.scene.SceneProduct;
import com.bathforge.model.products.Product;
import com.bathforge.model.products.Color;
import com.bathforge.repository.scene.SceneRepository;
import com.bathforge.repository.scene.SceneProductRepository;
import com.bathforge.repository.products.ProductRepository;
import com.bathforge.repository.products.ColorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@Transactional
public class SceneService {

    private final SceneRepository sceneRepository;
    private final SceneProductRepository sceneProductRepository;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;

    @Autowired
    public SceneService(SceneRepository sceneRepository,
            SceneProductRepository sceneProductRepository,
            ProductRepository productRepository,
            ColorRepository colorRepository) {
        this.sceneRepository = sceneRepository;
        this.sceneProductRepository = sceneProductRepository;
        this.productRepository = productRepository;
        this.colorRepository = colorRepository;
    }

    /**
     * Get all scenes
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> getAllScenes() {
        return sceneRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get scene by ID
     */
    @Transactional(readOnly = true)
    public Optional<SceneDTO> getSceneById(Long id) {
        return sceneRepository.findById(id).map(this::convertToDTO);
    }

    /**
     * Get scenes by user
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> getScenesByUser(String user) {
        return sceneRepository.findByUsernameOrderByUpdatedAtDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get public scenes
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> getPublicScenes() {
        return sceneRepository.findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search scenes by name or description
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> searchScenes(String searchText) {
        return sceneRepository.searchByNameOrDescription(searchText)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create new scene
     */
    public SceneDTO createScene(CreateSceneDTO createSceneDTO) {
        Scene scene = convertFromCreateDTO(createSceneDTO);
        Scene savedScene = sceneRepository.save(scene);

        // Add products to scene if provided
        if (createSceneDTO.getProducts() != null && !createSceneDTO.getProducts().isEmpty()) {
            Set<SceneProduct> sceneProducts = new HashSet<>();
            for (CreateSceneProductDTO productDTO : createSceneDTO.getProducts()) {
                SceneProduct sceneProduct = createSceneProduct(savedScene, productDTO);
                if (sceneProduct != null) {
                    sceneProducts.add(sceneProduct);
                }
            }
            sceneProductRepository.saveAll(sceneProducts);
            savedScene.setSceneProducts(sceneProducts);
        }

        return convertToDTO(savedScene);
    }

    /**
     * Update existing scene
     */
    public SceneDTO updateScene(Long id, UpdateSceneDTO updateSceneDTO) {
        Scene existingScene = sceneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found with id: " + id));

        updateSceneFromDTO(existingScene, updateSceneDTO);
        Scene updatedScene = sceneRepository.save(existingScene);

        // Update products if provided
        if (updateSceneDTO.getProducts() != null) {
            // Remove existing products
            sceneProductRepository.deleteBySceneId(id);

            // Add new products
            Set<SceneProduct> sceneProducts = new HashSet<>();
            for (CreateSceneProductDTO productDTO : updateSceneDTO.getProducts()) {
                SceneProduct sceneProduct = createSceneProduct(updatedScene, productDTO);
                if (sceneProduct != null) {
                    sceneProducts.add(sceneProduct);
                }
            }
            sceneProductRepository.saveAll(sceneProducts);
            updatedScene.setSceneProducts(sceneProducts);
        }

        return convertToDTO(updatedScene);
    }

    /**
     * Delete scene
     */
    public void deleteScene(Long id) {
        if (!sceneRepository.existsById(id)) {
            throw new IllegalArgumentException("Scene not found with id: " + id);
        }
        sceneRepository.deleteById(id);
    }

    /**
     * Add product to scene
     */
    public SceneProductDTO addProductToScene(Long sceneId, CreateSceneProductDTO productDTO) {
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found with id: " + sceneId));

        SceneProduct sceneProduct = createSceneProduct(scene, productDTO);
        if (sceneProduct == null) {
            throw new IllegalArgumentException("Invalid product or color data");
        }

        SceneProduct savedSceneProduct = sceneProductRepository.save(sceneProduct);
        return convertSceneProductToDTO(savedSceneProduct);
    }

    /**
     * Remove product from scene
     */
    public void removeProductFromScene(Long sceneId, Long sceneProductId) {
        SceneProduct sceneProduct = sceneProductRepository.findById(sceneProductId)
                .orElseThrow(() -> new IllegalArgumentException("Scene product not found with id: " + sceneProductId));

        if (!sceneProduct.getScene().getId().equals(sceneId)) {
            throw new IllegalArgumentException("Scene product does not belong to the specified scene");
        }

        sceneProductRepository.delete(sceneProduct);
    }

    /**
     * Get products in scene
     */
    @Transactional(readOnly = true)
    public List<SceneProductDTO> getProductsInScene(Long sceneId) {
        List<SceneProduct> sceneProducts = sceneProductRepository.findBySceneIdWithDetails(sceneId);
        return sceneProducts.stream()
                .map(this::convertSceneProductToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update product position/rotation in scene
     */
    public SceneProductDTO updateSceneProduct(Long sceneProductId, CreateSceneProductDTO updateDTO) {
        SceneProduct sceneProduct = sceneProductRepository.findById(sceneProductId)
                .orElseThrow(() -> new IllegalArgumentException("Scene product not found with id: " + sceneProductId));

        updateSceneProductFromDTO(sceneProduct, updateDTO);
        SceneProduct updatedSceneProduct = sceneProductRepository.save(sceneProduct);
        return convertSceneProductToDTO(updatedSceneProduct);
    }

    /**
     * Get scenes recently updated by user
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> getRecentScenesByUser(String user, int limit) {
        return sceneRepository.findRecentScenesByUser(user)
                .stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Count scenes by user
     */
    @Transactional(readOnly = true)
    public long countScenesByUser(String user) {
        return sceneRepository.countByUsername(user);
    }

    // Helper methods for conversion

    private Scene convertFromCreateDTO(CreateSceneDTO createDTO) {
        Scene scene = new Scene();
        scene.setName(createDTO.getName());
        scene.setDescription(createDTO.getDescription());
        scene.setUser(createDTO.getUser() != null ? createDTO.getUser() : "guest");
        scene.setSceneData(createDTO.getSceneData());
        scene.setCameraPosition(createDTO.getCameraPosition());
        scene.setLightingSettings(createDTO.getLightingSettings());
        scene.setBackgroundColor(createDTO.getBackgroundColor());
        scene.setIsPublic(createDTO.getIsPublic() != null ? createDTO.getIsPublic() : false);
        return scene;
    }

    private void updateSceneFromDTO(Scene scene, UpdateSceneDTO updateDTO) {
        if (updateDTO.getName() != null) {
            scene.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            scene.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getSceneData() != null) {
            scene.setSceneData(updateDTO.getSceneData());
        }
        if (updateDTO.getCameraPosition() != null) {
            scene.setCameraPosition(updateDTO.getCameraPosition());
        }
        if (updateDTO.getLightingSettings() != null) {
            scene.setLightingSettings(updateDTO.getLightingSettings());
        }
        if (updateDTO.getBackgroundColor() != null) {
            scene.setBackgroundColor(updateDTO.getBackgroundColor());
        }
        if (updateDTO.getIsPublic() != null) {
            scene.setIsPublic(updateDTO.getIsPublic());
        }
    }

    private SceneProduct createSceneProduct(Scene scene, CreateSceneProductDTO productDTO) {
        Product product = productRepository.findById(productDTO.getProductId())
                .orElse(null);

        if (product == null) {
            return null;
        }

        Color color = null;
        if (productDTO.getColorId() != null) {
            color = colorRepository.findById(productDTO.getColorId()).orElse(null);
        }

        SceneProduct sceneProduct = new SceneProduct();
        sceneProduct.setScene(scene);
        sceneProduct.setProduct(product);
        sceneProduct.setColor(color);
        sceneProduct.setPositionX(productDTO.getPositionX());
        sceneProduct.setPositionY(productDTO.getPositionY());
        sceneProduct.setPositionZ(productDTO.getPositionZ());
        sceneProduct.setRotationX(productDTO.getRotationX());
        sceneProduct.setRotationY(productDTO.getRotationY());
        sceneProduct.setRotationZ(productDTO.getRotationZ());
        sceneProduct.setScaleX(productDTO.getScaleX());
        sceneProduct.setScaleY(productDTO.getScaleY());
        sceneProduct.setScaleZ(productDTO.getScaleZ());
        sceneProduct.setCustomProperties(productDTO.getCustomProperties());

        return sceneProduct;
    }

    private void updateSceneProductFromDTO(SceneProduct sceneProduct, CreateSceneProductDTO updateDTO) {
        if (updateDTO.getColorId() != null) {
            Color color = colorRepository.findById(updateDTO.getColorId()).orElse(null);
            sceneProduct.setColor(color);
        }
        if (updateDTO.getPositionX() != null) {
            sceneProduct.setPositionX(updateDTO.getPositionX());
        }
        if (updateDTO.getPositionY() != null) {
            sceneProduct.setPositionY(updateDTO.getPositionY());
        }
        if (updateDTO.getPositionZ() != null) {
            sceneProduct.setPositionZ(updateDTO.getPositionZ());
        }
        if (updateDTO.getRotationX() != null) {
            sceneProduct.setRotationX(updateDTO.getRotationX());
        }
        if (updateDTO.getRotationY() != null) {
            sceneProduct.setRotationY(updateDTO.getRotationY());
        }
        if (updateDTO.getRotationZ() != null) {
            sceneProduct.setRotationZ(updateDTO.getRotationZ());
        }
        if (updateDTO.getScaleX() != null) {
            sceneProduct.setScaleX(updateDTO.getScaleX());
        }
        if (updateDTO.getScaleY() != null) {
            sceneProduct.setScaleY(updateDTO.getScaleY());
        }
        if (updateDTO.getScaleZ() != null) {
            sceneProduct.setScaleZ(updateDTO.getScaleZ());
        }
        if (updateDTO.getCustomProperties() != null) {
            sceneProduct.setCustomProperties(updateDTO.getCustomProperties());
        }
    }

    private SceneDTO convertToDTO(Scene scene) {
        SceneDTO dto = new SceneDTO();
        dto.setId(scene.getId());
        dto.setName(scene.getName());
        dto.setDescription(scene.getDescription());
        dto.setUser(scene.getUser());
        dto.setSceneData(scene.getSceneData());
        dto.setCameraPosition(scene.getCameraPosition());
        dto.setLightingSettings(scene.getLightingSettings());
        dto.setBackgroundColor(scene.getBackgroundColor());
        dto.setIsPublic(scene.getIsPublic());
        dto.setCreatedAt(scene.getCreatedAt());
        dto.setUpdatedAt(scene.getUpdatedAt());

        if (scene.getSceneProducts() != null) {
            List<SceneProductDTO> productDTOs = scene.getSceneProducts().stream()
                    .map(this::convertSceneProductToDTO)
                    .collect(Collectors.toList());
            dto.setSceneProducts(productDTOs);
        }

        return dto;
    }

    private SceneProductDTO convertSceneProductToDTO(SceneProduct sceneProduct) {
        SceneProductDTO dto = new SceneProductDTO();
        dto.setId(sceneProduct.getId());
        dto.setSceneId(sceneProduct.getScene().getId());
        dto.setProductId(sceneProduct.getProduct().getId());
        dto.setProductName(sceneProduct.getProduct().getName());
        dto.setProductModelPath(sceneProduct.getProduct().getModelPath());

        if (sceneProduct.getColor() != null) {
            dto.setColorId(sceneProduct.getColor().getId());
            dto.setColorName(sceneProduct.getColor().getName());
            dto.setColorHexCode(sceneProduct.getColor().getHexCode());
        }

        dto.setPositionX(sceneProduct.getPositionX());
        dto.setPositionY(sceneProduct.getPositionY());
        dto.setPositionZ(sceneProduct.getPositionZ());
        dto.setRotationX(sceneProduct.getRotationX());
        dto.setRotationY(sceneProduct.getRotationY());
        dto.setRotationZ(sceneProduct.getRotationZ());
        dto.setScaleX(sceneProduct.getScaleX());
        dto.setScaleY(sceneProduct.getScaleY());
        dto.setScaleZ(sceneProduct.getScaleZ());
        dto.setCustomProperties(sceneProduct.getCustomProperties());

        return dto;
    }
}