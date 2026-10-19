package com.foody.order_tracking_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foody.order_tracking_server.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}