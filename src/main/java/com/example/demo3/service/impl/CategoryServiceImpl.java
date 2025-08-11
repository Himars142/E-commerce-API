package com.example.demo3.service.impl;

import com.example.demo3.dto.CategoryCreateRequestDTO;
import com.example.demo3.dto.CategoryDTO;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static com.example.demo3.utill.GenerateRequestID.generateRequestID;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }


    @Override
    public PageableResponseCategoryDTO getAllCategories(int page, int size, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to get all categories pageable request id: {}, user agent: {}, page {}, size: {}",
                requestId, userAgent, page, size);
        Page<CategoryEntity> pageable = categoryRepository.findAll(PageRequest.of(page, size));
        PageableResponseCategoryDTO response = categoryMapper.createNewPageableResponseCategoryDTO(pageable);
        logger.info("Success get all categories pageable. Total elements: {}, total pages: {}, request id: {}",
                response.getTotalElements(), response.getTotalPages(), requestId);
        return response;
    }

    @Override
    public CategoryEntity getCategory(Long id, String requestId) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found. Category ID:" + id
                        + ". Request id:" + requestId));
    }

    @Override
    public CategoryDTO getCategoryById(Long id, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to get category by id: {}, request id: {}, userAgent: {}", id, requestId, userAgent);
        CategoryDTO categoryDTO = categoryMapper.createCategoryDTO(getCategory(id, requestId));
        logger.info("Success category retried with id: {}, request id: {}", categoryDTO.getId(), requestId);
        return categoryDTO;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public Long createCategory(CategoryCreateRequestDTO request, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to create category request id: {}, user agent: {}, category: {}",
                requestId, userAgent, request.toString());
        if (request.getParent() != null) {
            categoryRepository.findById(request.getParent())
                    .orElseThrow(() -> new NotFoundException("Parent category not found ID: " + request.getParent()
                            + ". Request id: " + requestId));
        }
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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public void deleteCategoryById(Long id, String userAgent) {
        String requestId = generateRequestID();
        logger.info("Attempt to delete category by id: {}, request id: {}, userAgent: {}", id, requestId, userAgent);
        CategoryEntity category = getCategory(id, requestId);
        categoryRepository.delete(category);
        logger.info("Success category deleted request id: {}, category id:{}", requestId, category.getId());
    }
}