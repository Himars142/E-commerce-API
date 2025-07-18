package com.example.demo3.repository;

import com.example.demo3.entity.UserEntity;
import com.example.demo3.testutil.BaseRepositoryTest;
import com.example.demo3.testutil.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest extends BaseRepositoryTest<UserRepository> {

    @Nested
    @DisplayName("Find by username tests")
    class FindByUsername {

        @Test
        @DisplayName("Should find user by username")
        void findByUsername_ShouldFindUserByUsername() {
            String username = "username";
            UserEntity user = TestDataFactory.createAndPersistUser(entityManager, username, "email@gmail.com");

            Optional<UserEntity> result = underTest.findByUsername(username);

            assertTrue(result.isPresent());
            assertEquals(result.get().getId(), user.getId());
        }

        @Test
        @DisplayName("Should not find user by username")
        void findByUsername_ShouldNotFindUSerByUsername() {
            Optional<UserEntity> result = underTest.findByUsername("username");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find by email tests")
    class FindByEmail {

        @Test
        @DisplayName("Should find user by email")
        void findByEmail_ShouldFindUserByEmail() {
            String email = "email@gmail.com";
            UserEntity user = TestDataFactory.createAndPersistUser(entityManager, "username", email);

            Optional<UserEntity> result = underTest.findByEmail(email);

            assertTrue(result.isPresent());
            assertEquals(result.get().getId(), user.getId());
        }

        @Test
        @DisplayName("Should not find user by email")
        void findByEmail_ShouldNotFindUserByEmail() {
            Optional<UserEntity> result = underTest.findByEmail("email@gmail.com");

            assertTrue(result.isEmpty());
        }
    }
}