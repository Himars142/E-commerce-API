package com.example.demo3.service;

import com.example.demo3.dto.CartDTO;
import com.example.demo3.dto.UpdateCartItemRequestDTO;

public interface CartService {
    CartDTO getCart(String userAgent);

    void addItemToCart(Long productId, String userAgent);

    void updateCartItem(Long productId, UpdateCartItemRequestDTO request, String userAgent);

    void removeItemFromCart(Long productId, String userAgent);

    int clearCart(String userAgent);

    void deleteAllByCartId(Long id);
}
