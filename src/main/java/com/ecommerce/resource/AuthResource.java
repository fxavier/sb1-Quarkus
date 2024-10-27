package com.ecommerce.resource;

import com.ecommerce.domain.dto.LoginDTO;
import com.ecommerce.domain.dto.UserRegistrationDTO;
import com.ecommerce.service.GoogleAuthService;
import com.ecommerce.service.OtpService;
import com.ecommerce.service.UserService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    
    @Inject
    UserService userService;
    
    @Inject
    GoogleAuthService googleAuthService;
    
    @Inject
    OtpService otpService;
    
    @POST
    @Path("/register")
    public Uni<Response> register(@Valid UserRegistrationDTO registrationDTO) {
        return userService.register(registrationDTO)
            .onItem().transform(user -> 
                Response.status(Response.Status.CREATED).entity(user).build());
    }
    
    @POST
    @Path("/login")
    public Uni<Response> login(@Valid LoginDTO loginDTO) {
        return userService.login(loginDTO)
            .onItem().transform(token -> 
                Response.ok().entity(new TokenResponse(token)).build());
    }
    
    @POST
    @Path("/google")
    public Uni<Response> googleLogin(@QueryParam("idToken") String idToken) {
        return googleAuthService.authenticateWithGoogle(idToken)
            .onItem().transform(token -> 
                Response.ok().entity(new TokenResponse(token)).build());
    }
    
    @POST
    @Path("/otp/generate")
    public Uni<Response> generateOtp(@QueryParam("phoneNumber") String phoneNumber) {
        return otpService.generateAndSendOtp(phoneNumber)
            .onItem().transform(otp -> Response.ok().build());
    }
    
    @POST
    @Path("/otp/verify")
    public Uni<Response> verifyOtp(
            @QueryParam("phoneNumber") String phoneNumber,
            @QueryParam("otp") String otp) {
        return otpService.verifyOtp(phoneNumber, otp)
            .onItem().transform(verified -> 
                verified ? Response.ok().build() 
                        : Response.status(Response.Status.UNAUTHORIZED).build());
    }
    
    @GET
    @Path("/verify-email")
    public Uni<Response> verifyEmail(@QueryParam("token") String token) {
        return userService.verifyEmail(token)
            .onItem().transform(user -> Response.ok().build());
    }
    
    @POST
    @Path("/forgot-password")
    public Uni<Response> forgotPassword(@QueryParam("email") String email) {
        return userService.initiatePasswordReset(email)
            .onItem().transform(user -> Response.ok().build());
    }
    
    @POST
    @Path("/reset-password")
    public Uni<Response> resetPassword(
            @QueryParam("token") String token,
            @QueryParam("password") String newPassword) {
        return userService.resetPassword(token, newPassword)
            .onItem().transform(user -> Response.ok().build());
    }
    
    private static class TokenResponse {
        private final String token;
        
        public TokenResponse(String token) {
            this.token = token;
        }
        
        public String getToken() {
            return token;
        }
    }
}