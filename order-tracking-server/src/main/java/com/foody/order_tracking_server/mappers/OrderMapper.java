package com.foody.order_tracking_server.mappers;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.foody.order_tracking_server.dto.order.OrderCreateDto;
import com.foody.order_tracking_server.dto.order.OrderDto;
import com.foody.order_tracking_server.model.Order;
import com.foody.order_tracking_server.model.OrderItem;

@Mapper(componentModel = "spring", uses = { OrderItemMapper.class })
public interface OrderMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "status", ignore = true)
  Order createDtoToEntity(OrderCreateDto dto);

  @AfterMapping
  default void linkItemsToOrder(@MappingTarget Order order) {
    if (order.getItems() != null) {
      for (OrderItem item : order.getItems()) {
        item.setOrder(order);
      }
    }
  }

  List<OrderDto> entityListToDtoList(List<Order> entityList);

  OrderDto entityToDto(Order entity);

}