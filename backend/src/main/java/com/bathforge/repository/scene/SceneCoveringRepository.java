package com.bathforge.repository.scene;

import com.bathforge.model.scene.SceneCovering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneCoveringRepository extends JpaRepository<SceneCovering, Long> {

    List<SceneCovering> findBySceneId(Long sceneId);

    void deleteBySceneId(Long sceneId);

    List<SceneCovering> findBySceneIdAndSurfaceType(Long sceneId, String surfaceType);

    SceneCovering findBySceneIdAndSurfaceIdentifier(Long sceneId, String surfaceIdentifier);
}