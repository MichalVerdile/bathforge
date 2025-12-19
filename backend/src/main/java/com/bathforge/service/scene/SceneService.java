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

/**
 * Service for managing 3D bathroom scenes and their components.
 * Handles CRUD operations for scenes, products within scenes, room models, and
 * surface coverings.
 * Supports both public and user-specific scenes with full transformation and
 * customization capabilities.
 */
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

    /**
     * Retrieves all scenes in the system.
     *
     * @return list of all scenes as DTOs
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> getAllScenes() {
        return sceneRepository.findAll()
                .stream()
                .map(this::toSceneDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a scene by its unique identifier.
     *
     * @param id the scene ID
     * @return an Optional containing the scene DTO if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<SceneDTO> getSceneById(Long id) {
        return sceneRepository.findById(id).map(this::toSceneDTO);
    }

    /**
     * Retrieves all scenes belonging to a specific user, ordered by last update
     * time.
     *
     * @param user the username
     * @return list of scenes owned by the user
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> getScenesByUser(String user) {
        return sceneRepository.findByUsernameOrderByUpdatedAtDesc(user)
                .stream()
                .map(this::toSceneDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all public scenes available for viewing by any user.
     *
     * @return list of public scenes ordered by creation date
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> getPublicScenes() {
        return sceneRepository.findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toSceneDTO)
                .collect(Collectors.toList());
    }

    /**
     * Searches for scenes by name or description.
     *
     * @param searchText the search term to match against scene names and
     *                   descriptions
     * @return list of scenes matching the search criteria
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> searchScenes(String searchText) {
        return sceneRepository.searchByNameOrDescription(searchText)
                .stream()
                .map(this::toSceneDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all products placed within a specific scene.
     *
     * @param sceneId the scene ID
     * @return list of products in the scene with their positions, rotations, and
     *         colors
     */
    @Transactional(readOnly = true)
    public List<SceneProductDTO> getProductsInScene(Long sceneId) {
        return sceneProductRepository.findBySceneIdWithDetails(sceneId)
                .stream()
                .map(this::toSceneProductDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the most recent scenes for a specific user.
     *
     * @param user  the username
     * @param limit the maximum number of scenes to return
     * @return list of recent scenes, limited to the specified count
     */
    @Transactional(readOnly = true)
    public List<SceneDTO> getRecentScenesByUser(String user, int limit) {
        return sceneRepository.findRecentScenesByUser(user)
                .stream()
                .limit(Math.max(0, limit))
                .map(this::toSceneDTO)
                .collect(Collectors.toList());
    }

    /**
     * Counts the total number of scenes created by a specific user.
     *
     * @param user the username
     * @return the count of scenes owned by the user
     */
    @Transactional(readOnly = true)
    public long countScenesByUser(String user) {
        return sceneRepository.countByUsername(user);
    }

    /**
     * Creates a new scene with products, room model, and coverings.
     * Associates the scene with the currently authenticated user if available.
     *
     * @param createSceneDTO the scene data including products, room model, and
     *                       coverings
     * @return the created scene as a DTO
     */
    @Transactional
    public SceneDTO createScene(CreateSceneDTO createSceneDTO) {
        Scene scene = fromCreateDTO(createSceneDTO);
        Scene saved = sceneRepository.save(scene);

        if (createSceneDTO.getProducts() != null && !createSceneDTO.getProducts().isEmpty()) {
            Set<SceneProduct> items = createSceneProducts(saved, createSceneDTO.getProducts());
            sceneProductRepository.saveAll(items);
            saved.setSceneProducts(items);
        }

        if (createSceneDTO.getRoomModel() != null) {
            createOrUpdateRoomModel(saved.getId(), createSceneDTO.getRoomModel());
        }

        if (createSceneDTO.getCoverings() != null && !createSceneDTO.getCoverings().isEmpty()) {
            for (CreateSceneCoveringDTO coveringDTO : createSceneDTO.getCoverings()) {
                createOrUpdateCovering(saved.getId(), coveringDTO);
            }
        }

        return toSceneDTO(saved);
    }

    /**
     * Creates a new scene for a specific user (bypassing authentication).
     * Used when creating scenes programmatically for quote requests.
     *
     * @param createSceneDTO the scene data including products, room model, and
     *                       coverings
     * @param user           the user entity to associate the scene with
     * @return the created scene as a DTO
     */
    @Transactional
    public SceneDTO createSceneForUser(CreateSceneDTO createSceneDTO, User user) {
        Scene scene = fromCreateDTO(createSceneDTO);
        scene.setUserEntity(user);
        scene.setUser(null);
        Scene saved = sceneRepository.save(scene);

        if (createSceneDTO.getProducts() != null && !createSceneDTO.getProducts().isEmpty()) {
            Set<SceneProduct> items = createSceneProducts(saved, createSceneDTO.getProducts());
            sceneProductRepository.saveAll(items);
            saved.setSceneProducts(items);
        }

        if (createSceneDTO.getRoomModel() != null) {
            createOrUpdateRoomModel(saved.getId(), createSceneDTO.getRoomModel());
        }

        if (createSceneDTO.getCoverings() != null && !createSceneDTO.getCoverings().isEmpty()) {
            for (CreateSceneCoveringDTO coveringDTO : createSceneDTO.getCoverings()) {
                createOrUpdateCovering(saved.getId(), coveringDTO);
            }
        }

        return toSceneDTO(saved);
    }

    /**
     * Updates an existing scene's properties, products, room model, and coverings.
     *
     * @param id             the scene ID to update
     * @param updateSceneDTO the updated scene data
     * @return the updated scene as a DTO
     * @throws IllegalArgumentException if the scene is not found
     */
    @Transactional
    public SceneDTO updateScene(Long id, UpdateSceneDTO updateSceneDTO) {
        Scene scene = sceneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found with id: " + id));

        applyUpdate(scene, updateSceneDTO);
        Scene updated = sceneRepository.save(scene);

        if (updateSceneDTO.getProducts() != null) {
            if (updated.getSceneProducts() != null) {
                updated.getSceneProducts().clear();
            }

            Set<SceneProduct> newItems = createSceneProducts(updated, updateSceneDTO.getProducts());
            if (updated.getSceneProducts() == null) {
                updated.setSceneProducts(new LinkedHashSet<>());
            }
            updated.getSceneProducts().addAll(newItems);
        }

        if (updateSceneDTO.getRoomModel() != null) {
            createOrUpdateRoomModel(id, updateSceneDTO.getRoomModel());
        }

        if (updateSceneDTO.getCoverings() != null) {
            sceneCoveringRepository.deleteBySceneId(id);
            for (CreateSceneCoveringDTO coveringDTO : updateSceneDTO.getCoverings()) {
                createOrUpdateCovering(id, coveringDTO);
            }
        }

        return toSceneDTO(updated);
    }

    /**
     * Deletes a scene and all its associated products, room model, and coverings.
     *
     * @param id the scene ID to delete
     * @throws IllegalArgumentException if the scene is not found
     */
    public void deleteScene(Long id) {
        if (!sceneRepository.existsById(id)) {
            throw new IllegalArgumentException("Scene not found with id: " + id);
        }
        sceneRepository.deleteById(id);
    }

    /**
     * Adds a product to a scene with position, rotation, scale, and color.
     *
     * @param sceneId    the scene ID
     * @param productDTO the product data including transformations
     * @return the added scene product as a DTO
     * @throws IllegalArgumentException if the scene or product is not found
     */
    @Transactional
    public SceneProductDTO addProductToScene(Long sceneId, CreateSceneProductDTO productDTO) {
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found with id: " + sceneId));

        SceneProduct sp = buildSceneProduct(scene, productDTO);
        SceneProduct saved = sceneProductRepository.save(sp);
        return toSceneProductDTO(saved);
    }

    /**
     * Removes a product from a scene.
     *
     * @param sceneId        the scene ID
     * @param sceneProductId the scene product ID to remove
     * @throws IllegalArgumentException if the scene product is not found or doesn't
     *                                  belong to the scene
     */
    @Transactional
    public void removeProductFromScene(Long sceneId, Long sceneProductId) {
        SceneProduct sp = sceneProductRepository.findById(sceneProductId)
                .orElseThrow(() -> new IllegalArgumentException("Scene product not found with id: " + sceneProductId));
        if (!Objects.equals(sp.getScene().getId(), sceneId)) {
            throw new IllegalArgumentException("Scene product does not belong to the specified scene");
        }
        sceneProductRepository.delete(sp);
    }

    /**
     * Updates a product's properties within a scene (color, position, rotation,
     * scale).
     *
     * @param sceneProductId the scene product ID to update
     * @param updateDTO      the updated product data
     * @return the updated scene product as a DTO
     * @throws IllegalArgumentException if the scene product is not found
     */
    @Transactional
    public SceneProductDTO updateSceneProduct(Long sceneProductId, CreateSceneProductDTO updateDTO) {
        SceneProduct sp = sceneProductRepository.findById(sceneProductId)
                .orElseThrow(() -> new IllegalArgumentException("Scene product not found with id: " + sceneProductId));

        applySceneProductUpdate(sp, updateDTO);
        SceneProduct saved = sceneProductRepository.save(sp);
        return toSceneProductDTO(saved);
    }

    /**
     * Converts a CreateSceneDTO to a Scene entity.
     * Automatically associates the scene with the currently authenticated user if
     * available.
     *
     * @param dto the scene creation data
     * @return the scene entity
     */
    private Scene fromCreateDTO(CreateSceneDTO dto) {
        Scene s = new Scene();
        s.setName(dto.getName());
        s.setDescription(dto.getDescription());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String userEmail = authentication.getName();
            User user = userService.findByEmail(userEmail).orElse(null);
            if (user != null) {
                s.setUserEntity(user);
            } else {
                s.setUser(dto.getUser() != null ? dto.getUser() : "guest");
            }
        } else {
            s.setUser(dto.getUser() != null ? dto.getUser() : "guest");
        }

        s.setSceneData(dto.getSceneData());
        s.setCameraPosition(dto.getCameraPosition());
        s.setLightingSettings(dto.getLightingSettings());
        s.setBackgroundColor(dto.getBackgroundColor());
        s.setIsPublic(Boolean.TRUE.equals(dto.getIsPublic()));
        return s;
    }

    /**
     * Applies updates from an UpdateSceneDTO to an existing Scene entity.
     * Only updates fields that are non-null in the DTO.
     *
     * @param s   the scene entity to update
     * @param dto the update data
     */
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

    /**
     * Creates a set of SceneProduct entities from DTOs.
     *
     * @param scene the scene to associate products with
     * @param items the list of product DTOs
     * @return set of SceneProduct entities
     */
    private Set<SceneProduct> createSceneProducts(Scene scene, List<CreateSceneProductDTO> items) {
        return items.stream()
                .map(dto -> buildSceneProduct(scene, dto))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Builds a SceneProduct entity from a DTO, resolving product and color
     * references.
     *
     * @param scene the scene to associate the product with
     * @param dto   the product data
     * @return the SceneProduct entity
     * @throws IllegalArgumentException if the product or color is not found
     */
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

    /**
     * Applies updates from a DTO to an existing SceneProduct entity.
     * Only updates fields that are non-null in the DTO.
     *
     * @param sp  the scene product entity to update
     * @param dto the update data
     */
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

    /**
     * Converts a Scene entity to a SceneDTO with all related products, room model,
     * and coverings.
     *
     * @param s the scene entity
     * @return the scene DTO
     */
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

        if (s.getRoomModel() != null) {
            dto.setRoomModel(toSceneRoomModelDTO(s.getRoomModel()));
        }

        if (s.getSceneCoverings() != null && !s.getSceneCoverings().isEmpty()) {
            dto.setSceneCoverings(
                    s.getSceneCoverings().stream()
                            .map(this::toSceneCoveringDTO)
                            .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * Converts a SceneProduct entity to a SceneProductDTO.
     *
     * @param sp the scene product entity
     * @return the scene product DTO
     */
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

    /**
     * Creates or updates the room model for a scene.
     * If a room model already exists for the scene, it is updated; otherwise, a new
     * one is created.
     *
     * @param sceneId      the scene ID
     * @param roomModelDTO the room model data including vertices and height
     * @return the created or updated room model as a DTO
     * @throws IllegalArgumentException if the scene is not found
     */
    @Transactional
    public SceneRoomModelDTO createOrUpdateRoomModel(Long sceneId, CreateSceneRoomModelDTO roomModelDTO) {
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found with id: " + sceneId));

        SceneRoomModel existingRoomModel = sceneRoomModelRepository.findBySceneId(sceneId);

        SceneRoomModel roomModel;
        if (existingRoomModel != null) {
            roomModel = existingRoomModel;
            roomModel.setVerticesData(roomModelDTO.getVerticesData());
            roomModel.setRoomHeight(roomModelDTO.getRoomHeight());
            roomModel.setModelType(roomModelDTO.getModelType());
            roomModel.setTemplatePath(roomModelDTO.getTemplatePath());
            roomModel.setRoomProperties(roomModelDTO.getRoomProperties());
        } else {
            roomModel = new SceneRoomModel(scene, roomModelDTO.getVerticesData(), roomModelDTO.getRoomHeight());
            roomModel.setModelType(roomModelDTO.getModelType());
            roomModel.setTemplatePath(roomModelDTO.getTemplatePath());
            roomModel.setRoomProperties(roomModelDTO.getRoomProperties());
        }

        SceneRoomModel saved = sceneRoomModelRepository.save(roomModel);
        return toSceneRoomModelDTO(saved);
    }

    /**
     * Retrieves the room model for a specific scene.
     *
     * @param sceneId the scene ID
     * @return the room model DTO, or null if no room model exists for the scene
     */
    @Transactional
    public SceneRoomModelDTO getRoomModelBySceneId(Long sceneId) {
        SceneRoomModel roomModel = sceneRoomModelRepository.findBySceneId(sceneId);
        return roomModel != null ? toSceneRoomModelDTO(roomModel) : null;
    }

    /**
     * Deletes the room model associated with a scene.
     *
     * @param sceneId the scene ID
     */
    @Transactional
    public void deleteRoomModel(Long sceneId) {
        sceneRoomModelRepository.deleteBySceneId(sceneId);
    }

    /**
     * Creates or updates a surface covering for a scene.
     * If a covering already exists for the specified surface identifier, it is
     * updated.
     *
     * @param sceneId     the scene ID
     * @param coveringDTO the covering data including product, surface type, and
     *                    repeat values
     * @return the created or updated covering as a DTO
     * @throws IllegalArgumentException if the scene or product is not found
     */
    @Transactional
    public SceneCoveringDTO createOrUpdateCovering(Long sceneId, CreateSceneCoveringDTO coveringDTO) {
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found with id: " + sceneId));

        Product product = productRepository.findById(coveringDTO.getProductId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Product not found with id: " + coveringDTO.getProductId()));

        SceneCovering existingCovering = null;
        if (coveringDTO.getSurfaceIdentifier() != null) {
            existingCovering = sceneCoveringRepository.findBySceneIdAndSurfaceIdentifier(sceneId,
                    coveringDTO.getSurfaceIdentifier());
        }

        SceneCovering covering;
        if (existingCovering != null) {
            covering = existingCovering;
            covering.setProduct(product);
            covering.setSurfaceType(coveringDTO.getSurfaceType());
            covering.setRepeatX(coveringDTO.getRepeatX());
            covering.setRepeatY(coveringDTO.getRepeatY());
            covering.setMaterialProperties(coveringDTO.getMaterialProperties());
        } else {
            covering = new SceneCovering(scene, product, coveringDTO.getSurfaceType(),
                    coveringDTO.getSurfaceIdentifier());
            covering.setRepeatX(coveringDTO.getRepeatX());
            covering.setRepeatY(coveringDTO.getRepeatY());
            covering.setMaterialProperties(coveringDTO.getMaterialProperties());
        }

        SceneCovering saved = sceneCoveringRepository.save(covering);
        return toSceneCoveringDTO(saved);
    }

    /**
     * Retrieves all surface coverings for a specific scene.
     *
     * @param sceneId the scene ID
     * @return list of coverings in the scene
     */
    public List<SceneCoveringDTO> getCoveringsBySceneId(Long sceneId) {
        List<SceneCovering> coverings = sceneCoveringRepository.findBySceneId(sceneId);
        return coverings.stream()
                .map(this::toSceneCoveringDTO)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a specific covering by its ID.
     *
     * @param coveringId the covering ID to delete
     */
    public void deleteCovering(Long coveringId) {
        sceneCoveringRepository.deleteById(coveringId);
    }

    /**
     * Deletes all coverings associated with a specific scene.
     *
     * @param sceneId the scene ID
     */
    public void deleteCoveringsBySceneId(Long sceneId) {
        sceneCoveringRepository.deleteBySceneId(sceneId);
    }

    /**
     * Converts a SceneRoomModel entity to a SceneRoomModelDTO.
     *
     * @param roomModel the room model entity
     * @return the room model DTO
     */
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

    /**
     * Converts a SceneCovering entity to a SceneCoveringDTO.
     *
     * @param covering the scene covering entity
     * @return the scene covering DTO
     */
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
