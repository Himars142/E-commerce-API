package com.example.demo3.service;

import com.example.demo3.dto.CartDTO;
import com.example.demo3.dto.UpdateCartItemRequestDTO;

public interface CartService {
    CartDTO getCart(String token);

    void addItemToCart(String token, Long productId);

    void updateCartItem(String token, Long productId, UpdateCartItemRequestDTO request);

    void removeItemFromCart(String token, Long productId);

    int clearCart(String token);

    void deleteAllByCartId(Long id);
}
