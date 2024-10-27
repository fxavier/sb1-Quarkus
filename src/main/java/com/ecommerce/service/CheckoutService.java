package com.ecommerce.service;

import com.ecommerce.domain.model.Order;
import com.ecommerce.domain.model.OrderStatus;
import com.ecommerce.domain.model.ShippingAddress;
import com.ecommerce.domain.model.User;
import com.stripe.model.PaymentIntent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CheckoutService {
    
    @Inject
    OrderService orderService;
    
    @Inject
    PaymentService paymentService;
    
    @Inject
    EmailService emailService;
    
    @Transactional
    public Uni<Order> initiateCheckout(String userId, ShippingAddress shippingAddress) {
        return orderService.createOrder(userId, shippingAddress)
            .chain(order -> paymentService.createPaymentIntent(order)
                .map(paymentIntent -> {
                    order.setPaymentIntentId(paymentIntent.getId());
                    return order;
                }));
    }
    
    @Transactional
    public Uni<Order> completeCheckout(Long orderId, String paymentIntentId) {
        return paymentService.confirmPayment(paymentIntentId)
            .chain(paymentIntent -> {
                if ("succeeded".equals(paymentIntent.getStatus())) {
                    return orderService.updateOrderStatus(orderId, OrderStatus.PAID)
                        .chain(order -> User.<User>findById(order.getUserId())
                            .chain(user -> {
                                emailService.sendOrderConfirmation(order, user);
                                return Uni.createFrom().item(order);
                            }));
                } else {
                    return orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED);
                }
            });
    }
}