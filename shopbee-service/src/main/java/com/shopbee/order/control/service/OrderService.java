package com.shopbee.order.control.service;

import com.shopbee.order.model.CreateOrderRequest;
import com.shopbee.order.model.OrderDTO;

import java.util.List;

public interface OrderService {

    OrderDTO createOrder(String tenantId, String userId, CreateOrderRequest createOrderRequest);

    List<OrderDTO> getOrders(String tenantId, String userId, Integer offset, Integer limit);

    OrderDTO getOrderById(String tenantId, String userId, String orderId);

    OrderDTO cancelOrder(String tenantId, String userId, String orderId);

    void updateOrderStatus(String tenantId, String orderId, String status);
}
