package com.example.demo3.service.impl;

import com.example.demo3.dto.CategoryCreateRequestDTO;
import com.example.demo3.dto.PageableResponseCategoryDTO;
import com.example.demo3.entity.CategoryEntity;
import com.example.demo3.entity.ProductEntity;
import com.example.demo3.exception.NotFoundException;
import com.example.demo3.mapper.CategoryMapper;
import com.example.demo3.repository.CategoryRepository;
import com.example.demo3.testutil.BaseServiceTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryServiceImplTest extends BaseServiceTest {
    @Mock
    private AuthServiceImpl authService;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl underTest;

    @Test
    void getAllCategories() {
        String userAgent = "testing";
        int page = 0;
        int size = 10;

        Page<CategoryEntity> pageable = Page.empty();
        PageableResponseCategoryDTO response = new PageableResponseCategoryDTO();

        when(categoryRepository.findAll(PageRequest.of(page, size))).thenReturn(pageable);
        when(categoryMapper.createNewPageableResponseCategoryDTO(pageable)).thenReturn(response);

        underTest.getAllCategories(page, size, userAgent);

        verify(categoryRepository).findAll(PageRequest.of(page, size));
        verify(categoryMapper).createNewPageableResponseCategoryDTO(pageable);
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {
        String requestId = UUID.randomUUID().toString();
        CategoryEntity category = new CategoryEntity();
        category.setId(1L);

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        underTest.getCategoryById(category.getId(), requestId);

        verify(categoryRepository).findById(category.getId());
    }

    @Test
    void getCategoryById_ShouldThrowNotFoundException() {
        String requestId = UUID.randomUUID().toString();

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.getCategoryById(1L, requestId));

        verify(categoryRepository).findById(1L);
    }

    @Test
    void createCategory() {
        String userAgent = "testing";
        String token = "valid-token";

        CategoryCreateRequestDTO requestDTO = new CategoryCreateRequestDTO();
        CategoryEntity category = new CategoryEntity();

        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.requestToEntity(requestDTO)).thenReturn(category);

        underTest.createCategory(token, requestDTO, userAgent);

        verify(categoryRepository).save(category);
        verify(categoryMapper).requestToEntity(requestDTO);
    }

    @Test
    void checkIfCategoryExist_ShouldReturnVoid() {
        String requestId = UUID.randomUUID().toString();

        when(categoryRepository.existsById(1L)).thenReturn(true);

        underTest.checkIfCategoryExist(1L, requestId);

        verify(categoryRepository).existsById(1L);
    }

    @Test
    void checkIfCategoryExist_ShouldThrowNotFoundException() {
        String requestId = UUID.randomUUID().toString();

        when(categoryRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> underTest.checkIfCategoryExist(1L, requestId));

        verify(categoryRepository).existsById(1L);
    }
}