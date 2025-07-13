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
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void getProductById_ShouldReturnProductDTO() {
        String userAgent = "testing";
        ProductEntity product = new ProductEntity();
        product.setId(1L);

        when(productsRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productMapper.toDTO(product)).thenReturn(new ProductBasicDTO());

        underTest.getProductById(product.getId(), userAgent);

        verify(productsRepository).findById(product.getId());
        verify(productMapper).toDTO(product);
    }

    @Test
    void getProductById_ShouldThrowNotFoundException() {
        String userAgent = "testing";

        when(productsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.getProductById(1L, userAgent));

        verify(productsRepository).findById(1L);
    }

    @Test
    void getAllProducts_ShouldReturnPageableResponseProducts() {
        int page = 0;
        int size = 10;
        String userAgent = "testing";
        Page<ProductEntity> pageable = Page.empty();

        when(productsRepository.findAll(PageRequest.of(page, size))).thenReturn(pageable);
        when(productMapper.createPageableResponseProducts(pageable)).thenReturn(new PageableResponseProducts());

        underTest.getAllProducts(page, size, userAgent);

        verify(productsRepository).findAll(PageRequest.of(page, size));
        verify(productMapper).createPageableResponseProducts(pageable);
    }

    @Test
    void addProduct_ShouldThrowBadRequestExceptionSkuMustBeUnique() {
        String token = "valid-token";
        String userAgent = "testing";

        ProductRequestDTO product = new ProductRequestDTO();
        product.setCategoryId(1L);
        product.setSku("TEST-SKU");

        when(categoryService.getCategoryById(any(), anyString())).thenReturn(new CategoryEntity());
        when(productsRepository.findBySku(product.getSku())).thenReturn(Optional.of(new ProductEntity()));

        assertThrows(BadRequestException.class, () -> underTest.addProduct(token, product, userAgent));

        verify(productsRepository).findBySku(product.getSku());
    }

    @Test
    void addProduct_ShouldAddProduct() {
        String token = "valid-token";
        String userAgent = "testing";

        ProductRequestDTO product = new ProductRequestDTO();
        product.setName("TEST-ADD-PRODUCT-REQUEST");
        product.setPrice(BigDecimal.valueOf(12.13));
        product.setStockQuantity(10);
        product.setDescription("TEST-DESCRIPTION");
        product.setSku("TEST-SKU");
        product.setCategoryId(1L);
        product.setSku("TEST-SKU");

        ProductEntity addedProduct = new ProductEntity();
        CategoryEntity category = new CategoryEntity();
        category.setName("TEST-CATEGORY");
        category.setId(1L);

        when(categoryService.getCategoryById(any(), anyString())).thenReturn(category);
        when(productsRepository.findBySku(product.getSku())).thenReturn(Optional.empty());
        when(productsRepository.save(addedProduct)).thenReturn(addedProduct);
        when(productMapper.createProduct(product, category)).thenReturn(addedProduct);

        underTest.addProduct(token, product, userAgent);

        verify(authService).checkIsUserAdmin(eq(token), anyString());
        verify(productsRepository).findBySku(product.getSku());
        verify(productMapper).createProduct(product, category);
        verify(productsRepository).save(addedProduct);
    }

    @Test
    void updateProduct_ShouldThrowNotFoundExceptionProductNotFound() {
        String token = "valid-token";
        String userAgent = "testing";

        UpdateProductRequestDTO request = new UpdateProductRequestDTO();
        request.setId(1L);

        when(productsRepository.findById(request.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.updateProduct(token, request, userAgent));

        verify(authService).checkIsUserAdmin(eq(token), anyString());
        verify(productsRepository).findById(request.getId());
    }

    @Test
    void updateProduct_ShouldThrowBadRequestException() {
        String token = "valid-token";
        String userAgent = "testing";

        UpdateProductRequestDTO request = new UpdateProductRequestDTO();
        request.setId(1L);
        request.setSku("TEST-SKU");

        ProductEntity product = new ProductEntity();
        product.setSku("PRODUCT-SKU");

        when(productsRepository.findById(request.getId())).thenReturn(Optional.of(product));
        when(productsRepository.findBySku(product.getSku())).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> underTest.updateProduct(token, request, userAgent));

        verify(authService).checkIsUserAdmin(eq(token), anyString());
        verify(productsRepository).findById(request.getId());
        verify(productsRepository).findBySku(product.getSku());
    }

    @Test
    void updateProduct_ShouldUpdateProduct() {
        String token = "valid-token";
        String userAgent = "testing";

        UpdateProductRequestDTO request = new UpdateProductRequestDTO();
        request.setId(1L);
        request.setCategoryId(1L);

        ProductEntity product = new ProductEntity();

        CategoryEntity category = new CategoryEntity();

        when(productsRepository.findById(request.getId())).thenReturn(Optional.of(product));
        when(categoryService.getCategoryById(eq(request.getCategoryId()), anyString())).thenReturn(category);
        when(productMapper.updateProductCategory(category, product)).thenReturn(product);
        when(productMapper.updateProduct(request, product)).thenReturn(product);

        underTest.updateProduct(token, request, userAgent);

        verify(authService).checkIsUserAdmin(eq(token), anyString());
        verify(categoryService).getCategoryById(eq(request.getCategoryId()), anyString());
        verify(productMapper).updateProductCategory(category, product);
        verify(productsRepository).save(any());
    }

    @Test
    void updateProduct_ShouldUpdateProductCaseSkuEqualAndCategoryNull() {
        String token = "valid-token";
        String userAgent = "testing";

        UpdateProductRequestDTO request = new UpdateProductRequestDTO();
        request.setId(1L);
        request.setSku("TEST-SKU");

        ProductEntity product = new ProductEntity();
        product.setSku("TEST-SKU");

        when(productsRepository.findById(request.getId())).thenReturn(Optional.of(product));
        when(productMapper.updateProduct(request, product)).thenReturn(product);

        underTest.updateProduct(token, request, userAgent);

        verify(authService).checkIsUserAdmin(eq(token), anyString());
        verify(productsRepository).save(any());
    }

    @Test
    void changeIsActive_ShouldThrowNotFoundExceptionProductNotFound() {
        String token = "valid-token";
        String userAgent = "testing";

        when(productsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.changeIsActive(token, 1L, userAgent));

        verify(authService).checkIsUserAdmin(eq(token), anyString());
        verify(productsRepository).findById(1L);
    }

    @Test
    void changeIsActive_ShouldChangeIsActive() {
        String token = "valid-token";
        String userAgent = "testing";

        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setIsActive(false);

        when(productsRepository.findById(1L)).thenReturn(Optional.of(product));

        underTest.changeIsActive(token, 1L, userAgent);

        verify(authService).checkIsUserAdmin(eq(token), anyString());
        verify(productsRepository).findById(1L);
        verify(productsRepository).save(product);

        assertEquals(true, product.getIsActive());
    }

    @Test
    void validateAndGetProduct_ShouldThrowBadRequestExceptionProductIsDisabled() {
        String requestId = UUID.randomUUID().toString();

        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setIsActive(false);

        when(productsRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> underTest.validateAndGetProduct(1L, requestId));

        verify(productsRepository).findById(1L);
    }

    @Test
    void validateAndGetProduct_ShouldThrowBadRequestExceptionStockQuantityLessThanZero() {
        String requestId = UUID.randomUUID().toString();

        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setIsActive(true);
        product.setStockQuantity(0);

        when(productsRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> underTest.validateAndGetProduct(1L, requestId));
    }

    @Test
    void validateAndGetProduct_ShouldReturnValidProduct() {
        String requestId = UUID.randomUUID().toString();

        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setIsActive(true);
        product.setStockQuantity(1);

        when(productsRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductEntity productEntity = underTest.validateAndGetProduct(1L, requestId);

        assertEquals(productEntity.getId(), product.getId());
        assertEquals(productEntity.getIsActive(), product.getIsActive());
        assertEquals(productEntity.getStockQuantity(), product.getStockQuantity());
    }

    @Test
    void getAllProductByCategoryId() {
        int page = 0;
        int size = 10;
        Long categoryId = 1L;
        String userAgent = "testing";

        Page<ProductEntity> productPage = Page.empty();

        when(productsRepository.findByCategoryId(categoryId, PageRequest.of(page, size))).thenReturn(productPage);
        when(productMapper.createPageableResponseGetProductsByCategory(productPage)).thenReturn(new PageableResponseGetProductsByCategory());

        underTest.getAllProductByCategoryId(categoryId, page, size, userAgent);

        verify(productsRepository).findByCategoryId(categoryId, PageRequest.of(page, size));
        verify(productMapper).createPageableResponseGetProductsByCategory(productPage);
    }

    @Test
    void increaseStockForOrderItems() {
        ProductEntity product = new ProductEntity();
        product.setStockQuantity(10);
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setProduct(product);
        orderItemEntity.setQuantity(2);
        List<OrderItemEntity> orderItems = List.of(orderItemEntity);

        underTest.increaseStockForOrderItems(orderItems);

        verify(productsRepository).saveAll(orderItems.stream().map(OrderItemEntity::getProduct).toList());

        assertEquals(12, product.getStockQuantity());
    }

    @Test
    void decreaseStockForOrderItems() {
        ProductEntity product = new ProductEntity();
        product.setStockQuantity(10);
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setProduct(product);
        cartItemEntity.setQuantity(2);
        List<CartItemEntity> cartItems = List.of(cartItemEntity);

        underTest.decreaseStockForOrderItems(cartItems);

        verify(productsRepository).saveAll(cartItems.stream().map(CartItemEntity::getProduct).toList());

        assertEquals(8, product.getStockQuantity());
    }

    @Test
    void validateProductsForOrder_ShouldThrowNotFoundExceptionProductDoNotExist() {
        String requestId = UUID.randomUUID().toString();

        ProductEntity product = new ProductEntity();
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setProduct(product);
        List<CartItemEntity> cartItems = List.of(cartItemEntity);

        when(productsRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.validateProductsForOrder(cartItems, requestId));

        verify(productsRepository).findById(any());
    }

    @Test
    void validateProductsForOrder_ShouldThrowBadRequestExceptionProductIsDisabled() {
        String requestId = UUID.randomUUID().toString();

        ProductEntity product = new ProductEntity();
        product.setIsActive(false);
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setProduct(product);
        List<CartItemEntity> cartItems = List.of(cartItemEntity);

        when(productsRepository.findById(any())).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> underTest.validateProductsForOrder(cartItems, requestId));

        verify(productsRepository).findById(any());
    }

    @Test
    void validateProductsForOrder_ShouldThrowBadRequestExceptionNotEnoughStock() {
        String requestId = UUID.randomUUID().toString();

        ProductEntity product = new ProductEntity();
        product.setIsActive(true);
        product.setStockQuantity(1);
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setQuantity(2);
        cartItemEntity.setProduct(product);
        List<CartItemEntity> cartItems = List.of(cartItemEntity);

        when(productsRepository.findById(any())).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> underTest.validateProductsForOrder(cartItems, requestId));

        verify(productsRepository).findById(any());
    }

    @Test
    void validateProductsForOrder() {
        String requestId = UUID.randomUUID().toString();

        ProductEntity product = new ProductEntity();
        product.setIsActive(true);
        product.setStockQuantity(10);
        CartItemEntity cartItemEntity = new CartItemEntity();
        cartItemEntity.setQuantity(2);
        cartItemEntity.setProduct(product);
        List<CartItemEntity> cartItems = List.of(cartItemEntity);

        when(productsRepository.findById(any())).thenReturn(Optional.of(product));

        underTest.validateProductsForOrder(cartItems, requestId);

        verify(productsRepository).findById(any());
    }
}