package com.ecommerce.resource;

import com.ecommerce.service.CartService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {
    
    @Inject
    CartService cartService;
    
    @GET
    public Uni<Response> getCart(@HeaderParam("X-User-Id") String userId) {
        return cartService.getCart(userId)
            .onItem().transform(cart -> Response.ok(cart).build());
    }
    
    @POST
    @Path("/items")
    public Uni<Response> addToCart(
            @HeaderParam("X-User-Id") String userId,
            @QueryParam("productId") Long productId,
            @QueryParam("quantity") Integer quantity) {
        return cartService.addToCart(userId, productId, quantity)
            .onItem().transform(cart -> Response.ok(cart).build());
    }
}