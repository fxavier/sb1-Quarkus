package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.Review;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class ReviewRepository implements PanacheRepository<Review> {
    
    public Uni<List<Review>> findByProduct(Long productId) {
        return list("product.id", productId);
    }
    
    public Uni<List<Review>> findByUser(String userId) {
        return list("userId", userId);
    }
    
    public Uni<Double> getAverageRating(Long productId) {
        return find("product.id", productId)
            .stream()
            .collect().asList()
            .map(reviews -> reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0));
    }
}