package com.ecommerce.service;

import com.ecommerce.domain.model.*;
import com.ecommerce.domain.repository.CartRepository;
import com.ecommerce.domain.repository.OrderRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class OrderService {
    
    @Inject
    OrderRepository orderRepository;
    
    @Inject
    CartRepository cartRepository;
    
    public Uni<List<Order>> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }
    
    public Uni<Order> getOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .onItem().ifNull().failWith(() ->
                new ResourceNotFoundException("Order not found"));
    }
    
    @Transactional
    public Uni<Order> createOrder(String userId, ShippingAddress shippingAddress) {
        return cartRepository.findByUserId(userId)
            .onItem().ifNull().failWith(() ->
                new ResourceNotFoundException("Cart not found"))
            .chain(cart -> {
                Order order = new Order();
                order.setUserId(userId);
                order.setOrderDate(LocalDateTime.now());
                order.setStatus(OrderStatus.PENDING);
                order.setTotalAmount(cart.getTotalAmount());
                order.setShippingAddress(shippingAddress);
                
                cart.getItems().forEach(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getPrice());
                    orderItem.setSubtotal(cartItem.getSubtotal());
                    order.getItems().add(orderItem);
                });
                
                return orderRepository.persist(order);
            });
    }
    
    @Transactional
    public Uni<Order> updateOrderStatus(Long orderId, OrderStatus status) {
        return orderRepository.findById(orderId)
            .onItem().ifNull().failWith(() ->
                new ResourceNotFoundException("Order not found"))
            .chain(order -> {
                order.setStatus(status);
                return orderRepository.persist(order);
            });
    }
}