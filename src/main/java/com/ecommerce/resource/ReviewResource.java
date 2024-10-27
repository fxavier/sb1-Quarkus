package com.ecommerce.resource;

import com.ecommerce.domain.dto.ReviewDTO;
import com.ecommerce.service.ReviewService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReviewResource {
    
    @Inject
    ReviewService reviewService;
    
    @GET
    @Path("/product/{productId}")
    public Uni<Response> getProductReviews(@PathParam("productId") Long productId) {
        return reviewService.getProductReviews(productId)
            .onItem().transform(reviews -> Response.ok(reviews).build());
    }
    
    @GET
    @Path("/user")
    public Uni<Response> getUserReviews(@HeaderParam("X-User-Id") String userId) {
        return reviewService.getUserReviews(userId)
            .onItem().transform(reviews -> Response.ok(reviews).build());
    }
    
    @POST
    @Path("/product/{productId}")
    public Uni<Response> createReview(
            @PathParam("productId") Long productId,
            @HeaderParam("X-User-Id") String userId,
            @Valid ReviewDTO reviewDTO) {
        return reviewService.createReview(productId, userId, reviewDTO)
            .onItem().transform(review -> 
                Response.status(Response.Status.CREATED).entity(review).build());
    }
    
    @PUT
    @Path("/{reviewId}")
    public Uni<Response> updateReview(
            @PathParam("reviewId") Long reviewId,
            @HeaderParam("X-User-Id") String userId,
            @Valid ReviewDTO reviewDTO) {
        return reviewService.updateReview(reviewId, userId, reviewDTO)
            .onItem().transform(review -> Response.ok(review).build());
    }
    
    @DELETE
    @Path("/{reviewId}")
    public Uni<Response> deleteReview(
            @PathParam("reviewId") Long reviewId,
            @HeaderParam("X-User-Id") String userId) {
        return reviewService.deleteReview(reviewId, userId)
            .onItem().transform(v -> Response.noContent().build());
    }
    
    @POST
    @Path("/{reviewId}/helpful")
    public Uni<Response> voteHelpful(@PathParam("reviewId") Long reviewId) {
        return reviewService.voteHelpful(reviewId)
            .onItem().transform(review -> Response.ok(review).build());
    }
}