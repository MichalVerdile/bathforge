package com.bathforge.dto.products;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ColorDTO {

    private Long id;

    @NotBlank(message = "Color name is required")
    private String name;

    @NotBlank(message = "Hex code is required")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid hex color code")
    private String hexCode;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String categoryName;

    public ColorDTO() {
    }

    public ColorDTO(String name, String hexCode, Long categoryId) {
        this.name = name;
        this.hexCode = hexCode;
        this.categoryId = categoryId;
    }

    public ColorDTO(Long id, String name, String hexCode, Long categoryId, String categoryName) {
        this.id = id;
        this.name = name;
        this.hexCode = hexCode;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHexCode() {
        return hexCode;
    }

    public void setHexCode(String hexCode) {
        this.hexCode = hexCode;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return "ColorDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", hexCode='" + hexCode + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}