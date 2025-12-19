package com.bathforge.repository.scene;

import com.bathforge.model.scene.SceneRoomModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing SceneRoomModel entities.
 * Provides database access methods for scene room model operations.
 */
@Repository
public interface SceneRoomModelRepository extends JpaRepository<SceneRoomModel, Long> {

    /**
     * Finds the room model for a specific scene.
     *
     * @param sceneId the ID of the scene
     * @return the scene room model if found, null otherwise
     */
    SceneRoomModel findBySceneId(Long sceneId);

    /**
     * Deletes the room model associated with a specific scene.
     *
     * @param sceneId the ID of the scene
     */
    void deleteBySceneId(Long sceneId);
}