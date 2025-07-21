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

import java.util.UUID;

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
    public PageableResponseCategoryDTO getAllCategories(int page, int size, String userAgent) {
        String requestId = UUID.randomUUID().toString();
        logger.info("Attempt to get all categories pageable request id: {}, user agent: {}, page {}, size: {}",
                requestId, userAgent, page, size);
        Page<CategoryEntity> pageable = categoryRepository.findAll(PageRequest.of(page, size));
        PageableResponseCategoryDTO response = categoryMapper.createNewPageableResponseCategoryDTO(pageable);
        logger.info("Success get all categories pageable. Total elements: {}, total pages: {}, request id: {}",
                response.getTotalElements(), response.getTotalPages(), requestId);
        return response;
    }

    @Override
    public CategoryEntity getCategoryById(Long id, String requestId) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found. Category ID:" + id + ". Request id:" + requestId));
    }

    @Override
    public Long createCategory(String token, CategoryCreateRequestDTO request, String userAgent) {
        String requestId = UUID.randomUUID().toString();
        logger.info("Attempt to create category request id: {}, user agent: {}, category: {}", requestId, userAgent, request.toString());
        authService.checkIsUserAdmin(token, requestId);
        CategoryEntity category = categoryRepository.save(categoryMapper.requestToEntity(request));
        logger.info("Success category created request id: {}, category id:{}", requestId, category.getId());
        return category.getId();
    }

    @Override
    public void checkIfCategoryExist(Long id, String requestId) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category not found with id:" + id + ". Request id:" + requestId);
        }
    }
}
