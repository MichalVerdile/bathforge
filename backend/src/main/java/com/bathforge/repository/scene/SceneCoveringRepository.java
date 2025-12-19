package com.bathforge.repository.scene;

import com.bathforge.model.scene.SceneCovering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing SceneCovering entities.
 * Provides database access methods for scene covering operations.
 */
@Repository
public interface SceneCoveringRepository extends JpaRepository<SceneCovering, Long> {

    /**
     * Finds all coverings for a specific scene.
     *
     * @param sceneId the ID of the scene
     * @return list of scene coverings
     */
    List<SceneCovering> findBySceneId(Long sceneId);

    /**
     * Deletes all coverings associated with a specific scene.
     *
     * @param sceneId the ID of the scene
     */
    void deleteBySceneId(Long sceneId);

    /**
     * Finds coverings for a specific scene and surface type.
     *
     * @param sceneId     the ID of the scene
     * @param surfaceType the type of surface (e.g., wall, floor, ceiling)
     * @return list of scene coverings matching the criteria
     */
    List<SceneCovering> findBySceneIdAndSurfaceType(Long sceneId, String surfaceType);

    /**
     * Finds a covering for a specific scene and surface identifier.
     *
     * @param sceneId           the ID of the scene
     * @param surfaceIdentifier the unique identifier of the surface
     * @return the scene covering if found, null otherwise
     */
    SceneCovering findBySceneIdAndSurfaceIdentifier(Long sceneId, String surfaceIdentifier);
}