package com.ecommerce.service;

import com.ecommerce.domain.dto.PageDTO;
import com.ecommerce.domain.dto.ProductDTO;
import com.ecommerce.domain.dto.ProductFilterDTO;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ProductService {
    
    @Inject
    ProductRepository productRepository;
    
    public Uni<PageDTO<Product>> getFilteredProducts(ProductFilterDTO filter) {
        // Set default values if not provided
        if (filter.getPage() == null) filter.setPage(0);
        if (filter.getSize() == null) filter.setSize(20);
        if (filter.getSortBy() == null) filter.setSortBy("id");
        if (filter.getSortDirection() == null) filter.setSortDirection("asc");
        
        return Uni.combine().all().unis(
            productRepository.findFiltered(filter),
            productRepository.countFiltered(filter)
        ).asTuple()
        .map(tuple -> new PageDTO<>(
            tuple.getItem1(),
            tuple.getItem2(),
            filter.getPage(),
            filter.getSize()
        ));
    }
    
    public Uni<Product> getProductById(Long id) {
        return productRepository.findById(id)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Product not found with id: " + id));
    }
    
    @Transactional
    public Uni<Product> createProduct(ProductDTO productDTO) {
        Product product = mapDtoToProduct(productDTO);
        return productRepository.persist(product);
    }
    
    @Transactional
    public Uni<Product> updateProduct(Long id, ProductDTO productDTO) {
        return productRepository.findById(id)
            .onItem().ifNull().failWith(() -> 
                new ResourceNotFoundException("Product not found with id: " + id))
            .chain(product -> {
                updateProductFromDto(product, productDTO);
                return productRepository.persist(product);
            });
    }
    
    @Transactional
    public Uni<Boolean> deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }
    
    public Multi<Product> searchProducts(String query) {
        return productRepository.searchProducts(query);
    }
    
    public Uni<List<Product>> findByPriceRange(double minPrice, double maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    private Product mapDtoToProduct(ProductDTO dto) {
        Product product = new Product();
        updateProductFromDto(product, dto);
        return product;
    }
    
    private void updateProductFromDto(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setActive(dto.getActive() != null ? dto.getActive() : true);
    }
}