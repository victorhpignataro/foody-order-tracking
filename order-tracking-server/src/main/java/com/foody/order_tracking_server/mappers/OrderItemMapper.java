package com.foody.order_tracking_server.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.foody.order_tracking_server.dto.orderitem.OrderItemDto;
import com.foody.order_tracking_server.model.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

  @Mapping(target = "orderId", source = "order.id")
  OrderItemDto entityToResponse(OrderItem entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "order", ignore = true)
  OrderItem orderItemResponseToEntity(OrderItemDto dto);
}