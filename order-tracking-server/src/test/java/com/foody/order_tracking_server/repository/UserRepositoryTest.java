package com.foody.order_tracking_server.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.foody.order_tracking_server.model.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        User user = new User();
        user.setName("João Silva");
        user.setEmail("joao@email.com");
        user.setPassword("encodedPassword");

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("joao@email.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("joao@email.com");
        assertThat(found.get().getName()).isEqualTo("João Silva");
    }

    @Test
    void findByEmail_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("nonexistent@email.com");

        assertThat(found).isEmpty();
    }

    @Test
    void save_ShouldPersistUser() {
        User user = new User();
        user.setName("João Silva");
        user.setEmail("joao@email.com");
        user.setPassword("encodedPassword");

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("joao@email.com");
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        User user = new User();
        user.setName("João Silva");
        user.setEmail("joao@email.com");
        user.setPassword("encodedPassword");

        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("joao@email.com");
    }

    @Test
    void delete_ShouldRemoveUser() {
        User user = new User();
        user.setName("João Silva");
        user.setEmail("joao@email.com");
        user.setPassword("encodedPassword");

        entityManager.persistAndFlush(user);
        Long userId = user.getId();

        userRepository.deleteById(userId);
        entityManager.flush();

        Optional<User> found = userRepository.findById(userId);
        assertThat(found).isEmpty();
    }

    @Test
    void emailShouldBeUnique() {
        User user1 = new User();
        user1.setName("João Silva");
        user1.setEmail("joao@email.com");
        user1.setPassword("encodedPassword");

        User user2 = new User();
        user2.setName("Maria Santos");
        user2.setEmail("joao@email.com"); // Same email
        user2.setPassword("encodedPassword");

        entityManager.persistAndFlush(user1);

        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(user2);
        }).isInstanceOf(Exception.class);
    }
}