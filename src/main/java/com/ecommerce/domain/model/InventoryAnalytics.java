package com.ecommerce.domain.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "inventory_analytics")
public class InventoryAnalytics extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(nullable = false)
    private LocalDate date;
    
    private Integer startingStock;
    private Integer endingStock;
    private Integer salesCount;
    private Integer restockCount;
    private Integer returnsCount;
    private Double turnoverRate;
    private Integer daysOutOfStock;
    private Integer lowStockIncidents;
}