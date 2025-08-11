package com.example.demo3.service;

import com.example.demo3.dto.*;
import com.example.demo3.entity.CartItemEntity;
import com.example.demo3.entity.OrderItemEntity;
import com.example.demo3.entity.ProductEntity;

import java.util.List;

public interface ProductService {
    ProductBasicDTO getProductById(Long id, String userAgent);

    PageableResponseProducts getAllProducts(int page, int size, String userAgent);

    Long addProduct(ProductRequestDTO product, String userAgent);

    void updateProduct(UpdateProductRequestDTO product, String userAgent);

    void changeIsActive(Long id, String userAgent);

    ProductEntity validateAndGetProduct(Long productId, String requestId);

    ProductEntity getProduct(Long productId, String requestId);

    PageableResponseGetProductsByCategory getAllProductByCategoryId(Long categoryId, int page, int size, String userAgent);

    void increaseStockForOrderItems(List<OrderItemEntity> orderItems);

    void decreaseStockForOrderItems(List<CartItemEntity> cartItems);

    void validateProductsForOrder(List<CartItemEntity> cartItems, String requestId);
}
