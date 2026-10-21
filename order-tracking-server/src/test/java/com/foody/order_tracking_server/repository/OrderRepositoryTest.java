package com.foody.order_tracking_server.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.foody.order_tracking_server.model.Order;
import com.foody.order_tracking_server.model.OrderItem;
import com.foody.order_tracking_server.model.OrderStatusEnum;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findById_WhenOrderExists_ShouldReturnOrder() {
        Order order = new Order();
        order.setClient("João Silva");
        order.setAddress("Rua A, 123");
        order.setStatus(OrderStatusEnum.RECEIVED);

        OrderItem item = new OrderItem("X-Burger", 2, new BigDecimal("25.00"));
        order.addItem(item);

        entityManager.persistAndFlush(order);

        Optional<Order> found = orderRepository.findById(order.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getClient()).isEqualTo("João Silva");
        assertThat(found.get().getItems()).hasSize(1);
        assertThat(found.get().getItems().get(0).getName()).isEqualTo("X-Burger");
    }

    @Test
    void findById_WhenOrderNotExists_ShouldReturnEmpty() {
        Optional<Order> found = orderRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        Order order1 = new Order();
        order1.setClient("João Silva");
        order1.setAddress("Rua A, 123");
        order1.setStatus(OrderStatusEnum.RECEIVED);

        Order order2 = new Order();
        order2.setClient("Maria Santos");
        order2.setAddress("Rua B, 456");
        order2.setStatus(OrderStatusEnum.DELIVERED);

        entityManager.persistAndFlush(order1);
        entityManager.persistAndFlush(order2);

        List<Order> orders = orderRepository.findAll();

        assertThat(orders).hasSize(2);
    }

    @Test
    void save_ShouldPersistOrderWithItems() {
        Order order = new Order();
        order.setClient("João Silva");
        order.setAddress("Rua A, 123");
        order.setStatus(OrderStatusEnum.RECEIVED);

        OrderItem item = new OrderItem("X-Burger", 2, new BigDecimal("25.00"));
        order.addItem(item);

        Order saved = orderRepository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getClient()).isEqualTo("João Silva");
        assertThat(saved.getItems()).hasSize(1);
        assertThat(saved.getItems().get(0).getOrder()).isSameAs(saved);
    }

    @Test
    void delete_ShouldRemoveOrderAndItems() {
        Order order = new Order();
        order.setClient("João Silva");
        order.setAddress("Rua A, 123");
        order.setStatus(OrderStatusEnum.RECEIVED);

        OrderItem item = new OrderItem("X-Burger", 2, new BigDecimal("25.00"));
        order.addItem(item);

        entityManager.persistAndFlush(order);
        Long orderId = order.getId();

        orderRepository.deleteById(orderId);
        entityManager.flush();

        Optional<Order> found = orderRepository.findById(orderId);
        assertThat(found).isEmpty();
    }

    @Test
    void updateStatus_ShouldPersistNewStatus() {
        Order order = new Order();
        order.setClient("João Silva");
        order.setAddress("Rua A, 123");
        order.setStatus(OrderStatusEnum.RECEIVED);

        entityManager.persistAndFlush(order);
        Long orderId = order.getId();

        order.setStatus(OrderStatusEnum.IN_PREPARATION);
        Order updated = orderRepository.save(order);

        assertThat(updated.getStatus()).isEqualTo(OrderStatusEnum.IN_PREPARATION);

        Optional<Order> found = orderRepository.findById(orderId);
        assertThat(found.get().getStatus()).isEqualTo(OrderStatusEnum.IN_PREPARATION);
    }
}