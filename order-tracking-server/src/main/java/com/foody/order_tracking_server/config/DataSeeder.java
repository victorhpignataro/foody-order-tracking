package com.foody.order_tracking_server.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.foody.order_tracking_server.model.Order;
import com.foody.order_tracking_server.model.OrderItem;
import com.foody.order_tracking_server.repository.OrderRepository;

import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@Configuration
@Profile("dev")
public class DataSeeder {

  @Bean
  CommandLineRunner initDatabase(OrderRepository orderRepository) {
    return args -> {
      System.out.println("Executando seeds...");

      if (orderRepository.count() == 0) {
        PodamFactory factory = new PodamFactoryImpl();
        List<Order> orders = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
          Order order = factory.manufacturePojo(Order.class);
          order.setId(null);

          OrderItem orderItem = factory.manufacturePojo(OrderItem.class);
          orderItem.setId(null);
          orderItem.setOrder(order);

          List<OrderItem> items = new ArrayList<>();
          items.add(orderItem);
          order.setItems(items);

          orders.add(order);
        }

        orderRepository.saveAll(orders);
        System.out.println(">> Seeds de pedidos inseridos com sucesso!");
      }
    };
  }
}