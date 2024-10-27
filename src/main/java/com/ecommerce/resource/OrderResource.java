package com.ecommerce.resource;

import com.ecommerce.domain.model.OrderStatus;
import com.ecommerce.domain.model.ShippingAddress;
import com.ecommerce.service.OrderService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {
    
    @Inject
    OrderService orderService;
    
    @GET
    public Uni<Response> getUserOrders(@HeaderParam("X-User-Id") String userId) {
        return orderService.getUserOrders(userId)
            .onItem().transform(orders -> Response.ok(orders).build());
    }
    
    @GET
    @Path("/{orderId}")
    public Uni<Response> getOrder(@PathParam("orderId") Long orderId) {
        return orderService.getOrder(orderId)
            .onItem().transform(order -> Response.ok(order).build());
    }
    
    @POST
    public Uni<Response> createOrder(
            @HeaderParam("X-User-Id") String userId,
            ShippingAddress shippingAddress) {
        return orderService.createOrder(userId, shippingAddress)
            .onItem().transform(order ->
                Response.status(Response.Status.CREATED).entity(order).build());
    }
    
    @PUT
    @Path("/{orderId}/status")
    public Uni<Response> updateOrderStatus(
            @PathParam("orderId") Long orderId,
            OrderStatus status) {
        return orderService.updateOrderStatus(orderId, status)
            .onItem().transform(order -> Response.ok(order).build());
    }
}