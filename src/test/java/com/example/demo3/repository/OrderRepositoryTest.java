package com.example.demo3.repository;

import com.example.demo3.entity.OrderEntity;
import com.example.demo3.entity.OrderStatus;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.testutil.BaseRepositoryTest;
import com.example.demo3.testutil.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderRepositoryTest extends BaseRepositoryTest<OrderRepository> {

    @Nested
    @DisplayName("Find by user id tests")
    class FindByUserId {

        @Test
        @DisplayName("Should find orders by user id")
        void findByUserId_ShouldFindOrdersByUserId() {
            UserEntity user = TestDataFactory.createAndPersistUser(entityManager);
            OrderEntity order = TestDataFactory.createAndPersistOrder(entityManager, user);

            Page<OrderEntity> result = underTest.findByUserId(user.getId(), PageRequest.of(0, 10));

            assertTrue(result.getContent().contains(order));
            assertEquals(0, result.getNumber());
            assertEquals(10, result.getSize());
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("Should not find orders by user id")
        void findByUserId_ShouldNotFindOrdersByUserId() {
            UserEntity user = TestDataFactory.createAndPersistUser(entityManager);

            Page<OrderEntity> result = underTest.findByUserId(user.getId(), PageRequest.of(0, 10));

            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getNumber());
            assertEquals(10, result.getSize());
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Find by status tests")
    class FindByStatus {

        @Test
        @DisplayName("Should find orders by status")
        void findByStatus_ShouldFindOrdersByStatus() {
            UserEntity user = TestDataFactory.createAndPersistUser(entityManager);
            OrderEntity order = TestDataFactory.createAndPersistOrderWithStatus(entityManager, user, OrderStatus.CONFIRMED);

            Page<OrderEntity> result = underTest.findByStatus(OrderStatus.CONFIRMED, PageRequest.of(0, 10));

            assertTrue(result.getContent().contains(order));
            assertEquals(0, result.getNumber());
            assertEquals(10, result.getSize());
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("Should not find orders by status")
        void findByStatus_ShouldNotFindOrdersByStatus() {
            Page<OrderEntity> result = underTest.findByStatus(OrderStatus.DELIVERED, PageRequest.of(0, 10));

            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getNumber());
            assertEquals(10, result.getSize());
            assertEquals(0, result.getTotalElements());
        }
    }
}