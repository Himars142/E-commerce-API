package com.example.demo3.repository;

import com.example.demo3.entity.CartEntity;
import com.example.demo3.entity.CartItemEntity;
import com.example.demo3.entity.ProductEntity;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.repository.testutil.BaseRepositoryTest;
import com.example.demo3.repository.testutil.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CartItemRepositoryTest extends BaseRepositoryTest<CartItemRepository> {

    @Nested
    @DisplayName("Delete all by cart id tests")
    class DeleteAllByCartId {

        @Test
        @DisplayName("Should delete all items for cart")
        void deleteAllByCartId_ShouldDeleteAllItemsForCart() {
            CartItemEntity item1 = TestDataFactory.createAndPersistCartItemComplete(entityManager);
            CartItemEntity item2 = TestDataFactory.createAndPersistCartItem(
                    entityManager, item1.getCart(),
                    TestDataFactory.createAndPersistProduct(entityManager), 3);
            CartItemEntity item3 = TestDataFactory.createAndPersistCartItemComplete(entityManager);

            int deletedItems = underTest.deleteAllByCartId(item1.getCart().getId());

            assertFalse(underTest.existsById(item1.getId()));
            assertFalse(underTest.existsById(item2.getId()));
            assertTrue(underTest.existsById(item3.getId()));
            assertEquals(2, deletedItems);
        }
    }

    @Nested
    @DisplayName("Find by cart id and product id tests")
    class FindByCartIdAndProductId {

        @Test
        @DisplayName("Should return item when exists")
        void findByCartIdAndProductId_ShouldReturnItem_WhenExists() {
            CartEntity cart = TestDataFactory.createAndPersistCartWithUser(entityManager);
            ProductEntity product = TestDataFactory.createAndPersistProduct(entityManager);
            CartItemEntity cartItem = TestDataFactory.createAndPersistCartItem(entityManager, cart, product, 2);

            Optional<CartItemEntity> result = underTest.findByCartIdAndProductId(cart.getId(), product.getId());

            assertTrue(result.isPresent());
            assertEquals(cartItem.getId(), result.get().getId());
            assertEquals(2, result.get().getQuantity());
            assertEquals(cartItem.getCart().getId(), result.get().getCart().getId());
            assertEquals(cartItem.getProduct().getId(), result.get().getProduct().getId());
        }

        @Test
        @DisplayName("Should return empty when not exists")
        void findByCartIdAndProductId_ShouldReturnEmpty_WhenNotExists() {
            UserEntity user = TestDataFactory.createAndPersistUser(entityManager);
            CartEntity cart = TestDataFactory.createAndPersistCart(entityManager, user);
            ProductEntity product = TestDataFactory.createAndPersistProduct(entityManager);

            Optional<CartItemEntity> result = underTest.findByCartIdAndProductId(cart.getId(), product.getId());

            assertTrue(result.isEmpty());
        }
    }
}