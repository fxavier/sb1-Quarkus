package com.ecommerce.service;

import com.ecommerce.domain.model.Order;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class PaymentService {
    
    @Inject
    OrderService orderService;
    
    public Uni<PaymentIntent> createPaymentIntent(Order order) {
        return Uni.createFrom().item(() -> {
            try {
                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(order.getTotalAmount().multiply(new java.math.BigDecimal(100)).longValue())
                    .setCurrency("usd")
                    .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build()
                    )
                    .putMetadata("orderId", order.getId().toString())
                    .build();
                
                return PaymentIntent.create(params);
            } catch (StripeException e) {
                throw new RuntimeException("Error creating payment intent", e);
            }
        });
    }
    
    public Uni<PaymentIntent> confirmPayment(String paymentIntentId) {
        return Uni.createFrom().item(() -> {
            try {
                PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
                Map<String, Object> params = new HashMap<>();
                params.put("payment_method", "pm_card_visa"); // For testing only
                paymentIntent.confirm(params);
                return paymentIntent;
            } catch (StripeException e) {
                throw new RuntimeException("Error confirming payment", e);
            }
        });
    }
}