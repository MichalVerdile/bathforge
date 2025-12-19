package com.bathforge.model.quote;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a message in a quote request conversation.
 * Used for communication between admins and the system regarding quote
 * requests.
 */
@Entity
@Table(name = "quote_request_messages")
public class QuoteRequestMessage {

    /** The unique identifier of the message */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The quote request this message belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_request_id", nullable = false)
    private QuoteRequest quoteRequest;

    /** The content of the message */
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    /** The type of sender (ADMIN or SYSTEM) */
    @Column(name = "sender_type", nullable = false)
    private String senderType;

    /** Timestamp when the message was created */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Lifecycle callback executed before persisting a new message.
     * Sets the creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Default constructor.
     */
    public QuoteRequestMessage() {
    }

    /**
     * Constructs a QuoteRequestMessage with all required fields.
     *
     * @param quoteRequest the quote request this message belongs to
     * @param message      the message content
     * @param senderType   the type of sender (ADMIN or SYSTEM)
     */
    public QuoteRequestMessage(QuoteRequest quoteRequest, String message, String senderType) {
        this.quoteRequest = quoteRequest;
        this.message = message;
        this.senderType = senderType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuoteRequest getQuoteRequest() {
        return quoteRequest;
    }

    public void setQuoteRequest(QuoteRequest quoteRequest) {
        this.quoteRequest = quoteRequest;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
