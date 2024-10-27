package com.ecommerce.domain.repository;

import com.ecommerce.domain.dto.ProductFilterDTO;
import com.ecommerce.domain.model.Product;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {
    
    public Uni<List<Product>> findByCategory(Long categoryId) {
        return find("category.id", categoryId).list();
    }
    
    public Multi<Product> searchProducts(String query) {
        return find("name like ?1 or description like ?1", "%" + query + "%").stream();
    }
    
    public Uni<List<Product>> findActiveProducts() {
        return find("active", true).list();
    }
    
    public Uni<List<Product>> findByPriceRange(double minPrice, double maxPrice) {
        return find("price >= ?1 and price <= ?2", minPrice, maxPrice).list();
    }
    
    public Uni<List<Product>> findFiltered(ProductFilterDTO filter) {
        StringBuilder query = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();
        int paramIndex = 1;
        
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
            query.append(" and (lower(name) like ?").append(paramIndex)
                .append(" or lower(description) like ?").append(paramIndex).append(")");
            params.add("%" + filter.getSearchTerm().toLowerCase() + "%");
            paramIndex++;
        }
        
        if (filter.getCategoryId() != null) {
            query.append(" and category.id = ?").append(paramIndex);
            params.add(filter.getCategoryId());
            paramIndex++;
        }
        
        if (filter.getMinPrice() != null) {
            query.append(" and price >= ?").append(paramIndex);
            params.add(filter.getMinPrice());
            paramIndex++;
        }
        
        if (filter.getMaxPrice() != null) {
            query.append(" and price <= ?").append(paramIndex);
            params.add(filter.getMaxPrice());
            paramIndex++;
        }
        
        if (filter.getMinRating() != null) {
            query.append(" and averageRating >= ?").append(paramIndex);
            params.add(filter.getMinRating());
            paramIndex++;
        }
        
        if (filter.getInStock() != null && filter.getInStock()) {
            query.append(" and stockQuantity > 0");
        }
        
        Sort sort = createSort(filter.getSortBy(), filter.getSortDirection());
        Page page = Page.of(filter.getPage(), filter.getSize());
        
        return find(query.toString(), sort, params.toArray()).page(page).list();
    }
    
    public Uni<Long> countFiltered(ProductFilterDTO filter) {
        StringBuilder query = new StringBuilder("1=1");
        List<Object> params = new ArrayList<>();
        int paramIndex = 1;
        
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
            query.append(" and (lower(name) like ?").append(paramIndex)
                .append(" or lower(description) like ?").append(paramIndex).append(")");
            params.add("%" + filter.getSearchTerm().toLowerCase() + "%");
            paramIndex++;
        }
        
        if (filter.getCategoryId() != null) {
            query.append(" and category.id = ?").append(paramIndex);
            params.add(filter.getCategoryId());
            paramIndex++;
        }
        
        if (filter.getMinPrice() != null) {
            query.append(" and price >= ?").append(paramIndex);
            params.add(filter.getMinPrice());
            paramIndex++;
        }
        
        if (filter.getMaxPrice() != null) {
            query.append(" and price <= ?").append(paramIndex);
            params.add(filter.getMaxPrice());
            paramIndex++;
        }
        
        if (filter.getMinRating() != null) {
            query.append(" and averageRating >= ?").append(paramIndex);
            params.add(filter.getMinRating());
            paramIndex++;
        }
        
        if (filter.getInStock() != null && filter.getInStock()) {
            query.append(" and stockQuantity > 0");
        }
        
        return count(query.toString(), params.toArray());
    }
    
    private Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null) {
            return Sort.by("id");
        }
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) 
            ? Sort.Direction.Descending 
            : Sort.Direction.Ascending;
        
        switch (sortBy.toLowerCase()) {
            case "price":
                return Sort.by("price", direction);
            case "name":
                return Sort.by("name", direction);
            case "rating":
                return Sort.by("averageRating", direction);
            case "created":
                return Sort.by("createdAt", direction);
            default:
                return Sort.by("id", direction);
        }
    }
}