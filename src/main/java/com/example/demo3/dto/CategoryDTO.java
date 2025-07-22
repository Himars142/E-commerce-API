package com.example.demo3.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    @JsonFormat(pattern = "dd MMM yyyy HH:mm", locale = "uk")
    private LocalDateTime createdAt;
    private CategoryDTO parent;

    public CategoryDTO() {
    }

    public CategoryDTO(Long id, String name, String description, LocalDateTime createdAt, CategoryDTO parent) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.parent = parent;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public CategoryDTO getParent() {
        return parent;
    }

    public void setParent(CategoryDTO parent) {
        this.parent = parent;
    }
}
