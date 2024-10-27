package com.ecommerce.service;

import com.ecommerce.domain.model.Favorite;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.repository.FavoriteRepository;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class FavoriteService {
    
    @Inject
    FavoriteRepository favoriteRepository;
    
    @Inject
    ProductRepository productRepository;
    
    public Uni<List<Favorite>> getUserFavorites(String userId) {
        return favoriteRepository.findByUser(userId);
    }
    
    public Uni<Boolean> isFavorite(String userId, Long productId) {
        return favoriteRepository.isFavorite(userId, productId);
    }
    
    @Transactional
    public Uni<Favorite> addToFavorites(String userId, Long productId) {
        return productRepository.findById(productId)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Product not found"))
            .chain(product -> favoriteRepository.isFavorite(userId, productId)
                .chain(exists -> {
                    if (exists) {
                        return Uni.createFrom().failure(
                            new IllegalStateException("Product already in favorites"));
                    }
                    
                    Favorite favorite = new Favorite();
                    favorite.setUserId(userId);
                    favorite.setProduct(product);
                    return favoriteRepository.persist(favorite);
                }));
    }
    
    @Transactional
    public Uni<Boolean> removeFromFavorites(String userId, Long productId) {
        return favoriteRepository.removeByUserAndProduct(userId, productId);
    }
}