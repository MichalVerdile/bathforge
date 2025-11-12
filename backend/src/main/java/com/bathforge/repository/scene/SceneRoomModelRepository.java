package com.bathforge.repository.scene;

import com.bathforge.model.scene.SceneRoomModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SceneRoomModelRepository extends JpaRepository<SceneRoomModel, Long> {

    SceneRoomModel findBySceneId(Long sceneId);

    void deleteBySceneId(Long sceneId);
}