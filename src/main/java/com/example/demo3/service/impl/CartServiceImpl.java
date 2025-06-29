package com.example.demo3.service.impl;

import com.example.demo3.dto.CartDTO;
import com.example.demo3.dto.UpdateCartItemRequestDTO;
import com.example.demo3.entity.CartEntity;
import com.example.demo3.entity.CartItemEntity;
import com.example.demo3.entity.ProductEntity;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.exception.NotFoundException;
import com.example.demo3.mapper.CartItemMapper;
import com.example.demo3.mapper.CartMapper;
import com.example.demo3.repository.CartItemRepository;
import com.example.demo3.repository.CartRepository;
import com.example.demo3.service.AuthService;
import com.example.demo3.service.CartService;
import com.example.demo3.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final CartItemRepository cartItemRepository;
    private final AuthService authService;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           CartMapper cartMapper,
                           ProductService productService,
                           AuthService authService,
                           CartItemMapper cartItemMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.authService = authService;
        this.cartMapper = cartMapper;
        this.productService = productService;
        this.cartItemMapper = cartItemMapper;
    }

    @Transactional
    @Override
    public CartDTO getCart(String token) {
        UserEntity user = authService.validateTokenAndGetUser(token);
        CartEntity cart = getOrCreateCartForUser(user);
        return cartMapper.toDTO(cart);
    }

    @Transactional
    @Override
    public void addItemToCart(String token, Long productId) {
        UserEntity user = authService.validateTokenAndGetUser(token);
        CartEntity cart = getOrCreateCartForUser(user);
        ProductEntity product = productService.validateAndGetProduct(productId);
        CartItemEntity cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .map(item -> cartItemMapper.changeQuantity(item, item.getQuantity() + 1))
                .orElseGet(() -> cartItemMapper.createNewCartItemEntity(cart, product, 1));
        cartItemRepository.save(cartItem);
    }

    @Transactional
    @Override
    public void updateCartItem(String token, Long productId, UpdateCartItemRequestDTO request) {
        UserEntity user = authService.validateTokenAndGetUser(token);
        if (request.getQuantity() <= 0) {
            removeItemFromCart(token, productId);
            return;
        }
        ProductEntity product = productService.validateAndGetProduct(productId);
        CartEntity cart = getOrCreateCartForUser(user);
        CartItemEntity cartItemEntity = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .map(cartItem -> cartItemMapper.changeQuantity(cartItem, request.getQuantity()))
                .orElseGet(() -> cartItemMapper.createNewCartItemEntity(cart, product, request.getQuantity()));
        cartItemRepository.save(cartItemEntity);
    }

    @Transactional
    @Override
    public void removeItemFromCart(String token, Long productId) {
        UserEntity user = authService.validateTokenAndGetUser(token);
        CartEntity cart = getOrCreateCartForUser(user);
        CartItemEntity deleteEntity = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new NotFoundException("No product with the ID:" + productId + " in the cart"));
        cartItemRepository.delete(deleteEntity);
    }

    @Transactional
    @Override
    public int clearCart(String token) {
        UserEntity user = authService.validateTokenAndGetUser(token);
        CartEntity cart = getOrCreateCartForUser(user);
        return cartItemRepository.deleteAllByCartId(cart.getId());
    }

    @Transactional
    @Override
    public void deleteAllByCartId(Long id) {
        cartItemRepository.deleteAllByCartId(id);
    }

    @Transactional
    private CartEntity getOrCreateCartForUser(UserEntity user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new CartEntity(user)));
    }
}
