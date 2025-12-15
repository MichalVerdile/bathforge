package com.bathforge.service.user;

import com.bathforge.dto.QuoteRequestDetailDTO;
import com.bathforge.dto.QuoteRequestMessageDTO;
import com.bathforge.model.quote.QuoteRequest;
import com.bathforge.model.quote.QuoteRequestMessage;
import com.bathforge.model.user.User;
import com.bathforge.repository.QuoteRequestMessageRepository;
import com.bathforge.repository.quote.QuoteRequestRepository;
import com.bathforge.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuoteRequestRepository quoteRequestRepository;
    private final QuoteRequestMessageRepository quoteRequestMessageRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            QuoteRequestRepository quoteRequestRepository,
            QuoteRequestMessageRepository quoteRequestMessageRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.quoteRequestRepository = quoteRequestRepository;
        this.quoteRequestMessageRepository = quoteRequestMessageRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public QuoteRequestDetailDTO getQuoteRequestDetail(Long quoteRequestId, Long userId) {
        QuoteRequest quoteRequest = quoteRequestRepository.findById(quoteRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Quote request not found"));

        // Verify the quote request belongs to the user
        if (!quoteRequest.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to quote request");
        }

        QuoteRequestDetailDTO dto = new QuoteRequestDetailDTO();
        dto.setId(quoteRequest.getId());
        dto.setStatus(quoteRequest.getStatus());
        dto.setRoomDimensions(quoteRequest.getRoomDimensions());
        dto.setAdditionalNotes(quoteRequest.getAdditionalNotes());
        dto.setSceneSnapshot(quoteRequest.getSceneSnapshot());
        dto.setCreatedAt(quoteRequest.getCreatedAt());
        dto.setUpdatedAt(quoteRequest.getUpdatedAt());
        dto.setDocumentUrl(quoteRequest.getDocumentUrl());

        // Get all messages for this quote request
        List<QuoteRequestMessage> messages = quoteRequestMessageRepository
                .findByQuoteRequestIdOrderByCreatedAtAsc(quoteRequestId);

        dto.setMessages(messages.stream()
                .map(msg -> new QuoteRequestMessageDTO(
                        msg.getId(),
                        msg.getMessage(),
                        msg.getSenderType(),
                        msg.getCreatedAt()))
                .collect(Collectors.toList()));

        return dto;
    }
}
