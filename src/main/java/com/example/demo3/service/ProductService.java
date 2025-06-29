package com.example.demo3.service;

import com.example.demo3.dto.*;
import com.example.demo3.entity.CartItemEntity;
import com.example.demo3.entity.OrderItemEntity;
import com.example.demo3.entity.ProductEntity;

import java.util.List;

public interface ProductService {
    ProductBasicDTO getProductById(Long id);

    PageableResponseProducts getAllProducts(int page, int size);

    void addProduct(String token, ProductRequestDTO product);

    void updateProduct(String token, UpdateProductRequestDTO product);

    void changeIsActive(String token, Long id);

    ProductEntity validateAndGetProduct(Long productId);

    ProductEntity getProduct(Long productId);

    PageableResponseGetProductsByCategory getAllProductByCategoryId(Long categoryId, int page, int size);

    void increaseStockForOrderItems(List<OrderItemEntity> orderItems);

    void decreaseStockForOrderItems(List<CartItemEntity> cartItems);

    void validateProductsForOrder(List<CartItemEntity> cartItems);
}
