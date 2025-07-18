package com.example.demo3.service.impl;

import com.example.demo3.dto.*;
import com.example.demo3.entity.CartItemEntity;
import com.example.demo3.entity.CategoryEntity;
import com.example.demo3.entity.OrderItemEntity;
import com.example.demo3.entity.ProductEntity;
import com.example.demo3.exception.BadRequestException;
import com.example.demo3.exception.NotFoundException;
import com.example.demo3.mapper.ProductMapper;
import com.example.demo3.repository.ProductsRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductServiceImplTest extends BaseServiceTest {

    @Mock
    private ProductsRepository productsRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private CategoryServiceImpl categoryService;
    @Mock
    private AuthServiceImpl authService;

    @InjectMocks
    private ProductServiceImpl underTest;

    private static final ProductEntity PRODUCT = new ProductEntity();
    private static final Page<ProductEntity> EMPTY_PAGE = Page.empty();
    private static final CartItemEntity CART_ITEM_ENTITY = new CartItemEntity();
    private static final CategoryEntity CATEGORY = new CategoryEntity();
    private static final int PRODUCT_STOCK_QUANTITY = 10;
    private static final int INVALID_PRODUCT_STOCK_QUANTITY = 0;

    @BeforeAll
    static void setUp() {
        PRODUCT.setId(EXISTING_ENTITY_ID);
        PRODUCT.setStockQuantity(PRODUCT_STOCK_QUANTITY);
        PRODUCT.setSku("PRODUCT-SKU");
        CART_ITEM_ENTITY.setProduct(PRODUCT);
        CART_ITEM_ENTITY.setQuantity(2);
        CATEGORY.setName("TEST-CATEGORY");
        CATEGORY.setId(EXISTING_ENTITY_ID);
    }

    @Nested
    @DisplayName("Get product by id tests")
    class GetProductById {

        private static final ProductBasicDTO RESPONSE = new ProductBasicDTO();

        @Test
        @DisplayName("Should return product DTO")
        void getProductById_ShouldReturnProductDTO() {
            when(productsRepository.findById(PRODUCT.getId())).thenReturn(Optional.of(PRODUCT));
            when(productMapper.toDTO(PRODUCT)).thenReturn(RESPONSE);

            ProductBasicDTO result = underTest.getProductById(PRODUCT.getId(), USER_AGENT);

            assertThat(result).isNotNull().isSameAs(RESPONSE);

            InOrder inOrder = inOrder(productsRepository, productMapper);
            inOrder.verify(productsRepository).findById(PRODUCT.getId());
            inOrder.verify(productMapper).toDTO(PRODUCT);
        }

        @Test
        @DisplayName("Should throw not found exception")
        void getProductById_ShouldThrowNotFoundException() {
            when(productsRepository.findById(NOT_EXISTING_ENTITY_ID)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> underTest.getProductById(NOT_EXISTING_ENTITY_ID, USER_AGENT));

            verify(productsRepository).findById(NOT_EXISTING_ENTITY_ID);
        }
    }

    @Nested
    @DisplayName("Get all products tests")
    class GetAllProducts {

        @Test
        @DisplayName("Should return pageable response products")
        void getAllProducts_ShouldReturnPageableResponseProducts() {
            when(productsRepository.findAll(PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE))).thenReturn(EMPTY_PAGE);
            when(productMapper.createPageableResponseProducts(EMPTY_PAGE)).thenReturn(new PageableResponseProducts());

            underTest.getAllProducts(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, USER_AGENT);

            InOrder inOrder = inOrder(productsRepository, productMapper);
            inOrder.verify(productsRepository).findAll(PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE));
            inOrder.verify(productMapper).createPageableResponseProducts(EMPTY_PAGE);
        }
    }

    @Nested
    @DisplayName("Add product tests")
    class AddProduct {

        private static final ProductRequestDTO REQUEST = new ProductRequestDTO();
        private static final ProductEntity ADDED_PRODUCT = new ProductEntity();

        @BeforeAll
        static void setUp() {
            REQUEST.setCategoryId(EXISTING_ENTITY_ID);
            REQUEST.setSku("TEST-SKU");
            REQUEST.setName("TEST-ADD-PRODUCT-REQUEST");
            REQUEST.setPrice(BigDecimal.valueOf(12.13));
            REQUEST.setStockQuantity(PRODUCT_STOCK_QUANTITY);
            REQUEST.setDescription("TEST-DESCRIPTION");
        }

        @Test
        @DisplayName("Should throw bad request exception sku must be unique")
        void addProduct_ShouldThrowBadRequestExceptionSkuMustBeUnique() {
            when(categoryService.getCategoryById(any(), anyString())).thenReturn(new CategoryEntity());
            when(productsRepository.findBySku(REQUEST.getSku())).thenReturn(Optional.of(new ProductEntity()));

            assertThrows(BadRequestException.class, () -> underTest.addProduct(TOKEN, REQUEST, USER_AGENT));

            verify(productsRepository).findBySku(REQUEST.getSku());
            verify(productsRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should add product")
        void addProduct_ShouldAddProduct() {
            when(categoryService.getCategoryById(any(), anyString())).thenReturn(CATEGORY);
            when(productsRepository.findBySku(REQUEST.getSku())).thenReturn(Optional.empty());
            when(productsRepository.save(ADDED_PRODUCT)).thenReturn(ADDED_PRODUCT);
            when(productMapper.createProduct(REQUEST, CATEGORY)).thenReturn(ADDED_PRODUCT);

            underTest.addProduct(TOKEN, REQUEST, USER_AGENT);

            InOrder inOrder = inOrder(authService, productsRepository, productMapper, productsRepository);
            inOrder.verify(authService).checkIsUserAdmin(eq(TOKEN), anyString());
            inOrder.verify(productsRepository).findBySku(REQUEST.getSku());
            inOrder.verify(productMapper).createProduct(REQUEST, CATEGORY);
            inOrder.verify(productsRepository).save(ADDED_PRODUCT);
        }
    }

    @Nested
    @DisplayName("Update product tests")
    class UpdateProduct {

        private static final UpdateProductRequestDTO REQUEST = new UpdateProductRequestDTO();

        @BeforeAll
        static void setUp() {
            REQUEST.setId(EXISTING_ENTITY_ID);
            REQUEST.setCategoryId(EXISTING_ENTITY_ID);
            REQUEST.setSku("TEST-SKU");
        }

        @Test
        @DisplayName("Should throw not found exception product not found")
        void updateProduct_ShouldThrowNotFoundExceptionProductNotFound() {
            when(productsRepository.findById(REQUEST.getId())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> underTest.updateProduct(TOKEN, REQUEST, USER_AGENT));

            InOrder inOrder = inOrder(authService, productsRepository);
            inOrder.verify(authService).checkIsUserAdmin(eq(TOKEN), anyString());
            inOrder.verify(productsRepository).findById(REQUEST.getId());
        }

        @Test
        @DisplayName("Should throw bad request exception")
        void updateProduct_ShouldThrowBadRequestException() {
            UpdateProductRequestDTO request = new UpdateProductRequestDTO();
            request.setId(EXISTING_ENTITY_ID);
            request.setSku("SKU-TAKEN-BY-ANOTHER-PRODUCT");

            ProductEntity productToUpdate = new ProductEntity();
            productToUpdate.setId(EXISTING_ENTITY_ID);
            productToUpdate.setSku("ORIGINAL-SKU");

            ProductEntity otherProductWithSku = new ProductEntity();
            otherProductWithSku.setId(ANOTHER_EXISTING_ENTITY_ID);
            otherProductWithSku.setSku("SKU-TAKEN-BY-ANOTHER-PRODUCT");

            when(productsRepository.findById(EXISTING_ENTITY_ID)).thenReturn(Optional.of(productToUpdate));
            when(productsRepository.findBySku(request.getSku())).thenReturn(Optional.of(otherProductWithSku));

            assertThrows(BadRequestException.class, () -> underTest.updateProduct(TOKEN, request, USER_AGENT));

            InOrder inOrder = inOrder(authService, productsRepository);
            inOrder.verify(authService).checkIsUserAdmin(eq(TOKEN), anyString());
            inOrder.verify(productsRepository).findById(EXISTING_ENTITY_ID);
            inOrder.verify(productsRepository).findBySku(request.getSku());
            inOrder.verify(productsRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update product")
        void updateProduct_ShouldUpdateProduct() {
            when(productsRepository.findById(REQUEST.getId())).thenReturn(Optional.of(PRODUCT));
            when(categoryService.getCategoryById(eq(REQUEST.getCategoryId()), anyString())).thenReturn(CATEGORY);
            when(productMapper.updateProductCategory(CATEGORY, PRODUCT)).thenReturn(PRODUCT);
            when(productMapper.updateProduct(REQUEST, PRODUCT)).thenReturn(PRODUCT);

            underTest.updateProduct(TOKEN, REQUEST, USER_AGENT);

            InOrder inOrder = inOrder(authService, categoryService, productMapper, productsRepository);
            inOrder.verify(authService).checkIsUserAdmin(eq(TOKEN), anyString());
            inOrder.verify(categoryService).getCategoryById(eq(REQUEST.getCategoryId()), anyString());
            inOrder.verify(productMapper).updateProductCategory(CATEGORY, PRODUCT);
            inOrder.verify(productsRepository).save(any());
        }

        @Test
        @DisplayName("Should update product case sku equal and category null")
        void updateProduct_ShouldUpdateProductCaseSkuEqualAndCategoryNull() {
            when(productsRepository.findById(REQUEST.getId())).thenReturn(Optional.of(PRODUCT));
            when(productMapper.updateProduct(REQUEST, PRODUCT)).thenReturn(PRODUCT);

            underTest.updateProduct(TOKEN, REQUEST, USER_AGENT);

            InOrder inOrder = inOrder(authService, productsRepository);
            inOrder.verify(authService).checkIsUserAdmin(eq(TOKEN), anyString());
            inOrder.verify(productsRepository).save(any());
        }
    }


    @Nested
    @DisplayName("Change is active tests")
    class ChangeIsActive {

        @Test
        @DisplayName("Should throw not found exception product not found")
        void changeIsActive_ShouldThrowNotFoundExceptionProductNotFound() {
            when(productsRepository.findById(NOT_EXISTING_ENTITY_ID)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> underTest.changeIsActive(TOKEN, NOT_EXISTING_ENTITY_ID, USER_AGENT));

            InOrder inOrder = inOrder(authService, productsRepository);
            inOrder.verify(authService).checkIsUserAdmin(eq(TOKEN), anyString());
            inOrder.verify(productsRepository).findById(NOT_EXISTING_ENTITY_ID);
        }

        @Test
        @DisplayName("Should change is active")
        void changeIsActive_ShouldChangeIsActive() {
            PRODUCT.setIsActive(false);

            when(productsRepository.findById(EXISTING_ENTITY_ID)).thenReturn(Optional.of(PRODUCT));

            underTest.changeIsActive(TOKEN, EXISTING_ENTITY_ID, USER_AGENT);

            assertThat(PRODUCT.getIsActive()).isEqualTo(true);

            InOrder inOrder = inOrder(authService, productsRepository, productsRepository);
            inOrder.verify(authService).checkIsUserAdmin(eq(TOKEN), anyString());
            inOrder.verify(productsRepository).findById(EXISTING_ENTITY_ID);
            inOrder.verify(productsRepository).save(PRODUCT);
        }
    }

    @Nested
    @DisplayName("Validate and get product tests")
    class ValidateAndGetProduct {

        @Test
        @DisplayName("Should throw bad request exception product is disabled")
        void validateAndGetProduct_ShouldThrowBadRequestExceptionProductIsDisabled() {
            PRODUCT.setIsActive(false);

            when(productsRepository.findById(EXISTING_ENTITY_ID)).thenReturn(Optional.of(PRODUCT));

            assertThrows(BadRequestException.class,
                    () -> underTest.validateAndGetProduct(EXISTING_ENTITY_ID, requestId));

            verify(productsRepository).findById(EXISTING_ENTITY_ID);
        }

        @Test
        @DisplayName("Should throw bad request exception stock quantity less than zero")
        void validateAndGetProduct_ShouldThrowBadRequestExceptionStockQuantityLessThanZero() {
            PRODUCT.setIsActive(true);
            PRODUCT.setStockQuantity(INVALID_PRODUCT_STOCK_QUANTITY);

            when(productsRepository.findById(EXISTING_ENTITY_ID)).thenReturn(Optional.of(PRODUCT));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> underTest.validateAndGetProduct(EXISTING_ENTITY_ID, requestId));

            assertThat(exception.getMessage()).contains(requestId);

            verify(productsRepository).findById(EXISTING_ENTITY_ID);
            verify(productsRepository).save(any(ProductEntity.class));
        }

        @Test
        @DisplayName("Should return valid product")
        void validateAndGetProduct_ShouldReturnValidProduct() {
            PRODUCT.setIsActive(true);
            PRODUCT.setStockQuantity(PRODUCT_STOCK_QUANTITY);

            when(productsRepository.findById(EXISTING_ENTITY_ID)).thenReturn(Optional.of(PRODUCT));

            ProductEntity productEntity = underTest.validateAndGetProduct(EXISTING_ENTITY_ID, requestId);

            assertThat(productEntity.getId()).isEqualTo(PRODUCT.getId());
            assertThat(productEntity.getIsActive()).isEqualTo(PRODUCT.getIsActive());
            assertThat(productEntity.getStockQuantity()).isEqualTo(PRODUCT.getStockQuantity());
        }
    }

    @Nested
    @DisplayName("Get all product by category id tests")
    class GetAllProductByCategoryId {

        private static final PageableResponseGetProductsByCategory RESPONSE
                = new PageableResponseGetProductsByCategory();

        @Test
        @DisplayName("Should return pageable of products by category id")
        void getAllProductByCategoryId() {
            when(productsRepository
                    .findByCategoryId(EXISTING_ENTITY_ID, PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE)))
                    .thenReturn(EMPTY_PAGE);
            when(productMapper.createPageableResponseGetProductsByCategory(EMPTY_PAGE))
                    .thenReturn(RESPONSE);

            PageableResponseGetProductsByCategory result = underTest
                    .getAllProductByCategoryId(EXISTING_ENTITY_ID, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, USER_AGENT);

            assertThat(result).isNotNull().isSameAs(RESPONSE);

            InOrder inOrder = inOrder(productsRepository, productMapper);
            inOrder.verify(productsRepository)
                    .findByCategoryId(EXISTING_ENTITY_ID, PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE));
            inOrder.verify(productMapper).createPageableResponseGetProductsByCategory(EMPTY_PAGE);
        }
    }

    @Nested
    @DisplayName("Increase stock for order items tests")
    class IncreaseStockForOrderItems {

        private static final OrderItemEntity ORDER_ITEM_ENTITY = new OrderItemEntity();
        private static final int ORDER_ITEM_ENTITY_STOCK_QUANTITY = 2;

        @BeforeAll
        static void setUp() {
            PRODUCT.setStockQuantity(PRODUCT_STOCK_QUANTITY);
            ORDER_ITEM_ENTITY.setProduct(PRODUCT);
            ORDER_ITEM_ENTITY.setQuantity(ORDER_ITEM_ENTITY_STOCK_QUANTITY);
        }

        @Test
        @DisplayName("Should increase stock for order items")
        void increaseStockForOrderItems() {
            List<OrderItemEntity> orderItems = List.of(ORDER_ITEM_ENTITY);

            underTest.increaseStockForOrderItems(orderItems);

            assertThat(PRODUCT.getStockQuantity())
                    .isEqualTo(PRODUCT_STOCK_QUANTITY + ORDER_ITEM_ENTITY_STOCK_QUANTITY);

            verify(productsRepository).saveAll(orderItems.stream().map(OrderItemEntity::getProduct).toList());
        }
    }

    @Nested
    @DisplayName("Decrease stock for order items tests")
    class DecreaseStockForOrderItems {

        @BeforeAll
        static void setUp() {
            PRODUCT.setStockQuantity(PRODUCT_STOCK_QUANTITY);
        }

        @Test
        @DisplayName("Should decrease stock for order items")
        void decreaseStockForOrderItems_ShouldDecreaseStockForOrderItems() {
            List<CartItemEntity> cartItems = List.of(CART_ITEM_ENTITY);

            underTest.decreaseStockForOrderItems(cartItems);

            assertThat(PRODUCT.getStockQuantity())
                    .isEqualTo(PRODUCT_STOCK_QUANTITY - CART_ITEM_ENTITY.getQuantity());

            verify(productsRepository).saveAll(cartItems.stream().map(CartItemEntity::getProduct).toList());
        }
    }

    @Nested
    @DisplayName("Validate products for order tests")
    class ValidateProductsForOrder {

        private static final int VALID_PRODUCT_STOCK_QUANTITY = 2;
        private static final int CART_ITEM_QUANTITY = 1;

        @Test
        @DisplayName("Should throw not found exception product do not exist")
        void validateProductsForOrder_ShouldThrowNotFoundExceptionProductDoNotExist() {
            List<CartItemEntity> cartItems = List.of(CART_ITEM_ENTITY);

            when(productsRepository.findById(any())).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> underTest.validateProductsForOrder(cartItems, requestId));

            assertThat(exception.getMessage()).contains(requestId);

            verify(productsRepository).findById(any());
        }

        @Test
        @DisplayName("Should throw bad request exception product is disabled")
        void validateProductsForOrder_ShouldThrowBadRequestExceptionProductIsDisabled() {
            PRODUCT.setIsActive(false);
            List<CartItemEntity> cartItems = List.of(CART_ITEM_ENTITY);

            when(productsRepository.findById(any(Long.class))).thenReturn(Optional.of(PRODUCT));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> underTest.validateProductsForOrder(cartItems, requestId));

            assertThat(exception.getMessage()).contains(requestId);

            verify(productsRepository).findById(any(Long.class));
        }

        @Test
        @DisplayName("Should throw bad request exception not enough stock")
        void validateProductsForOrder_ShouldThrowBadRequestExceptionNotEnoughStock() {
            PRODUCT.setIsActive(true);
            PRODUCT.setStockQuantity(INVALID_PRODUCT_STOCK_QUANTITY);
            CART_ITEM_ENTITY.setQuantity(CART_ITEM_QUANTITY);
            List<CartItemEntity> cartItems = List.of(CART_ITEM_ENTITY);

            when(productsRepository.findById(any())).thenReturn(Optional.of(PRODUCT));

            assertThrows(BadRequestException.class, () -> underTest.validateProductsForOrder(cartItems, requestId));

            verify(productsRepository).findById(any());
        }

        @Test
        @DisplayName("Should validate product and return it")
        void validateProductsForOrder() {
            PRODUCT.setIsActive(true);
            PRODUCT.setStockQuantity(VALID_PRODUCT_STOCK_QUANTITY);
            CART_ITEM_ENTITY.setQuantity(CART_ITEM_QUANTITY);
            List<CartItemEntity> cartItems = List.of(CART_ITEM_ENTITY);

            when(productsRepository.findById(any())).thenReturn(Optional.of(PRODUCT));

            underTest.validateProductsForOrder(cartItems, requestId);

            verify(productsRepository).findById(any());
        }
    }
}