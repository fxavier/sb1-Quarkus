package com.ecommerce.domain.dto;

import com.ecommerce.domain.model.DiscountType;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class CouponDTO {
    private Long id;
    
    @NotBlank(message = "Coupon code is required")
    @Pattern(regexp = "^[A-Z0-9_-]{4,16}$", message = "Invalid coupon code format")
    private String code;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Discount type is required")
    private DiscountType discountType;
    
    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private BigDecimal discountValue;
    
    @PositiveOrZero(message = "Minimum purchase must be positive or zero")
    private BigDecimal minimumPurchase;
    
    @PositiveOrZero(message = "Maximum discount must be positive or zero")
    private BigDecimal maximumDiscount;
    
    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;
    
    @NotNull(message = "Usage limit is required")
    @Positive(message = "Usage limit must be positive")
    private Integer usageLimit;
    
    private Set<Long> categoryIds;
    private Set<Long> productIds;
    private Boolean active;
}