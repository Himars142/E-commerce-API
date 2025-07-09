package com.example.demo3.repository;

import com.example.demo3.entity.OrderEntity;
import com.example.demo3.entity.OrderStatus;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.testutil.BaseRepositoryTest;
import com.example.demo3.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private OrderRepository underTest;

    @Test
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
    void findByUserId_ShouldNOTFindOrdersByUserId() {
        UserEntity user = TestDataFactory.createAndPersistUser(entityManager);

        Page<OrderEntity> result = underTest.findByUserId(user.getId(), PageRequest.of(0, 10));

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getTotalElements());
    }

    @Test
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
    void findByStatus_ShouldNOTFindOrdersByStatus() {
        Page<OrderEntity> result = underTest.findByStatus(OrderStatus.DELIVERED, PageRequest.of(0, 10));

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getTotalElements());
    }
}