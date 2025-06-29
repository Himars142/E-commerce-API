package com.example.demo3.mapper;

import com.example.demo3.dto.CartDTO;
import com.example.demo3.entity.CartEntity;
import org.springframework.stereotype.Component;

@Component
public class CartMapper {
    private final CartItemMapper cartItemMapper;

    public CartMapper(CartItemMapper cartItemMapper) {
        this.cartItemMapper = cartItemMapper;
    }

    public CartDTO toDTO(CartEntity cartEntity) {
        CartDTO dto = new CartDTO();
        dto.setCartItems(cartItemMapper.toDTO(cartEntity.getCartItems()));
        return dto;
    }
}
