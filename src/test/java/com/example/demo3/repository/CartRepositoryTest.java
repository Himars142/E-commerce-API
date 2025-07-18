package com.example.demo3.repository;

import com.example.demo3.entity.CartEntity;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.testutil.BaseRepositoryTest;
import com.example.demo3.testutil.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartRepositoryTest extends BaseRepositoryTest<CartRepository> {
    @Nested
    @DisplayName("Find by user id tests")
    class FindByUserId {

        @Test
        @DisplayName("Should find cart by user id")
        void findByUserId_ShouldFindCartByUserId() {
            UserEntity user = TestDataFactory.createAndPersistUser(entityManager);
            CartEntity cart = TestDataFactory.createAndPersistCart(entityManager, user);

            Optional<CartEntity> result = underTest.findByUserId(user.getId());

            assertTrue(result.isPresent());
            assertEquals(cart.getId(), result.get().getId());
        }

        @Test
        @DisplayName("Should not find cart by user id")
        void findByUserId_ShouldNotFindCartByUserId() {
            UserEntity user = TestDataFactory.createAndPersistUser(entityManager);

            Optional<CartEntity> result = underTest.findByUserId(user.getId());

            assertTrue(result.isEmpty());
        }
    }
}