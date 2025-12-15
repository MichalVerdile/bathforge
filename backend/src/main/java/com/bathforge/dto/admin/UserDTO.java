package com.bathforge.dto.admin;

import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String company;
    private String phone;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;
    private int sceneCount;
    private int quoteRequestCount;

    public UserDTO() {
    }

    public UserDTO(Long id, String email, String firstName, String lastName, String company,
            String phone, String role, boolean enabled, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = company;
        this.phone = phone;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getSceneCount() {
        return sceneCount;
    }

    public void setSceneCount(int sceneCount) {
        this.sceneCount = sceneCount;
    }

    public int getQuoteRequestCount() {
        return quoteRequestCount;
    }

    public void setQuoteRequestCount(int quoteRequestCount) {
        this.quoteRequestCount = quoteRequestCount;
    }
}
