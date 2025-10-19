package com.shopbee.order.boundary.api;

import com.shopbee.order.control.service.OrderService;
import com.shopbee.order.model.CreateOrderRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

public class OrderApiImpl implements OrdersApi {

    @Inject
    OrderService orderService;

    @Context
    UriInfo uriInfo;

    @Override
    public Response createOrder(String tenantId, CreateOrderRequest createOrderRequest) {
        // In a real app, the user ID would come from the JWT token (SecurityContext)
        String userId = "temp-user-id"; // Placeholder
        var order = orderService.createOrder(tenantId, userId, createOrderRequest);
        URI location = uriInfo.getAbsolutePathBuilder().path(order.getId()).build();
        return Response.created(location).entity(order).build();
    }

    @Override
    public Response getOrders(String tenantId, Integer offset, Integer limit) {
        String userId = "temp-user-id"; // Placeholder
        var orders = orderService.getOrders(tenantId, userId, offset, limit);
        return Response.ok(orders).build();
    }

    @Override
    public Response getOrderById(String tenantId, String orderId) {
        String userId = "temp-user-id"; // Placeholder
        var order = orderService.getOrderById(tenantId, userId, orderId);
        return Response.ok(order).build();
    }

    @Override
    public Response cancelOrder(String tenantId, String orderId) {
        String userId = "temp-user-id"; // Placeholder
        var order = orderService.cancelOrder(tenantId, userId, orderId);
        return Response.ok(order).build();
    }
}
