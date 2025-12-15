package com.bathforge.service.scene;

import com.bathforge.dto.scene.*;
import com.bathforge.model.products.Color;
import com.bathforge.model.products.Product;
import com.bathforge.model.scene.Scene;
import com.bathforge.model.scene.SceneProduct;
import com.bathforge.model.scene.SceneRoomModel;
import com.bathforge.model.scene.SceneCovering;
import com.bathforge.model.user.User;
import com.bathforge.repository.products.ColorRepository;
import com.bathforge.repository.products.ProductRepository;
import com.bathforge.repository.scene.SceneProductRepository;
import com.bathforge.repository.scene.SceneRepository;
import com.bathforge.repository.scene.SceneRoomModelRepository;
import com.bathforge.repository.scene.SceneCoveringRepository;
import com.bathforge.service.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SceneService {

    private final SceneRepository sceneRepository;
    private final SceneProductRepository sceneProductRepository;
    private final SceneRoomModelRepository sceneRoomModelRepository;
    private final SceneCoveringRepository sceneCoveringRepository;
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;
    private final UserService userService;

    public SceneService(SceneRepository sceneRepository,
            SceneProductRepository sceneProductRepository,
            SceneRoomModelRepository sceneRoomModelRepository,
            SceneCoveringRepository sceneCoveringRepository,
            ProductRepository productRepository,
            ColorRepository colorRepository,
            UserService userService) {
        this.sceneRepository = sceneRepository;
        this.sceneProductRepository = sceneProductRepository;
        this.sceneRoomModelRepository = sceneRoomModelRepository;
        this.sceneCoveringRepository = sceneCoveringRepository;
        this.productRepository = productRepository;
        this.colorRepository = colorRepository;
        this.userService = userService;
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

    @Transactional
    public SceneDTO createScene(CreateSceneDTO createSceneDTO) {
        Scene scene = fromCreateDTO(createSceneDTO);
        Scene saved = sceneRepository.save(scene);

        if (createSceneDTO.getProducts() != null && !createSceneDTO.getProducts().isEmpty()) {
            Set<SceneProduct> items = createSceneProducts(saved, createSceneDTO.getProducts());
            sceneProductRepository.saveAll(items);
            saved.setSceneProducts(items);
        }

        // Create room model if provided
        if (createSceneDTO.getRoomModel() != null) {
            createOrUpdateRoomModel(saved.getId(), createSceneDTO.getRoomModel());
        }

        // Create coverings if provided
        if (createSceneDTO.getCoverings() != null && !createSceneDTO.getCoverings().isEmpty()) {
            for (CreateSceneCoveringDTO coveringDTO : createSceneDTO.getCoverings()) {
                createOrUpdateCovering(saved.getId(), coveringDTO);
            }
        }

        return toSceneDTO(saved);
    }

    @Transactional
    public SceneDTO createSceneForUser(CreateSceneDTO createSceneDTO, User user) {
        Scene scene = fromCreateDTO(createSceneDTO);
        // Override the user association with the provided user entity
        scene.setUserEntity(user);
        scene.setUser(null); // Clear the username string since we have the entity
        Scene saved = sceneRepository.save(scene);

        if (createSceneDTO.getProducts() != null && !createSceneDTO.getProducts().isEmpty()) {
            Set<SceneProduct> items = createSceneProducts(saved, createSceneDTO.getProducts());
            sceneProductRepository.saveAll(items);
            saved.setSceneProducts(items);
        }

        // Create room model if provided
        if (createSceneDTO.getRoomModel() != null) {
            createOrUpdateRoomModel(saved.getId(), createSceneDTO.getRoomModel());
        }

        // Create coverings if provided
        if (createSceneDTO.getCoverings() != null && !createSceneDTO.getCoverings().isEmpty()) {
            for (CreateSceneCoveringDTO coveringDTO : createSceneDTO.getCoverings()) {
                createOrUpdateCovering(saved.getId(), coveringDTO);
            }
        }

        return toSceneDTO(saved);
    }

    @Transactional
    public SceneDTO updateScene(Long id, UpdateSceneDTO updateSceneDTO) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found with id: " + id));

        applyUpdate(scene, updateSceneDTO);
        Scene updated = sceneRepository.save(scene);

        if (updateSceneDTO.getProducts() != null) {
            // Clear existing products properly
            if (updated.getSceneProducts() != null) {
                updated.getSceneProducts().clear();
            }

            // Create new products and add them to the existing collection
            Set<SceneProduct> newItems = createSceneProducts(updated, updateSceneDTO.getProducts());
            if (updated.getSceneProducts() == null) {
                updated.setSceneProducts(new LinkedHashSet<>());
            }
            updated.getSceneProducts().addAll(newItems);
        }

        // Update room model if provided
        if (updateSceneDTO.getRoomModel() != null) {
            createOrUpdateRoomModel(id, updateSceneDTO.getRoomModel());
        }

        // Update coverings if provided
        if (updateSceneDTO.getCoverings() != null) {
            // Clear existing coverings first
            sceneCoveringRepository.deleteBySceneId(id);
            // Add new coverings
            for (CreateSceneCoveringDTO coveringDTO : updateSceneDTO.getCoverings()) {
                createOrUpdateCovering(id, coveringDTO);
            }
        }

        return toSceneDTO(updated);
    }

    public void deleteScene(Long id) {
        if (!sceneRepository.existsById(id)) {
            throw new IllegalArgumentException("Scene not found with id: " + id);
        }
        sceneRepository.deleteById(id);
    }

    @Transactional
    public SceneProductDTO addProductToScene(Long sceneId, CreateSceneProductDTO productDTO) {
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found with id: " + sceneId));

        SceneProduct sp = buildSceneProduct(scene, productDTO);
        SceneProduct saved = sceneProductRepository.save(sp);
        return toSceneProductDTO(saved);
    }

    @Transactional
    public void removeProductFromScene(Long sceneId, Long sceneProductId) {
        SceneProduct sp = sceneProductRepository.findById(sceneProductId)
                .orElseThrow(() -> new IllegalArgumentException("Scene product not found with id: " + sceneProductId));
        if (!Objects.equals(sp.getScene().getId(), sceneId)) {
            throw new IllegalArgumentException("Scene product does not belong to the specified scene");
        }
        sceneProductRepository.delete(sp);
    }

    @Transactional
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

        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String userEmail = authentication.getName();
            User user = userService.findByEmail(userEmail).orElse(null);
            if (user != null) {
                s.setUserEntity(user);
            } else {
                // Fallback to username string if user not found
                s.setUser(dto.getUser() != null ? dto.getUser() : "guest");
            }
        } else {
            // Not authenticated, use guest or provided username
            s.setUser(dto.getUser() != null ? dto.getUser() : "guest");
        }

        s.setSceneData(dto.getSceneData());
        s.setCameraPosition(dto.getCameraPosition());
        s.setLightingSettings(dto.getLightingSettings());
        s.setBackgroundColor(dto.getBackgroundColor());
        s.setIsPublic(Boolean.TRUE.equals(dto.getIsPublic()));
        return s;
    }

    private void applyUpdate(Scene s, UpdateSceneDTO dto) {
        if (dto.getName() != null)
            s.setName(dto.getName());
        if (dto.getDescription() != null)
            s.setDescription(dto.getDescription());
        if (dto.getSceneData() != null)
            s.setSceneData(dto.getSceneData());
        if (dto.getCameraPosition() != null)
            s.setCameraPosition(dto.getCameraPosition());
        if (dto.getLightingSettings() != null)
            s.setLightingSettings(dto.getLightingSettings());
        if (dto.getBackgroundColor() != null)
            s.setBackgroundColor(dto.getBackgroundColor());
        if (dto.getIsPublic() != null)
            s.setIsPublic(dto.getIsPublic());
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
        if (dto.getPositionX() != null)
            sp.setPositionX(dto.getPositionX());
        if (dto.getPositionY() != null)
            sp.setPositionY(dto.getPositionY());
        if (dto.getPositionZ() != null)
            sp.setPositionZ(dto.getPositionZ());
        if (dto.getRotationX() != null)
            sp.setRotationX(dto.getRotationX());
        if (dto.getRotationY() != null)
            sp.setRotationY(dto.getRotationY());
        if (dto.getRotationZ() != null)
            sp.setRotationZ(dto.getRotationZ());
        if (dto.getScaleX() != null)
            sp.setScaleX(dto.getScaleX());
        if (dto.getScaleY() != null)
            sp.setScaleY(dto.getScaleY());
        if (dto.getScaleZ() != null)
            sp.setScaleZ(dto.getScaleZ());
        if (dto.getCustomProperties() != null)
            sp.setCustomProperties(dto.getCustomProperties());
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
                            .collect(Collectors.toList()));
        }

        // Include room model if it exists
        if (s.getRoomModel() != null) {
            dto.setRoomModel(toSceneRoomModelDTO(s.getRoomModel()));
        }

        // Include coverings if they exist
        if (s.getSceneCoverings() != null && !s.getSceneCoverings().isEmpty()) {
            dto.setSceneCoverings(
                    s.getSceneCoverings().stream()
                            .map(this::toSceneCoveringDTO)
                            .collect(Collectors.toList()));
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

    @Transactional
    public SceneRoomModelDTO createOrUpdateRoomModel(Long sceneId, CreateSceneRoomModelDTO roomModelDTO) {
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found with id: " + sceneId));

        SceneRoomModel existingRoomModel = sceneRoomModelRepository.findBySceneId(sceneId);

        SceneRoomModel roomModel;
        if (existingRoomModel != null) {
            // Update existing room model
            roomModel = existingRoomModel;
            roomModel.setVerticesData(roomModelDTO.getVerticesData());
            roomModel.setRoomHeight(roomModelDTO.getRoomHeight());
            roomModel.setModelType(roomModelDTO.getModelType());
            roomModel.setTemplatePath(roomModelDTO.getTemplatePath());
            roomModel.setRoomProperties(roomModelDTO.getRoomProperties());
        } else {
            // Create new room model
            roomModel = new SceneRoomModel(scene, roomModelDTO.getVerticesData(), roomModelDTO.getRoomHeight());
            roomModel.setModelType(roomModelDTO.getModelType());
            roomModel.setTemplatePath(roomModelDTO.getTemplatePath());
            roomModel.setRoomProperties(roomModelDTO.getRoomProperties());
        }

        SceneRoomModel saved = sceneRoomModelRepository.save(roomModel);
        return toSceneRoomModelDTO(saved);
    }

    @Transactional
    public SceneRoomModelDTO getRoomModelBySceneId(Long sceneId) {
        SceneRoomModel roomModel = sceneRoomModelRepository.findBySceneId(sceneId);
        return roomModel != null ? toSceneRoomModelDTO(roomModel) : null;
    }

    @Transactional
    public void deleteRoomModel(Long sceneId) {
        sceneRoomModelRepository.deleteBySceneId(sceneId);
    }

    @Transactional
    public SceneCoveringDTO createOrUpdateCovering(Long sceneId, CreateSceneCoveringDTO coveringDTO) {
        System.out.println("DEBUG: Creating/updating covering for scene " + sceneId +
                ", surface: " + coveringDTO.getSurfaceIdentifier() +
                ", type: " + coveringDTO.getSurfaceType() +
                ", productId: " + coveringDTO.getProductId());

        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found with id: " + sceneId));

        Product product = productRepository.findById(coveringDTO.getProductId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Product not found with id: " + coveringDTO.getProductId()));

        // Check if a covering already exists for this surface
        SceneCovering existingCovering = null;
        if (coveringDTO.getSurfaceIdentifier() != null) {
            System.out.println("DEBUG: Looking for existing covering with surface identifier: "
                    + coveringDTO.getSurfaceIdentifier());
            existingCovering = sceneCoveringRepository.findBySceneIdAndSurfaceIdentifier(sceneId,
                    coveringDTO.getSurfaceIdentifier());
            System.out.println("DEBUG: Found existing covering: "
                    + (existingCovering != null ? existingCovering.getId() : "null"));
        }

        SceneCovering covering;
        if (existingCovering != null) {
            // Update existing covering
            System.out.println("DEBUG: Updating existing covering with ID: " + existingCovering.getId());
            covering = existingCovering;
            covering.setProduct(product);
            covering.setSurfaceType(coveringDTO.getSurfaceType());
            covering.setRepeatX(coveringDTO.getRepeatX());
            covering.setRepeatY(coveringDTO.getRepeatY());
            covering.setMaterialProperties(coveringDTO.getMaterialProperties());
        } else {
            // Create new covering
            System.out.println("DEBUG: Creating new covering");
            covering = new SceneCovering(scene, product, coveringDTO.getSurfaceType(),
                    coveringDTO.getSurfaceIdentifier());
            covering.setRepeatX(coveringDTO.getRepeatX());
            covering.setRepeatY(coveringDTO.getRepeatY());
            covering.setMaterialProperties(coveringDTO.getMaterialProperties());
        }

        SceneCovering saved = sceneCoveringRepository.save(covering);
        System.out.println("DEBUG: Saved covering with ID: " + saved.getId());
        return toSceneCoveringDTO(saved);
    }

    public List<SceneCoveringDTO> getCoveringsBySceneId(Long sceneId) {
        List<SceneCovering> coverings = sceneCoveringRepository.findBySceneId(sceneId);
        return coverings.stream()
                .map(this::toSceneCoveringDTO)
                .collect(Collectors.toList());
    }

    public void deleteCovering(Long coveringId) {
        sceneCoveringRepository.deleteById(coveringId);
    }

    public void deleteCoveringsBySceneId(Long sceneId) {
        sceneCoveringRepository.deleteBySceneId(sceneId);
    }

    private SceneRoomModelDTO toSceneRoomModelDTO(SceneRoomModel roomModel) {
        SceneRoomModelDTO dto = new SceneRoomModelDTO();
        dto.setId(roomModel.getId());
        dto.setSceneId(roomModel.getScene().getId());
        dto.setVerticesData(roomModel.getVerticesData());
        dto.setRoomHeight(roomModel.getRoomHeight());
        dto.setModelType(roomModel.getModelType());
        dto.setTemplatePath(roomModel.getTemplatePath());
        dto.setRoomProperties(roomModel.getRoomProperties());
        return dto;
    }

    private SceneCoveringDTO toSceneCoveringDTO(SceneCovering covering) {
        SceneCoveringDTO dto = new SceneCoveringDTO();
        dto.setId(covering.getId());
        dto.setSceneId(covering.getScene().getId());
        dto.setProductId(covering.getProduct().getId());
        dto.setProductName(covering.getProduct().getName());
        dto.setProductModelPath(covering.getProduct().getModelPath());
        dto.setSurfaceType(covering.getSurfaceType());
        dto.setSurfaceIdentifier(covering.getSurfaceIdentifier());
        dto.setRepeatX(covering.getRepeatX());
        dto.setRepeatY(covering.getRepeatY());
        dto.setMaterialProperties(covering.getMaterialProperties());
        return dto;
    }
}
