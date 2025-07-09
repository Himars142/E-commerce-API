package com.example.demo3.repository;

import com.example.demo3.entity.OrderEntity;
import com.example.demo3.entity.OrderItemEntity;
import com.example.demo3.entity.ProductEntity;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.testutil.BaseRepositoryTest;
import com.example.demo3.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderItemRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private OrderItemRepository underTest;

    @Test
    void findByOrder_Id_ShouldFindOrderItemsByOrderId() {
        UserEntity user = TestDataFactory.createAndPersistUser(entityManager);
        OrderEntity order = TestDataFactory.createAndPersistOrder(entityManager, user);
        ProductEntity product = TestDataFactory.createAndPersistProduct(entityManager);
        OrderItemEntity orderItemEntity1 = TestDataFactory.createAndPersistOrderItem(entityManager, order, product);
        OrderItemEntity orderItemEntity2 = TestDataFactory.createAndPersistOrderItem(entityManager, order, product);
        OrderItemEntity orderItemEntity3 = TestDataFactory.createAndPersistOrderItem(entityManager, order, product);

        underTest.findByOrder_Id(order.getId());

        Optional<List<OrderItemEntity>> result = underTest.findByOrder_Id(order.getId());

        assertTrue(result.isPresent());
        assertTrue(result.get().contains(orderItemEntity1));
        assertTrue(result.get().contains(orderItemEntity2));
        assertTrue(result.get().contains(orderItemEntity3));
    }

    @Test
    void findByOrder_Id_ShouldNotFindOrderItemsByOrderId() {
        UserEntity user = TestDataFactory.createAndPersistUser(entityManager);
        OrderEntity order = TestDataFactory.createAndPersistOrder(entityManager, user);

        Optional<List<OrderItemEntity>> result = underTest.findByOrder_Id(order.getId());

        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }
}