package com.bathforge.dto.admin;

/**
 * Data Transfer Object for updating quote request status and admin response.
 */
public class UpdateQuoteRequestDTO {
    /** The new status for the quote request */
    private String status;
    /** The admin's response to the quote request */
    private String adminResponse;

    public UpdateQuoteRequestDTO() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }
}
