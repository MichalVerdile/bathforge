package com.bathforge.model.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entity representing a user in the system.
 * Users can have different roles (customer, admin, industry) and own bathroom
 * scenes and quote requests.
 */
@Entity
@Table(name = "users")
public class User {

    /** The unique identifier of the user */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user's email address (unique) */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    /** The user's hashed password */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(nullable = false)
    private String password;

    /** The user's first name */
    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    /** The user's last name */
    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    /** Optional company name */
    @Column(nullable = true)
    private String company;

    /** Optional phone number */
    @Column(nullable = true)
    private String phone;

    /** Timestamp when the user account was created */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** Whether the user account is enabled */
    @Column(nullable = false)
    private boolean enabled = true;

    /** The role of the user (CUSTOMER, ADMIN, or INDUSTRY) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.CUSTOMER;

    /**
     * Lifecycle callback executed before persisting a new user.
     * Sets the creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * Constructs a User with required fields.
     *
     * @param email     the user's email address
     * @param password  the user's password (should be hashed before storing)
     * @param firstName the user's first name
     * @param lastName  the user's last name
     */
    public User(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
