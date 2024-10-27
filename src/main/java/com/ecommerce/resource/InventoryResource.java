package com.ecommerce.resource;

import com.ecommerce.domain.model.TransactionType;
import com.ecommerce.service.InventoryService;
import com.ecommerce.service.InventoryAnalyticsService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;

@Path("/api/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {
    
    @Inject
    InventoryService inventoryService;
    
    @Inject
    InventoryAnalyticsService analyticsService;
    
    @POST
    @Path("/transaction")
    public Uni<Response> recordTransaction(
            @QueryParam("productId") Long productId,
            @QueryParam("quantity") Integer quantity,
            @QueryParam("type") TransactionType type,
            @QueryParam("reference") String reference) {
        return inventoryService.recordTransaction(productId, quantity, type, reference)
            .onItem().transform(transaction -> 
                Response.status(Response.Status.CREATED).entity(transaction).build());
    }
    
    @GET
    @Path("/transactions/{productId}")
    public Uni<Response> getProductTransactions(@PathParam("productId") Long productId) {
        return inventoryService.getProductTransactions(productId)
            .onItem().transform(transactions -> Response.ok(transactions).build());
    }
    
    @GET
    @Path("/low-stock")
    public Uni<Response> getLowStockProducts() {
        return inventoryService.getLowStockProducts()
            .onItem().transform(products -> Response.ok(products).build());
    }
    
    @PUT
    @Path("/threshold/{productId}")
    public Uni<Response> updateStockThreshold(
            @PathParam("productId") Long productId,
            @QueryParam("threshold") Integer threshold) {
        return inventoryService.updateStockThreshold(productId, threshold)
            .onItem().transform(product -> Response.ok(product).build());
    }
    
    @GET
    @Path("/analytics/product/{productId}")
    public Uni<Response> getProductAnalytics(
            @PathParam("productId") Long productId,
            @QueryParam("startDate") LocalDate startDate,
            @QueryParam("endDate") LocalDate endDate) {
        return analyticsService.getProductAnalytics(productId, startDate, endDate)
            .onItem().transform(analytics -> Response.ok(analytics).build());
    }
    
    @GET
    @Path("/analytics/top-selling")
    public Uni<Response> getTopSellingProducts(
            @QueryParam("startDate") LocalDate startDate,
            @QueryParam("endDate") LocalDate endDate,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        return analyticsService.getTopSellingProducts(startDate, endDate, limit)
            .onItem().transform(products -> Response.ok(products).build());
    }
    
    @GET
    @Path("/analytics/stockout-risk")
    public Uni<Response> getStockoutRisk() {
        return analyticsService.getStockoutRisk()
            .onItem().transform(risks -> Response.ok(risks).build());
    }
}