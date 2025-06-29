package com.example.demo3.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CategoryBasicDTO {
    private Long id;
    private String name;
    private String description;
    private List<CategoryChildDTO> children;
    private Long parent;

    public CategoryBasicDTO() {
    }

    public CategoryBasicDTO(Long id,
                            String name,
                            String description,
                            List<CategoryChildDTO> children,
                            Long parent) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.children = children;
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

    public List<CategoryChildDTO> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryChildDTO> children) {
        this.children = children;
    }

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }
}
