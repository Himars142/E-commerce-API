package com.example.demo3.repository.testutil;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
public abstract class BaseRepositoryTest<T> {

    @Autowired
    protected T underTest;

    @Autowired
    protected TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        TestDataFactory.resetCounters();
    }

    protected void clearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }
}