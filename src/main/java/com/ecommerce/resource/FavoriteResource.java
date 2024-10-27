package com.ecommerce.resource;

import com.ecommerce.service.FavoriteService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/favorites")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FavoriteResource {
    
    @Inject
    FavoriteService favoriteService;
    
    @GET
    public Uni<Response> getUserFavorites(@HeaderParam("X-User-Id") String userId) {
        return favoriteService.getUserFavorites(userId)
            .onItem().transform(favorites -> Response.ok(favorites).build());
    }
    
    @GET
    @Path("/check/{productId}")
    public Uni<Response> isFavorite(
            @HeaderParam("X-User-Id") String userId,
            @PathParam("productId") Long productId) {
        return favoriteService.isFavorite(userId, productId)
            .onItem().transform(isFavorite -> Response.ok(isFavorite).build());
    }
    
    @POST
    @Path("/{productId}")
    public Uni<Response> addToFavorites(
            @HeaderParam("X-User-Id") String userId,
            @PathParam("productId") Long productId) {
        return favoriteService.addToFavorites(userId, productId)
            .onItem().transform(favorite -> 
                Response.status(Response.Status.CREATED).entity(favorite).build());
    }
    
    @DELETE
    @Path("/{productId}")
    public Uni<Response> removeFromFavorites(
            @HeaderParam("X-User-Id") String userId,
            @PathParam("productId") Long productId) {
        return favoriteService.removeFromFavorites(userId, productId)
            .onItem().transform(removed -> 
                removed ? Response.noContent().build() 
                       : Response.status(Response.Status.NOT_FOUND).build());
    }
}