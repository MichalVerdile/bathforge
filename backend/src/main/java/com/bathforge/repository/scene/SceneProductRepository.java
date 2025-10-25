package com.bathforge.repository.scene;

import com.bathforge.model.scene.SceneProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneProductRepository extends JpaRepository<SceneProduct, Long> {

    /**
     * Find scene products by scene ID
     */
    List<SceneProduct> findBySceneId(Long sceneId);

    /**
     * Find scene products by product ID
     */
    List<SceneProduct> findByProductId(Long productId);

    /**
     * Find scene products by color ID
     */
    List<SceneProduct> findByColorId(Long colorId);

    /**
     * Find scene products by scene ID and product ID
     */
    List<SceneProduct> findBySceneIdAndProductId(Long sceneId, Long productId);

    /**
     * Delete all scene products by scene ID
     */
    void deleteBySceneId(Long sceneId);

    /**
     * Count products in a scene
     */
    long countBySceneId(Long sceneId);

    /**
     * Find scenes that contain a specific product
     */
    @Query("SELECT sp FROM SceneProduct sp WHERE sp.product.id = :productId")
    List<SceneProduct> findScenesContainingProduct(@Param("productId") Long productId);

    /**
     * Find products in scenes with specific color
     */
    @Query("SELECT sp FROM SceneProduct sp WHERE sp.color.id = :colorId")
    List<SceneProduct> findProductsWithColor(@Param("colorId") Long colorId);

    /**
     * Get scene products with product and color details
     */
    @Query("SELECT sp FROM SceneProduct sp " +
            "JOIN FETCH sp.product p " +
            "LEFT JOIN FETCH sp.color c " +
            "WHERE sp.scene.id = :sceneId")
    List<SceneProduct> findBySceneIdWithDetails(@Param("sceneId") Long sceneId);

    /**
     * Find scene products by scene and within position range
     */
    @Query("SELECT sp FROM SceneProduct sp WHERE sp.scene.id = :sceneId " +
            "AND sp.positionX BETWEEN :minX AND :maxX " +
            "AND sp.positionY BETWEEN :minY AND :maxY " +
            "AND sp.positionZ BETWEEN :minZ AND :maxZ")
    List<SceneProduct> findBySceneIdAndPositionRange(
            @Param("sceneId") Long sceneId,
            @Param("minX") Double minX, @Param("maxX") Double maxX,
            @Param("minY") Double minY, @Param("maxY") Double maxY,
            @Param("minZ") Double minZ, @Param("maxZ") Double maxZ);
}