package com.example.demo3.service;

import com.example.demo3.dto.CategoryCreateRequestDTO;
import com.example.demo3.dto.PageableResponseCategoryDTO;
import com.example.demo3.entity.CategoryEntity;

public interface CategoryService {
    PageableResponseCategoryDTO getAllCategories(int page, int size);

    CategoryEntity getCategoryById(Long id);

    void createCategory(String token, CategoryCreateRequestDTO request);

    void checkIfCategoryExist(Long id);
}
