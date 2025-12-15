package com.bathforge.dto.admin;

public class UpdateQuoteRequestDTO {
    private String status;
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
