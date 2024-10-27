package com.ecommerce.resource;

import com.ecommerce.domain.dto.CouponDTO;
import com.ecommerce.service.CouponService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/coupons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CouponResource {
    
    @Inject
    CouponService couponService;
    
    @GET
    public Uni<Response> getActiveCoupons() {
        return couponService.getActiveCoupons()
            .onItem().transform(coupons -> Response.ok(coupons).build());
    }
    
    @POST
    public Uni<Response> createCoupon(@Valid CouponDTO couponDTO) {
        return couponService.createCoupon(couponDTO)
            .onItem().transform(coupon -> 
                Response.status(Response.Status.CREATED).entity(coupon).build());
    }
    
    @PUT
    @Path("/{id}")
    public Uni<Response> updateCoupon(
            @PathParam("id") Long id,
            @Valid CouponDTO couponDTO) {
        return couponService.updateCoupon(id, couponDTO)
            .onItem().transform(coupon -> Response.ok(coupon).build());
    }
    
    @GET
    @Path("/validate")
    public Uni<Response> validateCoupon(
            @QueryParam("code") String code,
            @QueryParam("cartTotal") BigDecimal cartTotal) {
        return couponService.calculateDiscount(code, cartTotal, null)
            .onItem().transform(discount -> Response.ok(
                new DiscountResponse(discount)).build());
    }
    
    private static class DiscountResponse {
        private final BigDecimal discount;
        
        public DiscountResponse(BigDecimal discount) {
            this.discount = discount;
        }
        
        public BigDecimal getDiscount() {
            return discount;
        }
    }
}