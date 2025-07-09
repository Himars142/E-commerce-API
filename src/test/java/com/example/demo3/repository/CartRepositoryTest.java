package com.example.demo3.repository;

import com.example.demo3.entity.CartEntity;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.testutil.BaseRepositoryTest;
import com.example.demo3.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CartRepository underTest;

    @Test
    void findByUserId_ShouldFindCartByUserId() {
        UserEntity user = TestDataFactory.createAndPersistUser(entityManager);
        CartEntity cart = TestDataFactory.createAndPersistCart(entityManager, user);

        Optional<CartEntity> result = underTest.findByUserId(user.getId());

        assertTrue(result.isPresent());
        assertEquals(cart.getId(), result.get().getId());
    }

    @Test
    void findByUserId_ShouldNotFindCartByUserId() {
        UserEntity user = TestDataFactory.createAndPersistUser(entityManager);

        Optional<CartEntity> result = underTest.findByUserId(user.getId());

        assertTrue(result.isEmpty());
    }
}