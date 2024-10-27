package com.ecommerce.resource;

import com.ecommerce.domain.model.ShippingAddress;
import com.ecommerce.service.CheckoutService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/checkout")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CheckoutResource {
    
    @Inject
    CheckoutService checkoutService;
    
    @POST
    @Path("/initiate")
    public Uni<Response> initiateCheckout(
            @HeaderParam("X-User-Id") String userId,
            ShippingAddress shippingAddress) {
        return checkoutService.initiateCheckout(userId, shippingAddress)
            .onItem().transform(order ->
                Response.status(Response.Status.CREATED).entity(order).build());
    }
    
    @POST
    @Path("/{orderId}/complete")
    public Uni<Response> completeCheckout(
            @PathParam("orderId") Long orderId,
            @QueryParam("paymentIntentId") String paymentIntentId) {
        return checkoutService.completeCheckout(orderId, paymentIntentId)
            .onItem().transform(order -> Response.ok(order).build());
    }
}