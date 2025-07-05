package com.example.demo3.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryCreateRequestDTO {
    @NotBlank(message = "Category name cannot be blank or null")
    @Size(max = 100)
    private String name;
    private String description = null;
    private Long parent = null;

    public CategoryCreateRequestDTO() {
    }

    public CategoryCreateRequestDTO(String name,
                                    String description,
                                    Long parent) {
        this.name = name;
        this.description = description;
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "CategoryCreateRequestDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parent=" + parent +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }
}
