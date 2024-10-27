package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.Cart;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CartRepository implements PanacheRepository<Cart> {
    
    public Uni<Cart> findByUserId(String userId) {
        return find("userId", userId).firstResult();
    }
}