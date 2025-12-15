package com.bathforge.repository;

import com.bathforge.model.quote.QuoteRequestMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRequestMessageRepository extends JpaRepository<QuoteRequestMessage, Long> {
    List<QuoteRequestMessage> findByQuoteRequestIdOrderByCreatedAtAsc(Long quoteRequestId);
}
