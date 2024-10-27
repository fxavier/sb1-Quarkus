package com.ecommerce.resource;

import com.ecommerce.domain.dto.PageDTO;
import com.ecommerce.domain.dto.ProductDTO;
import com.ecommerce.domain.dto.ProductFilterDTO;
import com.ecommerce.domain.model.Product;
import com.ecommerce.service.ProductService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {
    
    @Inject
    ProductService productService;
    
    @GET
    public Uni<Response> getProducts(
            @QueryParam("searchTerm") String searchTerm,
            @QueryParam("categoryId") Long categoryId,
            @QueryParam("minPrice") Double minPrice,
            @QueryParam("maxPrice") Double maxPrice,
            @QueryParam("minRating") Double minRating,
            @QueryParam("inStock") Boolean inStock,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortDirection") String sortDirection,
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size) {
        
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setSearchTerm(searchTerm);
        filter.setCategoryId(categoryId);
        filter.setMinPrice(minPrice != null ? BigDecimal.valueOf(minPrice) : null);
        filter.setMaxPrice(maxPrice != null ? BigDecimal.valueOf(maxPrice) : null);
        filter.setMinRating(minRating);
        filter.setInStock(inStock);
        filter.setSortBy(sortBy);
        filter.setSortDirection(sortDirection);
        filter.setPage(page);
        filter.setSize(size);
        
        return productService.getFilteredProducts(filter)
            .onItem().transform(products -> Response.ok(products).build());
    }
    
    @GET
    @Path("/{id}")
    public Uni<Response> getProduct(@PathParam("id") Long id) {
        return productService.getProductById(id)
            .onItem().transform(product -> Response.ok(product).build());
    }
    
    @POST
    public Uni<Response> createProduct(@Valid ProductDTO productDTO) {
        return productService.createProduct(productDTO)
            .onItem().transform(product -> 
                Response.status(Response.Status.CREATED).entity(product).build());
    }
    
    @PUT
    @Path("/{id}")
    public Uni<Response> updateProduct(@PathParam("id") Long id, @Valid ProductDTO productDTO) {
        return productService.updateProduct(id, productDTO)
            .onItem().transform(product -> Response.ok(product).build());
    }
    
    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteProduct(@PathParam("id") Long id) {
        return productService.deleteProduct(id)
            .onItem().transform(deleted -> 
                deleted ? Response.noContent().build()
                       : Response.status(Response.Status.NOT_FOUND).build());
    }
}