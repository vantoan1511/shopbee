package com.shopbee.order.control.service.impl;

import com.shopbee.common.exception.ApiServiceException;
import com.shopbee.order.control.mapper.OrderMapper;
import com.shopbee.order.control.repository.OrderRepository;
import com.shopbee.order.control.service.OrderService;
import com.shopbee.order.entity.Order;
import com.shopbee.order.entity.OrderItem;
import com.shopbee.order.model.CreateOrderRequest;
import com.shopbee.order.model.OrderDTO;
import com.shopbee.product.control.service.ProductService;
import com.shopbee.product.model.ProductDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderServiceImpl implements OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_PAGE_INDEX = 0;

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final OrderMapper orderMapper;

    @Inject
    public OrderServiceImpl(OrderRepository orderRepository, ProductService productService, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public OrderDTO createOrder(String tenantId, String userId, CreateOrderRequest createOrderRequest) {
        LOG.info("Creating new order for user [{}]", userId);

        // 1. Fetch product details and calculate total price
        List<OrderItem> orderItems = createOrderRequest.getItems().stream()
                .map(itemRequest -> {
                    ProductDTO product = productService.getProductById(tenantId, itemRequest.getProductId());
                    if (product.getStockQuantity() < itemRequest.getQuantity()) {
                        throw ApiServiceException.badRequest("Not enough stock for product [{}]. Requested: {}, Available: {}",
                                product.getId(), itemRequest.getQuantity(), product.getStockQuantity());
                    }
                    OrderItem orderItem = new OrderItem();
                    orderItem.setTenantId(tenantId);
                    orderItem.setProductId(product.getId());
                    orderItem.setQuantity(itemRequest.getQuantity());
                    orderItem.setPrice(product.getPrice());
                    return orderItem;
                }).collect(Collectors.toList());

        double totalPrice = orderItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // 2. Reserve stock
        productService.reserveStock(tenantId, orderItems.stream().map(orderMapper::toOrderItemDTO).collect(Collectors.toList()));

        // 3. Create and save the order
        Order order = new Order();
        order.setTenantId(tenantId);
        order.setUserId(userId);
        order.setItems(orderItems);
        order.setTotalPrice(totalPrice);
        order.setStatus(Order.Status.CREATED);
        orderItems.forEach(item -> item.setOrder(order));

        orderRepository.persist(order);

        return orderMapper.toOrderDTO(order);
    }

    @Override
    public List<OrderDTO> getOrders(String tenantId, String userId, Integer offset, Integer limit) {
        int page = Optional.ofNullable(offset).orElse(DEFAULT_PAGE_INDEX);
        int size = Optional.ofNullable(limit).orElse(DEFAULT_PAGE_SIZE);
        List<Order> orders = orderRepository.findByUserId(tenantId, userId, page, size);
        return orderMapper.toOrders(orders);
    }

    @Override
    public OrderDTO getOrderById(String tenantId, String userId, String orderId) {
        Order order = findOrderByIdAndUserId(tenantId, userId, orderId);
        return orderMapper.toOrderDTO(order);
    }

    @Override
    @Transactional
    public OrderDTO cancelOrder(String tenantId, String userId, String orderId) {
        Order order = findOrderByIdAndUserId(tenantId, userId, orderId);

        if (order.getStatus() == Order.Status.CREATED || order.getStatus() == Order.Status.PENDING_PAYMENT) {
            order.setStatus(Order.Status.CANCELLED);
            productService.releaseStock(tenantId, order.getItems().stream().map(orderMapper::toOrderItemDTO).collect(Collectors.toList()));
        } else {
            throw ApiServiceException.badRequest("Order [{}] cannot be cancelled in its current state: {}", orderId, order.getStatus());
        }

        return orderMapper.toOrderDTO(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(String tenantId, String orderId, String status) {
        // This is an internal method, so we find by ID only
        Order order = orderRepository.findById(tenantId, orderId);
        if (order == null) {
            throw ApiServiceException.notFound("Order [{}] not found", orderId);
        }
        LOG.info("Updating status of order [{}] to [{}]", orderId, status);
        order.setStatus(Order.Status.valueOf(status));
    }

    private Order findOrderByIdAndUserId(String tenantId, String userId, String orderId) {
        Order order = orderRepository.findById(tenantId, orderId);
        if (order == null) {
            throw ApiServiceException.notFound("Order [{}] not found", orderId);
        }
        if (!order.getUserId().equals(userId)) {
            // Prevent users from accessing others' orders
            throw ApiServiceException.forbidden("Access to order [{}] is forbidden", orderId);
        }
        return order;
    }
}
