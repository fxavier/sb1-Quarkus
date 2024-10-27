package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.Coupon;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CouponRepository implements PanacheRepository<Coupon> {
    
    public Uni<Coupon> findByCode(String code) {
        return find("code", code).firstResult();
    }
    
    public Uni<List<Coupon>> findActive() {
        LocalDateTime now = LocalDateTime.now();
        return list("active = true and startDate <= ?1 and endDate >= ?1", now);
    }
    
    public Uni<List<Coupon>> findByCategory(Long categoryId) {
        return list("select c from Coupon c join c.applicableCategories cat where cat.id = ?1", categoryId);
    }
    
    public Uni<List<Coupon>> findByProduct(Long productId) {
        return list("select c from Coupon c join c.applicableProducts p where p.id = ?1", productId);
    }
}