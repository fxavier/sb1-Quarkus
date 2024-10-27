package com.ecommerce.service;

import com.ecommerce.domain.model.*;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class InventoryAnalyticsService {
    
    @Inject
    ProductRepository productRepository;
    
    public Uni<Map<String, Object>> getProductAnalytics(Long productId, LocalDate startDate, LocalDate endDate) {
        return InventoryAnalytics.find(
                "product.id = ?1 and date between ?2 and ?3",
                productId, startDate, endDate)
            .list()
            .map(analytics -> {
                Map<String, Object> result = new HashMap<>();
                
                // Calculate averages
                double avgTurnover = analytics.stream()
                    .mapToDouble(InventoryAnalytics::getTurnoverRate)
                    .average()
                    .orElse(0.0);
                
                int totalSales = analytics.stream()
                    .mapToInt(InventoryAnalytics::getSalesCount)
                    .sum();
                
                int totalRestocks = analytics.stream()
                    .mapToInt(InventoryAnalytics::getRestockCount)
                    .sum();
                
                int totalOutOfStock = analytics.stream()
                    .mapToInt(InventoryAnalytics::getDaysOutOfStock)
                    .sum();
                
                // Daily trends
                Map<LocalDate, Integer> salesTrend = analytics.stream()
                    .collect(Collectors.toMap(
                        InventoryAnalytics::getDate,
                        InventoryAnalytics::getSalesCount
                    ));
                
                result.put("averageTurnover", avgTurnover);
                result.put("totalSales", totalSales);
                result.put("totalRestocks", totalRestocks);
                result.put("daysOutOfStock", totalOutOfStock);
                result.put("salesTrend", salesTrend);
                
                return result;
            });
    }
    
    public Uni<List<Map<String, Object>>> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit) {
        return InventoryAnalytics.find(
                "date between ?1 and ?2 group by product.id " +
                "order by sum(salesCount) desc",
                startDate, endDate)
            .page(0, limit)
            .list()
            .map(analytics -> analytics.stream()
                .map(a -> {
                    Map<String, Object> product = new HashMap<>();
                    product.put("productId", a.getProduct().getId());
                    product.put("productName", a.getProduct().getName());
                    product.put("totalSales", a.getSalesCount());
                    product.put("turnoverRate", a.getTurnoverRate());
                    return product;
                })
                .collect(Collectors.toList()));
    }
    
    public Uni<List<Map<String, Object>>> getStockoutRisk() {
        return Product.find("stockQuantity <= lowStockThreshold and active = true")
            .list()
            .map(products -> products.stream()
                .map(p -> {
                    Map<String, Object> risk = new HashMap<>();
                    risk.put("productId", p.getId());
                    risk.put("productName", p.getName());
                    risk.put("currentStock", p.getStockQuantity());
                    risk.put("threshold", p.getLowStockThreshold());
                    risk.put("riskLevel", calculateRiskLevel(p));
                    return risk;
                })
                .collect(Collectors.toList()));
    }
    
    private String calculateRiskLevel(Product product) {
        double stockRatio = (double) product.getStockQuantity() / product.getLowStockThreshold();
        if (stockRatio <= 0.25) return "CRITICAL";
        if (stockRatio <= 0.5) return "HIGH";
        if (stockRatio <= 0.75) return "MEDIUM";
        return "LOW";
    }
}