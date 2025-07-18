package com.example.demo3.service.impl;

import com.example.demo3.dto.CategoryCreateRequestDTO;
import com.example.demo3.dto.PageableResponseCategoryDTO;
import com.example.demo3.entity.CategoryEntity;
import com.example.demo3.exception.NotFoundException;
import com.example.demo3.mapper.CategoryMapper;
import com.example.demo3.repository.CategoryRepository;
import com.example.demo3.testutil.BaseServiceTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CategoryServiceImplTest extends BaseServiceTest {

    @Mock
    private AuthServiceImpl authService;
    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl underTest;

    private static final CategoryEntity CATEGORY = new CategoryEntity();

    @BeforeAll
    static void setUp() {
        CATEGORY.setId(1L);
    }

    @Nested
    @DisplayName("Get all categories tests")
    class GetAllCategories {

        @Test
        @DisplayName("Should return pageable response")
        void getAllCategories() {
            Page<CategoryEntity> pageable = Page.empty();
            PageableResponseCategoryDTO response = new PageableResponseCategoryDTO();

            when(categoryRepository.findAll(PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE))).thenReturn(pageable);
            when(categoryMapper.createNewPageableResponseCategoryDTO(pageable)).thenReturn(response);

            PageableResponseCategoryDTO result = underTest.getAllCategories(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, USER_AGENT);

            assertThat(result)
                    .isNotNull()
                    .isEqualTo(response);

            InOrder inOrder = inOrder(categoryRepository, categoryMapper);
            inOrder.verify(categoryRepository).findAll(PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE));
            inOrder.verify(categoryMapper).createNewPageableResponseCategoryDTO(pageable);
        }
    }

    @Nested
    @DisplayName("Get category by id tests")
    class GetCategoryById {

        @Test
        @DisplayName("Should return category")
        void getCategoryById_ShouldReturnCategory() {
            when(categoryRepository.findById(CATEGORY.getId())).thenReturn(Optional.of(CATEGORY));

            CategoryEntity result = underTest.getCategoryById(CATEGORY.getId(), requestId);

            assertThat(result)
                    .isNotNull()
                    .extracting(CategoryEntity::getId)
                    .isEqualTo(1L);

            verify(categoryRepository).findById(CATEGORY.getId());
        }

        @Test
        @DisplayName("Should throw not found exception")
        void getCategoryById_ShouldThrowNotFoundException() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> underTest.getCategoryById(1L, requestId));

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains(requestId);

            verify(categoryRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("Create category tests")
    class CreateCategory {

        @Test
        @DisplayName("Should create category")
        void createCategory() {
            CategoryCreateRequestDTO requestDTO = new CategoryCreateRequestDTO();

            when(categoryRepository.save(CATEGORY)).thenReturn(CATEGORY);
            when(categoryMapper.requestToEntity(requestDTO)).thenReturn(CATEGORY);

            underTest.createCategory(TOKEN, requestDTO, USER_AGENT);

            InOrder inOrder = inOrder(authService, categoryMapper, categoryRepository);
            inOrder.verify(authService).checkIsUserAdmin(eq(TOKEN), anyString());
            inOrder.verify(categoryMapper).requestToEntity(requestDTO);
            inOrder.verify(categoryRepository).save(CATEGORY);
        }
    }

    @Nested
    @DisplayName("Check if category exist")
    class CheckIfCategoryExist {

        @Test
        @DisplayName("Should return void")
        void checkIfCategoryExist_ShouldReturnVoid() {
            when(categoryRepository.existsById(1L)).thenReturn(true);

            underTest.checkIfCategoryExist(1L, requestId);

            verify(categoryRepository).existsById(1L);
        }

        @Test
        @DisplayName("Should throw not found exception")
        void checkIfCategoryExist_ShouldThrowNotFoundException() {
            when(categoryRepository.existsById(1L)).thenReturn(false);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> underTest.checkIfCategoryExist(1L, requestId));

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains(requestId);

            verify(categoryRepository).existsById(1L);
        }
    }
}