package com.foody.order_tracking_server.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "orders")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "client")
  @Size(max = 150, message = "O cliente deve conter no máximo 150 caracteres")
  private String client;

  @Column(name = "address")
  @Size(max = 256, message = "O endereço deve conter no máximo 256 caracteres")
  private String address;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> items = new ArrayList<>();

  @Column(nullable = false)
  private OrderStatusEnum status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public Order() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getClient() {
    return client;
  }

  public void setClient(String client) {
    this.client = client;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public List<OrderItem> getItems() {
    return items;
  }

  public void setItems(List<OrderItem> items) {
    this.items = items;
  }

  public void addItem(OrderItem item) {
    items.add(item);
    item.setOrder(this);
  }

  public void removeItem(OrderItem item) {
    items.remove(item);
    item.setOrder(null);
  }

  public OrderStatusEnum getStatus() {
    return status;
  }

  public void setStatus(OrderStatusEnum status) {
    this.status = status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

}
