package com.ecommerce.service;

import com.ecommerce.domain.model.Cart;
import com.ecommerce.domain.model.CartItem;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.repository.CartRepository;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;

@ApplicationScoped
public class CartService {
    
    @Inject
    CartRepository cartRepository;
    
    @Inject
    ProductRepository productRepository;
    
    public Uni<Cart> getCart(String userId) {
        return cartRepository.findByUserId(userId)
            .onItem().ifNull().switchTo(() -> createCart(userId));
    }
    
    @Transactional
    public Uni<Cart> addToCart(String userId, Long productId, Integer quantity) {
        return Uni.combine().all().unis(
            getCart(userId),
            productRepository.findById(productId)
        ).asTuple()
        .chain(tuple -> {
            Cart cart = tuple.getItem1();
            Product product = tuple.getItem2();
            
            if (product == null) {
                return Uni.createFrom().failure(
                    new ResourceNotFoundException("Product not found"));
            }
            
            CartItem cartItem = findOrCreateCartItem(cart, product);
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setPrice(product.getPrice());
            cartItem.setSubtotal(product.getPrice().multiply(
                BigDecimal.valueOf(cartItem.getQuantity())));
            
            updateCartTotal(cart);
            
            return cartRepository.persist(cart);
        });
    }
    
    private CartItem findOrCreateCartItem(Cart cart, Product product) {
        return cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(product.getId()))
            .findFirst()
            .orElseGet(() -> {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(0);
                cart.getItems().add(newItem);
                return newItem;
            });
    }
    
    private void updateCartTotal(Cart cart) {
        cart.setTotalAmount(cart.getItems().stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    
    private Uni<Cart> createCart(String userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.persist(cart);
    }
}