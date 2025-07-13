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
import com.example.demo3.service.ProductService;
import com.example.demo3.testutil.BaseServiceTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartServiceImplTest extends BaseServiceTest {
    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private AuthService authService;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @InjectMocks
    private CartServiceImpl underTest;

    @Test
    void getCart_ShouldReturnCart() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setId(1L);
        CartEntity cart = new CartEntity(user);
        cart.setId(1L);

        CartDTO expectedCartDTO = new CartDTO();
        expectedCartDTO.setCartItems(Collections.emptyList());

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartMapper.toDTO(cart)).thenReturn(expectedCartDTO);

        CartDTO result = underTest.getCart(token, userAgent);

        assertNotNull(result);
        assertEquals(expectedCartDTO, result);

        verify(authService).validateTokenAndGetUser(eq(token), anyString());
        verify(cartRepository).findByUserId(user.getId());
        verify(cartMapper).toDTO(cart);
    }

    @Test
    void addItemToCart_ShouldUpdateItemInTheCartQuantity() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setId(1L);
        CartEntity cart = new CartEntity(user);
        cart.setId(1L);
        ProductEntity product = new ProductEntity();
        product.setId(1L);

        CartItemEntity existingCartItem = new CartItemEntity();
        existingCartItem.setProduct(product);
        existingCartItem.setQuantity(1);

        CartItemEntity updatedCartItem = new CartItemEntity();
        updatedCartItem.setProduct(product);
        updatedCartItem.setQuantity(2);

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(cartRepository.findByUserId(eq(user.getId()))).thenReturn(Optional.of(cart));
        when(productService.validateAndGetProduct(eq(product.getId()), anyString())).thenReturn(product);
        when(cartItemRepository.findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()))).thenReturn(Optional.of(existingCartItem));
        when(cartItemMapper.changeQuantity(eq(existingCartItem), eq(2))).thenReturn(updatedCartItem);

        underTest.addItemToCart(token, product.getId(), userAgent);

        verify(authService).validateTokenAndGetUser(eq(token), anyString());
        verify(cartRepository).findByUserId(eq(user.getId()));
        verify(productService).validateAndGetProduct(eq(product.getId()), anyString());
        verify(cartItemRepository).findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()));
        verify(cartItemMapper).changeQuantity(eq(existingCartItem), eq(2));
        verify(cartItemRepository).save(eq(updatedCartItem));
    }

    @Test
    void addItemToCart_ShouldReturnNewCartItem() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setId(1L);
        CartEntity cart = new CartEntity(user);
        cart.setId(1L);
        ProductEntity product = new ProductEntity();
        product.setId(1L);

        CartItemEntity existingCartItem = new CartItemEntity();
        existingCartItem.setProduct(product);
        existingCartItem.setQuantity(1);

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(cartRepository.findByUserId(eq(user.getId()))).thenReturn(Optional.of(cart));
        when(productService.validateAndGetProduct(eq(product.getId()), anyString())).thenReturn(product);
        when(cartItemRepository.findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()))).thenReturn(Optional.empty());
        when(cartItemMapper.createNewCartItemEntity(cart, product, 1)).thenReturn(existingCartItem);

        underTest.addItemToCart(token, product.getId(), userAgent);

        verify(authService).validateTokenAndGetUser(eq(token), anyString());
        verify(cartRepository).findByUserId(eq(user.getId()));
        verify(productService).validateAndGetProduct(eq(product.getId()), anyString());
        verify(cartItemRepository).findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()));
        verify(cartItemMapper).createNewCartItemEntity(cart, product, 1);
        verify(cartItemRepository).save(eq(existingCartItem));
    }

    @Test
    void updateCartItem_ShouldUpdateItemFromCart() {
        String token = "valid-token";
        String userAgent = "testing";
        Long productId = 1L;
        UpdateCartItemRequestDTO request = new UpdateCartItemRequestDTO(5);

        UserEntity user = new UserEntity();
        user.setId(1L);
        CartEntity cart = new CartEntity(user);
        cart.setId(1L);
        ProductEntity product = new ProductEntity();
        product.setId(1L);

        CartItemEntity existingCartItem = new CartItemEntity();
        existingCartItem.setId(1L);
        existingCartItem.setProduct(product);
        existingCartItem.setQuantity(1);

        CartItemEntity updatedCartItem = new CartItemEntity();
        updatedCartItem.setId(1L);
        updatedCartItem.setProduct(product);
        updatedCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(productService.validateAndGetProduct(eq(product.getId()), anyString())).thenReturn(product);
        when(cartRepository.findByUserId(eq(user.getId()))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()))).thenReturn(Optional.of(existingCartItem));
        when(cartItemMapper.changeQuantity(eq(existingCartItem), eq(request.getQuantity()))).thenReturn(updatedCartItem);

        underTest.updateCartItem(token, productId, request, userAgent);

        verify(authService).validateTokenAndGetUser(eq(token), anyString());
        verify(cartRepository).findByUserId(eq(user.getId()));
        verify(productService).validateAndGetProduct(eq(product.getId()), anyString());
        verify(cartItemRepository).findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()));
        verify(cartItemMapper).changeQuantity(eq(existingCartItem), eq(request.getQuantity()));
        verify(cartItemRepository).save(eq(updatedCartItem));
    }

    @Test
    void updateCartItem_ShouldCreateNewCartItem() {
        String token = "valid-token";
        String userAgent = "testing";
        Long productId = 1L;
        UpdateCartItemRequestDTO request = new UpdateCartItemRequestDTO(5);


        UserEntity user = new UserEntity();
        user.setId(1L);
        CartEntity cart = new CartEntity(user);
        cart.setId(1L);
        ProductEntity product = new ProductEntity();
        product.setId(1L);

        CartItemEntity existingCartItem = new CartItemEntity();
        existingCartItem.setProduct(product);
        existingCartItem.setQuantity(1);

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(cartRepository.findByUserId(eq(user.getId()))).thenReturn(Optional.of(cart));
        when(productService.validateAndGetProduct(eq(product.getId()), anyString())).thenReturn(product);
        when(cartItemRepository.findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()))).thenReturn(Optional.empty());
        when(cartItemMapper.createNewCartItemEntity(cart, product, request.getQuantity())).thenReturn(existingCartItem);

        underTest.updateCartItem(token, productId, request, userAgent);

        verify(authService).validateTokenAndGetUser(eq(token), anyString());
        verify(cartRepository).findByUserId(eq(user.getId()));
        verify(productService).validateAndGetProduct(eq(product.getId()), anyString());
        verify(cartItemRepository).findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()));
        verify(cartItemMapper).createNewCartItemEntity(cart, product, request.getQuantity());
        verify(cartItemRepository).save(eq(existingCartItem));
    }

    @Test
    void updateCartItem_ShouldRedirectToRemoveItem() {
        String token = "valid-token";
        String userAgent = "testing";
        Long productId = 1L;
        UpdateCartItemRequestDTO request = new UpdateCartItemRequestDTO(0);

        UserEntity user = new UserEntity();
        user.setId(1L);
        CartEntity cart = new CartEntity(user);
        cart.setId(1L);
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setProduct(product);
        cartItem.setCart(cart);

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(cartRepository.findByUserId(eq(user.getId()))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()))).thenReturn(Optional.of(cartItem));

        underTest.updateCartItem(token, productId, request, userAgent);

        verify(authService).validateTokenAndGetUser(eq(token), anyString());
        verify(cartRepository).findByUserId(eq(user.getId()));
        verify(cartItemRepository).findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()));
    }

    @Test
    void removeItemFromCart_ShouldDeleteCartItem() {
        String token = "valid-token";
        String userAgent = "testing";
        Long productId = 1L;

        UserEntity user = new UserEntity();
        user.setId(1L);
        CartEntity cart = new CartEntity(user);
        cart.setId(1L);
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setProduct(product);
        cartItem.setCart(cart);

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(cartRepository.findByUserId(eq(user.getId()))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()))).thenReturn(Optional.of(cartItem));

        underTest.removeItemFromCart(token, productId, userAgent);

        verify(authService).validateTokenAndGetUser(eq(token), anyString());
        verify(cartRepository).findByUserId(eq(user.getId()));
        verify(cartItemRepository).findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()));
    }

    @Test
    void removeItemFromCart_ShouldThrowNotFoundException() {
        String token = "valid-token";
        String userAgent = "testing";
        Long productId = 1L;

        UserEntity user = new UserEntity();
        user.setId(1L);
        CartEntity cart = new CartEntity(user);
        cart.setId(1L);
        ProductEntity product = new ProductEntity();
        product.setId(1L);

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(cartRepository.findByUserId(eq(user.getId()))).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.removeItemFromCart(token, productId, userAgent));

        verify(authService).validateTokenAndGetUser(eq(token), anyString());
        verify(cartRepository).findByUserId(eq(user.getId()));
        verify(cartItemRepository).findByCartIdAndProductId(eq(cart.getId()), eq(product.getId()));
    }

    @Test
    void clearCart_ShouldThrowCartNotFound() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setId(1L);

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(cartRepository.findByUserId(eq(user.getId()))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> underTest.clearCart(token, userAgent));

        verify(authService).validateTokenAndGetUser(eq(token), anyString());
        verify(cartRepository).findByUserId(eq(user.getId()));
    }

    @Test
    void clearCart_ShouldReturnNumberOfDeletedItems() {
        String token = "valid-token";
        String userAgent = "testing";

        UserEntity user = new UserEntity();
        user.setId(1L);
        CartEntity cart = new CartEntity(user);
        cart.setId(1L);

        when(authService.validateTokenAndGetUser(eq(token), anyString())).thenReturn(user);
        when(cartRepository.findByUserId(eq(user.getId()))).thenReturn(Optional.of(cart));

        underTest.clearCart(token, userAgent);

        verify(authService).validateTokenAndGetUser(eq(token), anyString());
        verify(cartRepository).findByUserId(eq(user.getId()));
        verify(cartItemRepository).deleteAllByCartId(eq(cart.getId()));
    }

    @Test
    void deleteAllByCartId_ShouldDeleteAllByCartID() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        CartEntity cart = new CartEntity(user);
        cart.setId(1L);

        when(cartItemRepository.deleteAllByCartId(cart.getId())).thenReturn(0);

        underTest.deleteAllByCartId(cart.getId());

        verify(cartItemRepository).deleteAllByCartId(cart.getId());
    }
}