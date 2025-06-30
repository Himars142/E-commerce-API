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
        logger.info("Attempt to get all categories pageable. Page: {}, size: {}", page, size);
        Page<CategoryEntity> pageable = categoryRepository.findAll(PageRequest.of(page, size));
        return categoryMapper.createNewPageableResponseCategoryDTO(pageable);
    }

    @Override
    public CategoryEntity getCategoryById(Long id) {
        logger.debug("Attempt to get category by id: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found. Category ID:" + id));
    }

    @Override
    public void createCategory(String token, CategoryCreateRequestDTO request) {
        logger.info("Attempt to create category");
        authService.checkIsUserAdmin(token);
        categoryRepository.save(categoryMapper.requestToEntity(request));
        logger.info("Success category created");
    }

    @Override
    public void checkIfCategoryExist(Long id) {
        logger.debug("Attempt to check if category exist with id:{}", id);
        if (categoryRepository.existsById(id)) {
            throw new NotFoundException("Category not found with id:" + id);
        }
        logger.debug("Success category exist with id:{}", id);
    }
}
