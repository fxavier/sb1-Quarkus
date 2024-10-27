package com.ecommerce.service;

import com.ecommerce.domain.dto.CouponDTO;
import com.ecommerce.domain.model.*;
import com.ecommerce.domain.repository.CouponRepository;
import com.ecommerce.domain.repository.CategoryRepository;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CouponService {
    
    @Inject
    CouponRepository couponRepository;
    
    @Inject
    CategoryRepository categoryRepository;
    
    @Inject
    ProductRepository productRepository;
    
    public Uni<List<Coupon>> getActiveCoupons() {
        return couponRepository.findActive();
    }
    
    @Transactional
    public Uni<Coupon> createCoupon(CouponDTO couponDTO) {
        return validateCouponCode(couponDTO.getCode())
            .chain(() -> {
                Coupon coupon = new Coupon();
                updateCouponFromDto(coupon, couponDTO);
                return loadRelatedEntities(coupon, couponDTO)
                    .chain(c -> couponRepository.persist(c));
            });
    }
    
    @Transactional
    public Uni<Coupon> updateCoupon(Long id, CouponDTO couponDTO) {
        return couponRepository.findById(id)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Coupon not found"))
            .chain(coupon -> {
                if (!coupon.getCode().equals(couponDTO.getCode())) {
                    return validateCouponCode(couponDTO.getCode())
                        .chain(() -> {
                            updateCouponFromDto(coupon, couponDTO);
                            return loadRelatedEntities(coupon, couponDTO)
                                .chain(c -> couponRepository.persist(c));
                        });
                }
                updateCouponFromDto(coupon, couponDTO);
                return loadRelatedEntities(coupon, couponDTO)
                    .chain(c -> couponRepository.persist(c));
            });
    }
    
    public Uni<BigDecimal> calculateDiscount(String code, BigDecimal cartTotal, List<CartItem> items) {
        return couponRepository.findByCode(code)
            .chain(coupon -> {
                if (coupon == null || !isValidCoupon(coupon, cartTotal)) {
                    return Uni.createFrom().item(BigDecimal.ZERO);
                }
                
                return calculateApplicableDiscount(coupon, cartTotal, items);
            });
    }
    
    private Uni<Void> validateCouponCode(String code) {
        return couponRepository.findByCode(code)
            .chain(existing -> {
                if (existing != null) {
                    return Uni.createFrom().failure(
                        new IllegalStateException("Coupon code already exists"));
                }
                return Uni.createFrom().voidItem();
            });
    }
    
    private void updateCouponFromDto(Coupon coupon, CouponDTO dto) {
        coupon.setCode(dto.getCode());
        coupon.setDescription(dto.getDescription());
        coupon.setDiscountType(dto.getDiscountType());
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setMinimumPurchase(dto.getMinimumPurchase());
        coupon.setMaximumDiscount(dto.getMaximumDiscount());
        coupon.setStartDate(dto.getStartDate());
        coupon.setEndDate(dto.getEndDate());
        coupon.setUsageLimit(dto.getUsageLimit());
        coupon.setActive(dto.getActive() != null ? dto.getActive() : true);
    }
    
    private Uni<Coupon> loadRelatedEntities(Coupon coupon, CouponDTO dto) {
        return Uni.combine().all().unis(
            loadCategories(dto.getCategoryIds()),
            loadProducts(dto.getProductIds())
        ).asTuple()
        .map(tuple -> {
            coupon.setApplicableCategories(tuple.getItem1());
            coupon.setApplicableProducts(tuple.getItem2());
            return coupon;
        });
    }
    
    private Uni<Set<Category>> loadCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Uni.createFrom().item(new HashSet<>());
        }
        return categoryRepository.list("id in ?1", categoryIds)
            .map(list -> new HashSet<>(list));
    }
    
    private Uni<Set<Product>> loadProducts(Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Uni.createFrom().item(new HashSet<>());
        }
        return productRepository.list("id in ?1", productIds)
            .map(list -> new HashSet<>(list));
    }
    
    private boolean isValidCoupon(Coupon coupon, BigDecimal cartTotal) {
        LocalDateTime now = LocalDateTime.now();
        return coupon.getActive() &&
               now.isAfter(coupon.getStartDate()) &&
               now.isBefore(coupon.getEndDate()) &&
               coupon.getUsageCount() < coupon.getUsageLimit() &&
               (coupon.getMinimumPurchase() == null || 
                cartTotal.compareTo(coupon.getMinimumPurchase()) >= 0);
    }
    
    private Uni<BigDecimal> calculateApplicableDiscount(
            Coupon coupon, BigDecimal cartTotal, List<CartItem> items) {
        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = cartTotal.multiply(
                coupon.getDiscountValue().divide(new BigDecimal("100")));
        } else {
            discount = coupon.getDiscountValue();
        }
        
        if (coupon.getMaximumDiscount() != null && 
            discount.compareTo(coupon.getMaximumDiscount()) > 0) {
            discount = coupon.getMaximumDiscount();
        }
        
        return Uni.createFrom().item(discount);
    }
}