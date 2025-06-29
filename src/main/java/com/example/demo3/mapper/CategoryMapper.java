package com.example.demo3.mapper;

import com.example.demo3.dto.CategoryBasicDTO;
import com.example.demo3.dto.CategoryChildDTO;
import com.example.demo3.dto.CategoryCreateRequestDTO;
import com.example.demo3.dto.PageableResponseCategoryDTO;
import com.example.demo3.entity.CategoryEntity;
import com.example.demo3.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {
    private final CategoryRepository categoryRepository;

    public CategoryMapper(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryBasicDTO toDTO(CategoryEntity entity) {
        CategoryBasicDTO dto = new CategoryBasicDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setParent(entity.getParent() != null ? entity.getParent().getId() : null);
        if (entity.getChildren() != null) {
            List<CategoryChildDTO> children = entity.getChildren().stream()
                    .map(child -> {
                        CategoryChildDTO childDTO = new CategoryChildDTO();
                        childDTO.setId(child.getId());
                        childDTO.setName(child.getName());
                        return childDTO;
                    }).toList();
            dto.setChildren(children);
        }
        return dto;
    }

    public List<CategoryChildDTO> toChildrenDTOList(List<CategoryEntity> entities) {
        return entities.stream().map(child -> {
            CategoryChildDTO childDTO = new CategoryChildDTO();
            childDTO.setId(child.getId());
            childDTO.setName(child.getName());
            return childDTO;
        }).toList();
    }

    public List<CategoryBasicDTO> toDTOList(List<CategoryEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CategoryEntity requestToEntity(CategoryCreateRequestDTO request) {
        CategoryEntity entity = new CategoryEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setParent(request.getParent() != null
                ? categoryRepository.findById(request.getParent()).get()
                : null);
        return entity;
    }

    public PageableResponseCategoryDTO createNewPageableResponseCategoryDTO(Page<CategoryEntity> pageable) {
        PageableResponseCategoryDTO response = new PageableResponseCategoryDTO();
        response.setContent(this.toDTOList(pageable.getContent())
                .stream()
                .filter(category -> category.getParent() == null)
                .toList());
        response.setLast(pageable.isLast());
        response.setFirst(pageable.isFirst());
        response.setTotalPages(pageable.getTotalPages());
        response.setPageNumber(pageable.getPageable().getPageNumber());
        response.setPageSize(pageable.getPageable().getPageSize());
        response.setTotalElements(pageable.getTotalElements());
        return response;
    }
}
