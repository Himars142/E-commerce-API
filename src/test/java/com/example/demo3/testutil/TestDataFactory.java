package com.example.demo3.testutil;

import com.example.demo3.entity.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

public class TestDataFactory {
    private static final AtomicLong USER_COUNTER = new AtomicLong(1);
    private static final AtomicLong PRODUCT_COUNTER = new AtomicLong(1);
    private static final AtomicLong ORDER_COUNTER = new AtomicLong(1);

    public static UserEntity createAndPersistUser(TestEntityManager entityManager) {
        return createAndPersistUser(entityManager,
                "username" + USER_COUNTER.getAndIncrement(),
                "user" + USER_COUNTER.get() + "@test.com");
    }

    public static UserEntity createAndPersistUser(TestEntityManager entityManager,
                                                  String username, String email) {
        return createAndPersistUser(entityManager, username, email, "password123");
    }

    public static UserEntity createAndPersistUser(TestEntityManager entityManager,
                                                  String username, String email, String password) {
        UserEntity user = new UserEntity(username, email, password);
        return entityManager.persistAndFlush(user);
    }


    public static UserEntity createUserWithoutPersist(String username, String email) {
        return new UserEntity(username, email, "password123");
    }

    public static ProductEntity createAndPersistProduct(TestEntityManager entityManager) {
        long counter = PRODUCT_COUNTER.getAndIncrement();
        return createAndPersistProduct(entityManager,
                "SKU-" + counter,
                "Product " + counter,
                BigDecimal.valueOf(10.99));
    }

    public static ProductEntity createAndPersistProduct(TestEntityManager entityManager, String sku) {
        return createAndPersistProduct(entityManager, sku, "Product " + sku, BigDecimal.valueOf(10.99));
    }

    public static ProductEntity createAndPersistProduct(TestEntityManager entityManager,
                                                        String sku, String name, BigDecimal price) {
        ProductEntity product = new ProductEntity();
        product.setSku(sku);
        product.setName(name);
        product.setPrice(price);
        return entityManager.persistAndFlush(product);
    }

    public static ProductEntity createAndPersistProductWithCategory(TestEntityManager entityManager,
                                                                    CategoryEntity category) {
        ProductEntity product = createAndPersistProduct(entityManager);
        product.setCategory(category);
        return entityManager.persistAndFlush(product);
    }

    public static CategoryEntity createAndPersistCategory(TestEntityManager entityManager) {
        return createAndPersistCategory(entityManager, "Test Category");
    }

    public static CategoryEntity createAndPersistCategory(TestEntityManager entityManager, String name) {
        CategoryEntity category = new CategoryEntity();
        category.setName(name);
        return entityManager.persistAndFlush(category);
    }

    public static CartEntity createAndPersistCart(TestEntityManager entityManager, UserEntity user) {
        CartEntity cart = new CartEntity(user);
        return entityManager.persistAndFlush(cart);
    }

    public static CartEntity createAndPersistCartWithUser(TestEntityManager entityManager) {
        UserEntity user = createAndPersistUser(entityManager);
        return createAndPersistCart(entityManager, user);
    }

    public static CartItemEntity createAndPersistCartItem(TestEntityManager entityManager,
                                                          CartEntity cart,
                                                          ProductEntity product,
                                                          int quantity) {
        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        return entityManager.persistAndFlush(cartItem);
    }

    public static CartItemEntity createAndPersistCartItemComplete(TestEntityManager entityManager) {
        UserEntity user = createAndPersistUser(entityManager);
        CartEntity cart = createAndPersistCart(entityManager, user);
        ProductEntity product = createAndPersistProduct(entityManager);
        return createAndPersistCartItem(entityManager, cart, product, 2);
    }

    public static OrderEntity createAndPersistOrder(TestEntityManager entityManager, UserEntity user) {
        long counter = ORDER_COUNTER.getAndIncrement();
        return createAndPersistOrder(entityManager, user,
                "ORDER-" + counter,
                BigDecimal.valueOf(100.00),
                "Test Address " + counter);
    }

    public static OrderEntity createAndPersistOrder(TestEntityManager entityManager,
                                                    UserEntity user, String orderNumber) {
        return createAndPersistOrder(entityManager, user, orderNumber,
                BigDecimal.valueOf(100.00), "Test Address");
    }

    public static OrderEntity createAndPersistOrder(TestEntityManager entityManager,
                                                    UserEntity user,
                                                    String orderNumber,
                                                    BigDecimal totalAmount,
                                                    String shippingAddress) {
        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setOrderNumber(orderNumber);
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(shippingAddress);
        order.setStatus(OrderStatus.PENDING);
        return entityManager.persistAndFlush(order);
    }

    public static OrderEntity createAndPersistOrderWithStatus(TestEntityManager entityManager,
                                                              UserEntity user,
                                                              OrderStatus status) {
        OrderEntity order = createAndPersistOrder(entityManager, user);
        order.setStatus(status);
        return entityManager.persistAndFlush(order);
    }

    public static OrderItemEntity createAndPersistOrderItem(TestEntityManager entityManager,
                                                            OrderEntity order,
                                                            ProductEntity product,
                                                            int quantity,
                                                            BigDecimal unitPrice) {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(unitPrice);
        orderItem.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        return entityManager.persistAndFlush(orderItem);
    }


    public static OrderItemEntity createAndPersistOrderItem(TestEntityManager entityManager,
                                                            OrderEntity order,
                                                            ProductEntity product) {
        return createAndPersistOrderItem(entityManager, order, product, 1, BigDecimal.TEN);
    }

    public static void resetCounters() {
        USER_COUNTER.set(1);
        PRODUCT_COUNTER.set(1);
        ORDER_COUNTER.set(1);
    }

    public static UserEntity[] createAndPersistUsers(TestEntityManager entityManager, int count) {
        UserEntity[] users = new UserEntity[count];
        for (int i = 0; i < count; i++) {
            users[i] = createAndPersistUser(entityManager);
        }
        return users;
    }

    public static ProductEntity[] createAndPersistProducts(TestEntityManager entityManager, int count) {
        ProductEntity[] products = new ProductEntity[count];
        for (int i = 0; i < count; i++) {
            products[i] = createAndPersistProduct(entityManager);
        }
        return products;
    }
}
