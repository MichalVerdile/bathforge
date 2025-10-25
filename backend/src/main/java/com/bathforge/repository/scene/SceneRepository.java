package com.bathforge.repository.scene;

import com.bathforge.model.scene.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {

    /**
     * Find scenes by user
     */
    List<Scene> findByUsername(String user);

    /**
     * Find public scenes
     */
    List<Scene> findByIsPublicTrue();

    /**
     * Find scenes by user and public status
     */
    List<Scene> findByUsernameAndIsPublic(String user, Boolean isPublic);

    /**
     * Find scenes by name containing (case insensitive)
     */
    List<Scene> findByNameContainingIgnoreCase(String name);

    /**
     * Find scenes created after a certain date
     */
    List<Scene> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find scenes updated after a certain date
     */
    List<Scene> findByUpdatedAtAfter(LocalDateTime date);

    /**
     * Find scenes by user ordered by creation date descending
     */
    List<Scene> findByUsernameOrderByCreatedAtDesc(String user);

    /**
     * Find scenes by user ordered by update date descending
     */
    List<Scene> findByUsernameOrderByUpdatedAtDesc(String user);

    /**
     * Find public scenes ordered by creation date descending
     */
    List<Scene> findByIsPublicTrueOrderByCreatedAtDesc();

    /**
     * Search scenes by name or description containing text (case insensitive)
     */
    @Query("SELECT s FROM Scene s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Scene> searchByNameOrDescription(@Param("searchText") String searchText);

    /**
     * Find scenes by username with pagination-like functionality
     */
    @Query("SELECT s FROM Scene s WHERE s.username = :username ORDER BY s.updatedAt DESC")
    List<Scene> findRecentScenesByUser(@Param("username") String user);

    /**
     * Count scenes by user
     */
    long countByUsername(String user);

    /**
     * Count public scenes
     */
    long countByIsPublicTrue();
}