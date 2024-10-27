package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.Favorite;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FavoriteRepository implements PanacheRepository<Favorite> {
    
    public Uni<List<Favorite>> findByUser(String userId) {
        return list("userId", userId);
    }
    
    public Uni<Boolean> isFavorite(String userId, Long productId) {
        return count("userId = ?1 and product.id = ?2", userId, productId)
            .map(count -> count > 0);
    }
    
    public Uni<Boolean> removeByUserAndProduct(String userId, Long productId) {
        return delete("userId = ?1 and product.id = ?2", userId, productId)
            .map(count -> count > 0);
    }
}