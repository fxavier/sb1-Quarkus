package com.ecommerce.domain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Set;

@Data
public class ProductFilterDTO {
    private String searchTerm;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private Set<String> tags;
    private Boolean inStock;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}