package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.Order;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {
    
    public Uni<List<Order>> findByUserId(String userId) {
        return list("userId", userId);
    }
}