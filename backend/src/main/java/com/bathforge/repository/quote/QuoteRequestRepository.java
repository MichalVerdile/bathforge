package com.bathforge.repository.quote;

import com.bathforge.model.quote.QuoteRequest;
import com.bathforge.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing QuoteRequest entities.
 * Provides database access methods for quote request operations.
 */
@Repository
public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {

    /**
     * Finds all quote requests for a specific user, ordered by creation date
     * descending.
     *
     * @param user the user whose quote requests to find
     * @return list of quote requests ordered by creation date (newest first)
     */
    List<QuoteRequest> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Finds all quote requests for a specific user ID, ordered by creation date
     * descending.
     *
     * @param userId the ID of the user whose quote requests to find
     * @return list of quote requests ordered by creation date (newest first)
     */
    List<QuoteRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
}
