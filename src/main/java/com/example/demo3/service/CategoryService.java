package com.example.demo3.service;

import com.example.demo3.dto.CategoryCreateRequestDTO;
import com.example.demo3.dto.CategoryDTO;
import com.example.demo3.dto.PageableResponseCategoryDTO;
import com.example.demo3.entity.CategoryEntity;

public interface CategoryService {
    PageableResponseCategoryDTO getAllCategories(int page, int size, String userAgent);

    CategoryEntity getCategory(Long id, String requestId);

    CategoryDTO getCategoryById(Long id, String userAgent);

    Long createCategory(String token, CategoryCreateRequestDTO request, String userAgent);

    void checkIfCategoryExist(Long id, String requestId);
}
