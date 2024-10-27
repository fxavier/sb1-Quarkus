package com.ecommerce.service;

import com.ecommerce.domain.model.User;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Collections;

@ApplicationScoped
public class GoogleAuthService {
    
    @Inject
    JwtService jwtService;
    
    @ConfigProperty(name = "google.client.id")
    String clientId;
    
    private final GoogleIdTokenVerifier verifier;
    
    public GoogleAuthService() {
        verifier = new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(), new GsonFactory())
            .setAudience(Collections.singletonList(clientId))
            .build();
    }
    
    public Uni<String> authenticateWithGoogle(String idTokenString) {
        return Uni.createFrom().item(() -> {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new SecurityException("Invalid ID token");
            }
            
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String googleId = payload.getSubject();
            
            return User.<User>find("email", email)
                .firstResult()
                .chain(existingUser -> {
                    if (existingUser != null) {
                        existingUser.setGoogleId(googleId);
                        return existingUser.persist();
                    }
                    
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setGoogleId(googleId);
                    newUser.setEmailVerified(true);
                    
                    return newUser.persist();
                })
                .map(user -> jwtService.generateToken(user));
        }).flatMap(uni -> uni);
    }
}