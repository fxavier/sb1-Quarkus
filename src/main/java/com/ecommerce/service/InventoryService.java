package com.ecommerce.service;

import com.ecommerce.domain.model.*;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class InventoryService {
    
    @Inject
    ProductRepository productRepository;
    
    @Inject
    EmailService emailService;
    
    @Transactional
    public Uni<InventoryTransaction> recordTransaction(Long productId, Integer quantity, TransactionType type, String reference) {
        return productRepository.findById(productId)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Product not found"))
            .chain(product -> {
                // Update product stock
                int newStock = product.getStockQuantity();
                switch (type) {
                    case PURCHASE:
                    case RESTOCK:
                    case RETURN:
                        newStock += quantity;
                        break;
                    case SALE:
                    case DAMAGED:
                        newStock -= quantity;
                        break;
                    case ADJUSTMENT:
                        newStock = quantity;
                        break;
                }
                
                if (newStock < 0) {
                    return Uni.createFrom().failure(
                        new IllegalStateException("Insufficient stock"));
                }
                
                product.setStockQuantity(newStock);
                
                // Create transaction record
                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setProduct(product);
                transaction.setQuantity(quantity);
                transaction.setType(type);
                transaction.setReference(reference);
                
                // Check for low stock
                if (newStock <= product.getLowStockThreshold()) {
                    notifyLowStock(product);
                }
                
                return transaction.persist()
                    .chain(t -> product.persist()
                        .map(p -> t));
            });
    }
    
    private void notifyLowStock(Product product) {
        StockAlert alert = new StockAlert();
        alert.setProduct(product);
        alert.setThreshold(product.getLowStockThreshold());
        alert.setActive(true);
        
        alert.persist()
            .subscribe().with(
                savedAlert -> emailService.sendLowStockAlert(product)
            );
    }
    
    public Uni<List<InventoryTransaction>> getProductTransactions(Long productId) {
        return InventoryTransaction.find("product.id", productId)
            .list();
    }
    
    public Uni<List<Product>> getLowStockProducts() {
        return Product.find("stockQuantity <= lowStockThreshold and active = true")
            .list();
    }
    
    @Transactional
    public Uni<Product> updateStockThreshold(Long productId, Integer threshold) {
        return productRepository.findById(productId)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Product not found"))
            .chain(product -> {
                product.setLowStockThreshold(threshold);
                return product.persist();
            });
    }
}