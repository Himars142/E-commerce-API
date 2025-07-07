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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final CartItemRepository cartItemRepository;
    private final AuthService authService;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

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
    public CartDTO getCart(String token, String userAgent) {
        String requestId = UUID.randomUUID().toString();
        logger.info("Attempt to get a car request id:{}, user agent: {}", requestId, userAgent);
        UserEntity user = authService.validateTokenAndGetUser(token, requestId);
        CartEntity cart = getOrCreateCartForUser(user);
        CartDTO cartDTO = cartMapper.toDTO(cart);
        logger.info("Successfully retrieved cart request id: {}, for user ID: {}. Cart ID: {}",
                requestId, user.getId(), cart.getId());
        return cartDTO;
    }

    @Transactional
    @Override
    public void addItemToCart(String token, Long productId, String userAgent) {
        String requestId = UUID.randomUUID().toString();
        logger.info("Attempt to add item to a cart request id: {}, user agent: {}, product id: {}",
                requestId, userAgent, productId);
        UserEntity user = authService.validateTokenAndGetUser(token, requestId);
        CartEntity cart = getOrCreateCartForUser(user);
        ProductEntity product = productService.validateAndGetProduct(productId, requestId);
        CartItemEntity cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .map(item -> cartItemMapper.changeQuantity(item, item.getQuantity() + 1))
                .orElseGet(() -> cartItemMapper.createNewCartItemEntity(cart, product, 1));
        cartItemRepository.save(cartItem);
        logger.info("Successfully added product request id: {}, product ID {} (quantity: {}) to cart ID {} for user ID {}. Cart item ID: {}, request id: {}",
                requestId, productId, cartItem.getQuantity(), cart.getId(), user.getId(), cartItem.getId(), requestId);
    }

    @Transactional
    @Override
    public void updateCartItem(String token, Long productId, UpdateCartItemRequestDTO request, String userAgent) {
        if (request.getQuantity() <= 0) {
            logger.info("Redirecting request from update item to remove item from cart");
            removeItemFromCart(token, productId, userAgent);
            return;
        }
        String requestId = UUID.randomUUID().toString();
        logger.info("Attempt to update cart item request id: {}, product id: {}, quantity: {}, user agent: {}",
                requestId, productId, request.getQuantity(), userAgent);
        UserEntity user = authService.validateTokenAndGetUser(token, requestId);
        ProductEntity product = productService.validateAndGetProduct(productId, requestId);
        CartEntity cart = getOrCreateCartForUser(user);
        CartItemEntity cartItemEntity = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .map(cartItem -> cartItemMapper.changeQuantity(cartItem, request.getQuantity()))
                .orElseGet(() -> cartItemMapper.createNewCartItemEntity(cart, product, request.getQuantity()));
        cartItemRepository.save(cartItemEntity);
        logger.info("Successfully updated product ID {} (quantity: {}) in cart ID {} for user ID {}. Cart item ID: {}, request id: {}",
                productId, request.getQuantity(), cart.getId(), user.getId(), cartItemEntity.getId(), requestId);
    }

    @Transactional
    @Override
    public void removeItemFromCart(String token, Long productId, String userAgent) {
        String requestId = UUID.randomUUID().toString();
        logger.info("Attempt to remove item from cart. Request id: {}, user agent: {}, product id: {}",
                requestId, userAgent, productId);
        UserEntity user = authService.validateTokenAndGetUser(token, requestId);
        CartEntity cart = getOrCreateCartForUser(user);
        CartItemEntity deleteEntity = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new NotFoundException("No product with the ID:" + productId + " in the cart! Request id: " + requestId));
        cartItemRepository.delete(deleteEntity);
        logger.info("Successfully removed product ID {} in cart {} for user ID {}, request id: {}.",
                productId, cart.getId(), user.getId(), requestId);
    }

    @Transactional
    @Override
    public int clearCart(String token, String userAgent) {
        String requestId = UUID.randomUUID().toString();
        logger.info("Attempt clear cart. Request id: {}, user agent: {}", requestId, userAgent);
        UserEntity user = authService.validateTokenAndGetUser(token, requestId);
        CartEntity cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("User don`t have cart! Request id: " + requestId));
        int deletedItems = cartItemRepository.deleteAllByCartId(cart.getId());
        logger.info("Successfully clear cart {} for user ID {}", cart.getId(), user.getId());
        return deletedItems;
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
