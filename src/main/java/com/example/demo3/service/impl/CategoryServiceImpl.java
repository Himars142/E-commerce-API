package com.example.demo3.service.impl;

import com.example.demo3.dto.CategoryCreateRequestDTO;
import com.example.demo3.dto.PageableResponseCategoryDTO;
import com.example.demo3.entity.CategoryEntity;
import com.example.demo3.exception.NotFoundException;
import com.example.demo3.mapper.CategoryMapper;
import com.example.demo3.repository.CategoryRepository;
import com.example.demo3.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final AuthServiceImpl authService;

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               CategoryMapper categoryMapper,
                               AuthServiceImpl authService) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.authService = authService;
    }


    @Override
    public PageableResponseCategoryDTO getAllCategories(int page, int size) {
        Page<CategoryEntity> pageable = categoryRepository.findAll(PageRequest.of(page, size));
        PageableResponseCategoryDTO response = categoryMapper.createNewPageableResponseCategoryDTO(pageable);
        logger.info("Success get all categories pageable. Total elements: {}, total pages: {}",
                response.getTotalElements(), response.getTotalPages());
        return response;
    }

    @Override
    public CategoryEntity getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found. Category ID:" + id));
    }

    @Override
    public void createCategory(String token, CategoryCreateRequestDTO request) {
        authService.checkIsUserAdmin(token);
        CategoryEntity category = categoryRepository.save(categoryMapper.requestToEntity(request));
        logger.info("Success category created id:{}", category.getId());
    }

    @Override
    public void checkIfCategoryExist(Long id) {
        if (categoryRepository.existsById(id)) {
            throw new NotFoundException("Category not found with id:" + id);
        }
    }
}
