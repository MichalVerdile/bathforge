package com.bathforge.service.scene;

import com.bathforge.dto.scene.*;
import com.bathforge.model.products.Color;
import com.bathforge.model.products.Product;
import com.bathforge.model.scene.Scene;
import com.bathforge.model.scene.SceneProduct;
import com.bathforge.repository.products.ColorRepository;
import com.bathforge.repository.products.ProductRepository;
import com.bathforge.repository.scene.SceneProductRepository;
import com.bathforge.repository.scene.SceneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SceneService {

    private final SceneRepository sceneRepository;
    private final SceneProductRepository sceneProductRepository;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;

    public SceneService(SceneRepository sceneRepository,
                        SceneProductRepository sceneProductRepository,
                        ProductRepository productRepository,
                        ColorRepository colorRepository) {
        this.sceneRepository = sceneRepository;
        this.sceneProductRepository = sceneProductRepository;
        this.productRepository = productRepository;
        this.colorRepository = colorRepository;
    }

    @Transactional(readOnly = true)
    public List<SceneDTO> getAllScenes() {
        return sceneRepository.findAll()
                .stream()
                .map(this::toSceneDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<SceneDTO> getSceneById(Long id) {
        return sceneRepository.findById(id).map(this::toSceneDTO);
    }

    @Transactional(readOnly = true)
    public List<SceneDTO> getScenesByUser(String user) {
        return sceneRepository.findByUsernameOrderByUpdatedAtDesc(user)
                .stream()
                .map(this::toSceneDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SceneDTO> getPublicScenes() {
        return sceneRepository.findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toSceneDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SceneDTO> searchScenes(String searchText) {
        return sceneRepository.searchByNameOrDescription(searchText)
                .stream()
                .map(this::toSceneDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SceneProductDTO> getProductsInScene(Long sceneId) {
        return sceneProductRepository.findBySceneIdWithDetails(sceneId)
                .stream()
                .map(this::toSceneProductDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SceneDTO> getRecentScenesByUser(String user, int limit) {
        return sceneRepository.findRecentScenesByUser(user)
                .stream()
                .limit(Math.max(0, limit))
                .map(this::toSceneDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countScenesByUser(String user) {
        return sceneRepository.countByUsername(user);
    }

    public SceneDTO createScene(CreateSceneDTO createSceneDTO) {
        Scene scene = fromCreateDTO(createSceneDTO);
        Scene saved = sceneRepository.save(scene);

        if (createSceneDTO.getProducts() != null && !createSceneDTO.getProducts().isEmpty()) {
            Set<SceneProduct> items = createSceneProducts(saved, createSceneDTO.getProducts());
            sceneProductRepository.saveAll(items);
            saved.setSceneProducts(items);
        }

        return toSceneDTO(saved);
    }

    public SceneDTO updateScene(Long id, UpdateSceneDTO updateSceneDTO) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found with id: " + id));

        applyUpdate(scene, updateSceneDTO);
        Scene updated = sceneRepository.save(scene);

        if (updateSceneDTO.getProducts() != null) {
            sceneProductRepository.deleteBySceneId(id);
            Set<SceneProduct> items = createSceneProducts(updated, updateSceneDTO.getProducts());
            sceneProductRepository.saveAll(items);
            updated.setSceneProducts(items);
        }

        return toSceneDTO(updated);
    }

    public void deleteScene(Long id) {
        if (!sceneRepository.existsById(id)) {
            throw new IllegalArgumentException("Scene not found with id: " + id);
        }
        sceneRepository.deleteById(id);
    }

    public SceneProductDTO addProductToScene(Long sceneId, CreateSceneProductDTO productDTO) {
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found with id: " + sceneId));

        SceneProduct sp = buildSceneProduct(scene, productDTO);
        SceneProduct saved = sceneProductRepository.save(sp);
        return toSceneProductDTO(saved);
    }

    public void removeProductFromScene(Long sceneId, Long sceneProductId) {
        SceneProduct sp = sceneProductRepository.findById(sceneProductId)
                .orElseThrow(() -> new IllegalArgumentException("Scene product not found with id: " + sceneProductId));
        if (!Objects.equals(sp.getScene().getId(), sceneId)) {
            throw new IllegalArgumentException("Scene product does not belong to the specified scene");
        }
        sceneProductRepository.delete(sp);
    }

    public SceneProductDTO updateSceneProduct(Long sceneProductId, CreateSceneProductDTO updateDTO) {
        SceneProduct sp = sceneProductRepository.findById(sceneProductId)
                .orElseThrow(() -> new IllegalArgumentException("Scene product not found with id: " + sceneProductId));

        applySceneProductUpdate(sp, updateDTO);
        SceneProduct saved = sceneProductRepository.save(sp);
        return toSceneProductDTO(saved);
    }

    private Scene fromCreateDTO(CreateSceneDTO dto) {
        Scene s = new Scene();
        s.setName(dto.getName());
        s.setDescription(dto.getDescription());
        s.setUser(dto.getUser() != null ? dto.getUser() : "guest");
        s.setSceneData(dto.getSceneData());
        s.setCameraPosition(dto.getCameraPosition());
        s.setLightingSettings(dto.getLightingSettings());
        s.setBackgroundColor(dto.getBackgroundColor());
        s.setIsPublic(Boolean.TRUE.equals(dto.getIsPublic()));
        return s;
    }

    private void applyUpdate(Scene s, UpdateSceneDTO dto) {
        if (dto.getName() != null) s.setName(dto.getName());
        if (dto.getDescription() != null) s.setDescription(dto.getDescription());
        if (dto.getSceneData() != null) s.setSceneData(dto.getSceneData());
        if (dto.getCameraPosition() != null) s.setCameraPosition(dto.getCameraPosition());
        if (dto.getLightingSettings() != null) s.setLightingSettings(dto.getLightingSettings());
        if (dto.getBackgroundColor() != null) s.setBackgroundColor(dto.getBackgroundColor());
        if (dto.getIsPublic() != null) s.setIsPublic(dto.getIsPublic());
    }

    private Set<SceneProduct> createSceneProducts(Scene scene, List<CreateSceneProductDTO> items) {
        return items.stream()
                .map(dto -> buildSceneProduct(scene, dto))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private SceneProduct buildSceneProduct(Scene scene, CreateSceneProductDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + dto.getProductId()));

        Color color = null;
        if (dto.getColorId() != null) {
            color = colorRepository.findById(dto.getColorId())
                    .orElseThrow(() -> new IllegalArgumentException("Color not found with id: " + dto.getColorId()));
        }

        SceneProduct sp = new SceneProduct();
        sp.setScene(scene);
        sp.setProduct(product);
        sp.setColor(color);
        sp.setPositionX(dto.getPositionX());
        sp.setPositionY(dto.getPositionY());
        sp.setPositionZ(dto.getPositionZ());
        sp.setRotationX(dto.getRotationX());
        sp.setRotationY(dto.getRotationY());
        sp.setRotationZ(dto.getRotationZ());
        sp.setScaleX(dto.getScaleX());
        sp.setScaleY(dto.getScaleY());
        sp.setScaleZ(dto.getScaleZ());
        sp.setCustomProperties(dto.getCustomProperties());
        return sp;
    }

    private void applySceneProductUpdate(SceneProduct sp, CreateSceneProductDTO dto) {
        if (dto.getColorId() != null) {
            Color color = colorRepository.findById(dto.getColorId())
                    .orElseThrow(() -> new IllegalArgumentException("Color not found with id: " + dto.getColorId()));
            sp.setColor(color);
        }
        if (dto.getPositionX() != null) sp.setPositionX(dto.getPositionX());
        if (dto.getPositionY() != null) sp.setPositionY(dto.getPositionY());
        if (dto.getPositionZ() != null) sp.setPositionZ(dto.getPositionZ());
        if (dto.getRotationX() != null) sp.setRotationX(dto.getRotationX());
        if (dto.getRotationY() != null) sp.setRotationY(dto.getRotationY());
        if (dto.getRotationZ() != null) sp.setRotationZ(dto.getRotationZ());
        if (dto.getScaleX() != null) sp.setScaleX(dto.getScaleX());
        if (dto.getScaleY() != null) sp.setScaleY(dto.getScaleY());
        if (dto.getScaleZ() != null) sp.setScaleZ(dto.getScaleZ());
        if (dto.getCustomProperties() != null) sp.setCustomProperties(dto.getCustomProperties());
    }

    private SceneDTO toSceneDTO(Scene s) {
        SceneDTO dto = new SceneDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setDescription(s.getDescription());
        dto.setUser(s.getUser());
        dto.setSceneData(s.getSceneData());
        dto.setCameraPosition(s.getCameraPosition());
        dto.setLightingSettings(s.getLightingSettings());
        dto.setBackgroundColor(s.getBackgroundColor());
        dto.setIsPublic(s.getIsPublic());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setUpdatedAt(s.getUpdatedAt());

        if (s.getSceneProducts() != null && !s.getSceneProducts().isEmpty()) {
            dto.setSceneProducts(
                    s.getSceneProducts().stream()
                            .map(this::toSceneProductDTO)
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }

    private SceneProductDTO toSceneProductDTO(SceneProduct sp) {
        SceneProductDTO dto = new SceneProductDTO();
        dto.setId(sp.getId());
        dto.setSceneId(sp.getScene().getId());
        dto.setProductId(sp.getProduct().getId());
        dto.setProductName(sp.getProduct().getName());
        dto.setProductModelPath(sp.getProduct().getModelPath());

        if (sp.getColor() != null) {
            dto.setColorId(sp.getColor().getId());
            dto.setColorName(sp.getColor().getName());
            dto.setColorHexCode(sp.getColor().getHexCode());
        }

        dto.setPositionX(sp.getPositionX());
        dto.setPositionY(sp.getPositionY());
        dto.setPositionZ(sp.getPositionZ());
        dto.setRotationX(sp.getRotationX());
        dto.setRotationY(sp.getRotationY());
        dto.setRotationZ(sp.getRotationZ());
        dto.setScaleX(sp.getScaleX());
        dto.setScaleY(sp.getScaleY());
        dto.setScaleZ(sp.getScaleZ());
        dto.setCustomProperties(sp.getCustomProperties());
        return dto;
    }
}
