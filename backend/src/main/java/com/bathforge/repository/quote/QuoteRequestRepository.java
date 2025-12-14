package com.bathforge.repository.quote;

import com.bathforge.model.quote.QuoteRequest;
import com.bathforge.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {
    List<QuoteRequest> findByUserOrderByCreatedAtDesc(User user);

    List<QuoteRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
}
