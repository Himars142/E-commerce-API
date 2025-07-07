package com.example.demo3.service;

import com.example.demo3.dto.CartDTO;
import com.example.demo3.dto.UpdateCartItemRequestDTO;

public interface CartService {
    CartDTO getCart(String token, String userAgent);

    void addItemToCart(String token, Long productId, String userAgent);

    void updateCartItem(String token, Long productId, UpdateCartItemRequestDTO request, String userAgent);

    void removeItemFromCart(String token, Long productId, String userAgent);

    int clearCart(String token, String userAgent);

    void deleteAllByCartId(Long id);
}
