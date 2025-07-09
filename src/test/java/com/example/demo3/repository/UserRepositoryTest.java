package com.example.demo3.repository;

import com.example.demo3.entity.UserEntity;
import com.example.demo3.testutil.BaseRepositoryTest;
import com.example.demo3.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository underTest;

    @Test
    void findByUsername_ShouldFindUSerByUsername() {
        String username = "username";
        UserEntity user = TestDataFactory.createAndPersistUser(entityManager, username, "email@gmail.com");

        Optional<UserEntity> result = underTest.findByUsername(username);

        assertTrue(result.isPresent());
        assertEquals(result.get().getId(), user.getId());
    }

    @Test
    void findByUsername_ShouldNotFindUSerByUsername() {
        Optional<UserEntity> result = underTest.findByUsername("username");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmail_ShouldFindUserByEmail() {
        String email = "email@gmail.com";
        UserEntity user = TestDataFactory.createAndPersistUser(entityManager, "username", email);

        Optional<UserEntity> result = underTest.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(result.get().getId(), user.getId());
    }

    @Test
    void findByEmail_ShouldNotFindUserByEmail() {
        Optional<UserEntity> result = underTest.findByEmail("email@gmail.com");

        assertTrue(result.isEmpty());
    }
}