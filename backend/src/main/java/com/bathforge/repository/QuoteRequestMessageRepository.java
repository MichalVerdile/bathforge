package com.bathforge.repository;

import com.bathforge.model.quote.QuoteRequestMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing QuoteRequestMessage entities.
 * Provides database access methods for quote request message operations.
 */
@Repository
public interface QuoteRequestMessageRepository extends JpaRepository<QuoteRequestMessage, Long> {

    /**
     * Finds all messages for a specific quote request, ordered by creation date
     * ascending.
     *
     * @param quoteRequestId the ID of the quote request
     * @return list of messages ordered by creation date (oldest first)
     */
    List<QuoteRequestMessage> findByQuoteRequestIdOrderByCreatedAtAsc(Long quoteRequestId);
}
