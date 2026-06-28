package com.shopbee.order.control.mapper;

import com.shopbee.order.entity.Order;
import com.shopbee.order.entity.OrderItem;
import com.shopbee.order.model.OrderDTO;
import com.shopbee.order.model.OrderItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    OrderDTO toOrderDTO(Order order);

    List<OrderDTO> toOrders(List<Order> orders);

    OrderItemDTO toOrderItemDTO(OrderItem orderItem);
}
