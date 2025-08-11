package com.example.demo3.service.impl;

import com.example.demo3.dto.CartDTO;
import com.example.demo3.dto.UpdateCartItemRequestDTO;
import com.example.demo3.entity.CartEntity;
import com.example.demo3.entity.CartItemEntity;
import com.example.demo3.entity.ProductEntity;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.exception.BadRequestException;
import com.example.demo3.exception.NotFoundException;
import com.example.demo3.mapper.CartItemMapper;
import com.example.demo3.mapper.CartMapper;
import com.example.demo3.repository.CartItemRepository;
import com.example.demo3.repository.CartRepository;
import com.example.demo3.service.AuthService;
import com.example.demo3.service.ProductService;
import com.example.demo3.service.impl.testutil.BaseServiceTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    private static final UserEntity USER = new UserEntity();
    private static final CartEntity CART = new CartEntity(USER);
    private static final ProductEntity PRODUCT = new ProductEntity();
    private static final Long PRODUCT_ID = 1L;

    @BeforeAll
    static void setUp() {
        USER.setId(1L);
        CART.setId(1L);
        PRODUCT.setId(1L);
    }

    @Nested
    @DisplayName("Get cart tests")
    class GetCart {

        private static final CartDTO EXPECTED_CART_DTO = new CartDTO();

        @BeforeAll
        static void setUp() {
            EXPECTED_CART_DTO.setCartItems(Collections.emptyList());
        }

        @Test
        @DisplayName("Should return cart")
        void getCart_ShouldReturnCart() {
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(cartRepository.findByUserId(USER.getId())).thenReturn(Optional.of(CART));
            when(cartMapper.toDTO(CART)).thenReturn(EXPECTED_CART_DTO);

            CartDTO result = underTest.getCart(USER_AGENT);

            assertThat(result).isNotNull().isEqualTo(EXPECTED_CART_DTO);

            InOrder inOrder = inOrder(authService, cartRepository, cartMapper);
            inOrder.verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
            inOrder.verify(cartRepository).findByUserId(USER.getId());
            inOrder.verify(cartMapper).toDTO(CART);
        }

        @Test
        @DisplayName("When user has no cart should create new cart")
        void getCart_WhenUserHasNoCart_ShouldCreateNewCart() {
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(cartRepository.findByUserId(USER.getId())).thenReturn(Optional.empty());
            when(cartRepository.save(any())).thenReturn(CART);
            when(cartMapper.toDTO(CART)).thenReturn(EXPECTED_CART_DTO);

            CartDTO result = underTest.getCart(USER_AGENT);

            assertThat(result).isNotNull().isEqualTo(EXPECTED_CART_DTO);

            InOrder inOrder = inOrder(authService, cartRepository, cartRepository);
            inOrder.verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
            inOrder.verify(cartRepository).findByUserId(USER.getId());
            inOrder.verify(cartRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Add item to cart tests")
    class AddItemToCart {

        private static final CartItemEntity EXISTING_CART_ITEM = new CartItemEntity();
        private static final CartItemEntity UPDATED_CART_ITEM = new CartItemEntity();

        @BeforeAll
        static void setUp() {
            UPDATED_CART_ITEM.setProduct(PRODUCT);
            UPDATED_CART_ITEM.setQuantity(2);
            EXISTING_CART_ITEM.setProduct(PRODUCT);
            EXISTING_CART_ITEM.setQuantity(1);
        }

        @Test
        @DisplayName("Should update item in the cart quantity")
        void addItemToCart_ShouldUpdateItemInTheCartQuantity() {
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(cartRepository.findByUserId(eq(USER.getId()))).thenReturn(Optional.of(CART));
            when(productService.validateAndGetProduct(eq(PRODUCT.getId()), anyString())).thenReturn(PRODUCT);
            when(cartItemRepository.findByCartIdAndProductId(eq(CART.getId()), eq(PRODUCT.getId())))
                    .thenReturn(Optional.of(EXISTING_CART_ITEM));
            when(cartItemMapper.changeQuantity(any(CartItemEntity.class), eq(2))).thenReturn(UPDATED_CART_ITEM);

            underTest.addItemToCart(PRODUCT.getId(), USER_AGENT);

            InOrder inOrder = inOrder(authService, cartRepository, productService, cartItemRepository, cartItemMapper,
                    cartItemRepository);
            inOrder.verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
            inOrder.verify(cartRepository).findByUserId(eq(USER.getId()));
            inOrder.verify(productService).validateAndGetProduct(eq(PRODUCT.getId()), anyString());
            inOrder.verify(cartItemRepository).findByCartIdAndProductId(eq(CART.getId()), eq(PRODUCT.getId()));
            inOrder.verify(cartItemMapper).changeQuantity(any(CartItemEntity.class), eq(2));
            inOrder.verify(cartItemRepository).save(eq(UPDATED_CART_ITEM));
        }

        @Test
        @DisplayName("When item not exists should create new cart item")
        void addItemToCart_WhenItemNotExists_ShouldCreateNewCartItem() {
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(cartRepository.findByUserId(eq(USER.getId()))).thenReturn(Optional.of(CART));
            when(productService.validateAndGetProduct(eq(PRODUCT.getId()), anyString())).thenReturn(PRODUCT);
            when(cartItemRepository.findByCartIdAndProductId(eq(CART.getId()), eq(PRODUCT.getId())))
                    .thenReturn(Optional.empty());
            when(cartItemMapper.createNewCartItemEntity(CART, PRODUCT, 1)).thenReturn(EXISTING_CART_ITEM);

            underTest.addItemToCart(PRODUCT.getId(), USER_AGENT);

            InOrder inOrder = inOrder(authService, cartRepository, productService, cartItemRepository, cartItemMapper,
                    cartItemRepository);
            inOrder.verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
            inOrder.verify(cartRepository).findByUserId(eq(USER.getId()));
            inOrder.verify(productService).validateAndGetProduct(eq(PRODUCT.getId()), anyString());
            inOrder.verify(cartItemRepository).findByCartIdAndProductId(eq(CART.getId()), eq(PRODUCT.getId()));
            inOrder.verify(cartItemMapper).createNewCartItemEntity(CART, PRODUCT, 1);
            inOrder.verify(cartItemRepository).save(eq(EXISTING_CART_ITEM));
        }

        @Test
        @DisplayName("When CART item exists and quantity exceeds limit should throw exception")
        void addItemToCart_WhenCartItemExistsAndQuantityExceedsLimit_ShouldThrowException() {
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(cartRepository.findByUserId(eq(USER.getId()))).thenReturn(Optional.of(CART));
            when(productService.validateAndGetProduct(eq(PRODUCT.getId()), anyString()))
                    .thenThrow(new BadRequestException("test error"));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> underTest.addItemToCart(PRODUCT.getId(), USER_AGENT));

            assertThat(exception).isNotNull();

            InOrder inOrder = inOrder(authService, cartRepository, productService);
            inOrder.verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
            inOrder.verify(cartRepository).findByUserId(eq(USER.getId()));
            inOrder.verify(productService).validateAndGetProduct(eq(PRODUCT.getId()), anyString());
        }
    }

    @Nested
    @DisplayName("Update cart item tests")
    class UpdateCartItem {

        private static final CartItemEntity EXISTING_CART_ITEM = new CartItemEntity();
        private static final UpdateCartItemRequestDTO REQUEST = new UpdateCartItemRequestDTO(5);
        private static final CartItemEntity UPDATED_CART_ITEM = new CartItemEntity();

        @BeforeAll
        static void setUp() {
            EXISTING_CART_ITEM.setId(1L);
            EXISTING_CART_ITEM.setProduct(PRODUCT);
            EXISTING_CART_ITEM.setQuantity(1);
            UPDATED_CART_ITEM.setId(1L);
            UPDATED_CART_ITEM.setProduct(PRODUCT);
            UPDATED_CART_ITEM.setQuantity(EXISTING_CART_ITEM.getQuantity() + REQUEST.getQuantity());
        }

        @Test
        @DisplayName("Should update item from cart")
        void updateCartItem_ShouldUpdateItemFromCart() {
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(productService.validateAndGetProduct(eq(PRODUCT.getId()), anyString())).thenReturn(PRODUCT);
            when(cartRepository.findByUserId(eq(USER.getId()))).thenReturn(Optional.of(CART));
            when(cartItemRepository.findByCartIdAndProductId(eq(CART.getId()), eq(PRODUCT.getId())))
                    .thenReturn(Optional.of(EXISTING_CART_ITEM));
            when(cartItemMapper.changeQuantity(eq(EXISTING_CART_ITEM), eq(REQUEST.getQuantity())))
                    .thenReturn(UPDATED_CART_ITEM);

            underTest.updateCartItem(PRODUCT_ID, REQUEST, USER_AGENT);

            InOrder inOrder = inOrder(authService, productService, cartRepository, cartItemRepository, cartItemMapper,
                    cartItemRepository);
            inOrder.verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
            inOrder.verify(productService).validateAndGetProduct(eq(PRODUCT.getId()), anyString());
            inOrder.verify(cartRepository).findByUserId(eq(USER.getId()));
            inOrder.verify(cartItemRepository).findByCartIdAndProductId(eq(CART.getId()), eq(PRODUCT.getId()));
            inOrder.verify(cartItemMapper).changeQuantity(eq(EXISTING_CART_ITEM), eq(REQUEST.getQuantity()));
            inOrder.verify(cartItemRepository).save(eq(UPDATED_CART_ITEM));
        }

        @Test
        @DisplayName("Should create new cart item")
        void updateCartItem_ShouldCreateNewCartItem() {
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(cartRepository.findByUserId(eq(USER.getId()))).thenReturn(Optional.of(CART));
            when(productService.validateAndGetProduct(eq(PRODUCT.getId()), anyString())).thenReturn(PRODUCT);
            when(cartItemRepository.findByCartIdAndProductId(eq(CART.getId()), eq(PRODUCT.getId())))
                    .thenReturn(Optional.empty());
            when(cartItemMapper.createNewCartItemEntity(CART, PRODUCT, REQUEST.getQuantity()))
                    .thenReturn(EXISTING_CART_ITEM);

            underTest.updateCartItem(PRODUCT_ID, REQUEST, USER_AGENT);

            InOrder inOrder = inOrder(authService, productService, cartRepository, cartItemRepository, cartItemMapper,
                    cartItemRepository);
            inOrder.verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
            inOrder.verify(productService).validateAndGetProduct(eq(PRODUCT.getId()), anyString());
            inOrder.verify(cartRepository).findByUserId(eq(USER.getId()));
            inOrder.verify(cartItemRepository).findByCartIdAndProductId(eq(CART.getId()), eq(PRODUCT.getId()));
            inOrder.verify(cartItemMapper).createNewCartItemEntity(CART, PRODUCT, REQUEST.getQuantity());
            inOrder.verify(cartItemRepository).save(eq(EXISTING_CART_ITEM));
        }
    }

    @Nested
    @DisplayName("Remove item from cart tests")
    class RemoveItemFromCart {

        private static final CartItemEntity CART_ITEM = new CartItemEntity();

        @BeforeAll
        static void setUp() {
            CART_ITEM.setProduct(PRODUCT);
            CART_ITEM.setCart(CART);
        }

        private void mockRemoveItemFromCart(CartItemEntity cartItemEntity) {
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(cartRepository.findByUserId(eq(USER.getId()))).thenReturn(Optional.of(CART));
            when(cartItemRepository.findByCartIdAndProductId(eq(CART.getId()), eq(PRODUCT.getId())))
                    .thenReturn(cartItemEntity != null ? Optional.of(cartItemEntity) : Optional.empty());
        }

        @Test
        @DisplayName("Should delete cart item")
        void removeItemFromCart_ShouldDeleteCartItem() {
            mockRemoveItemFromCart(CART_ITEM);

            underTest.removeItemFromCart(PRODUCT_ID, USER_AGENT);

            InOrder inOrder = inOrder(authService, cartRepository, cartItemRepository, cartItemRepository);
            inOrder.verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
            inOrder.verify(cartRepository).findByUserId(eq(USER.getId()));
            inOrder.verify(cartItemRepository).findByCartIdAndProductId(eq(CART.getId()), eq(PRODUCT.getId()));
            inOrder.verify(cartItemRepository).delete(CART_ITEM);
        }

        @Test
        @DisplayName("Should throw not found exception")
        void removeItemFromCart_ShouldThrowNotFoundException() {
            mockRemoveItemFromCart(null);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> underTest.removeItemFromCart(PRODUCT_ID, USER_AGENT));

            assertThat(exception).isNotNull();

            InOrder inOrder = inOrder(authService, cartRepository, cartItemRepository);
            inOrder.verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
            inOrder.verify(cartRepository).findByUserId(eq(USER.getId()));
            inOrder.verify(cartItemRepository).findByCartIdAndProductId(eq(CART.getId()), eq(PRODUCT.getId()));
        }
    }

    @Nested
    @DisplayName("Clear cart tests")
    class ClearCart {

        private void mockClearCart(CartEntity cart) {
            when(authService.validateTokenAndGetUser(eq(TOKEN), anyString())).thenReturn(USER);
            when(cartRepository.findByUserId(eq(USER.getId())))
                    .thenReturn(cart != null ? Optional.of(cart) : Optional.empty());
        }

        @Test
        @DisplayName("Should throw cart not found")
        void clearCart_ShouldThrowCartNotFound() {
            mockClearCart(null);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> underTest.clearCart(USER_AGENT));

            assertThat(exception).isNotNull();

            InOrder inOrder = inOrder(authService, cartRepository);
            inOrder.verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
            inOrder.verify(cartRepository).findByUserId(eq(USER.getId()));
        }

        @Test
        @DisplayName("Should return number of deleted items")
        void clearCart_ShouldReturnNumberOfDeletedItems() {
            mockClearCart(CART);

            underTest.clearCart(USER_AGENT);

            InOrder inOrder = inOrder(authService, cartRepository, cartItemRepository);
            inOrder.verify(authService).validateTokenAndGetUser(eq(TOKEN), anyString());
            inOrder.verify(cartRepository).findByUserId(eq(USER.getId()));
            inOrder.verify(cartItemRepository).deleteAllByCartId(eq(CART.getId()));
        }
    }

    @Nested
    @DisplayName("Delete all by cart id tests")
    class DeleteAllByCartId {

        @Test
        @DisplayName("Should delete all by cart id")
        void deleteAllByCartId_ShouldDeleteAllByCartID() {
            when(cartItemRepository.deleteAllByCartId(CART.getId())).thenReturn(0);

            underTest.deleteAllByCartId(CART.getId());

            verify(cartItemRepository).deleteAllByCartId(CART.getId());
        }
    }
}