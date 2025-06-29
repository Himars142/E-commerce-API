package com.example.demo3.mapper;

import com.example.demo3.dto.CartItemDTO;
import com.example.demo3.entity.CartEntity;
import com.example.demo3.entity.CartItemEntity;
import com.example.demo3.entity.ProductEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartItemMapper {
    private final ProductMapper productMapper;

    public CartItemMapper(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    CartItemDTO toDTO(CartItemEntity entity) {
        CartItemDTO dto = new CartItemDTO();
        dto.setQuantity(entity.getQuantity());
        dto.setProduct(productMapper.toDTO(entity.getProduct()));
        return dto;
    }

    List<CartItemDTO> toDTO(List<CartItemEntity> entityList) {
        return entityList.stream()
                .map(this::toDTO)
                .toList();
    }

    public CartItemEntity changeQuantity(CartItemEntity cartItemEntity, int quantity) {
        cartItemEntity.setQuantity(quantity);
        return cartItemEntity;
    }

    public CartItemEntity createNewCartItemEntity(CartEntity cart, ProductEntity product, int quantity) {
        CartItemEntity newItem = new CartItemEntity();
        newItem.setCart(cart);
        newItem.setProduct(product);
        newItem.setQuantity(quantity);
        return newItem;
    }
}
