package com.ecommerce.service;

import com.ecommerce.domain.dto.LoginDTO;
import com.ecommerce.domain.dto.UserRegistrationDTO;
import com.ecommerce.domain.model.User;
import com.ecommerce.domain.model.UserProfile;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class UserService {
    
    @Inject
    EmailService emailService;
    
    @Inject
    JwtService jwtService;
    
    public Uni<User> register(UserRegistrationDTO registrationDTO) {
        return User.<User>find("email", registrationDTO.getEmail())
            .firstResult()
            .chain(existingUser -> {
                if (existingUser != null) {
                    return Uni.createFrom().failure(
                        new IllegalStateException("Email already registered"));
                }
                
                User user = new User();
                user.setEmail(registrationDTO.getEmail());
                user.setPassword(BCrypt.hashpw(registrationDTO.getPassword(), BCrypt.gensalt()));
                user.setPhoneNumber(registrationDTO.getPhoneNumber());
                user.setVerificationToken(UUID.randomUUID().toString());
                user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
                
                return user.<User>persist()
                    .chain(savedUser -> {
                        UserProfile profile = new UserProfile();
                        profile.setUser(savedUser);
                        profile.setFirstName(registrationDTO.getFirstName());
                        profile.setLastName(registrationDTO.getLastName());
                        return profile.<UserProfile>persist()
                            .map(p -> savedUser);
                    })
                    .chain(savedUser -> {
                        emailService.sendVerificationEmail(savedUser);
                        return Uni.createFrom().item(savedUser);
                    });
            });
    }
    
    public Uni<String> login(LoginDTO loginDTO) {
        return User.<User>find("email", loginDTO.getEmail())
            .firstResult()
            .chain(user -> {
                if (user == null || !BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
                    return Uni.createFrom().failure(
                        new SecurityException("Invalid credentials"));
                }
                
                if (!user.isEmailVerified()) {
                    return Uni.createFrom().failure(
                        new SecurityException("Email not verified"));
                }
                
                return Uni.createFrom().item(jwtService.generateToken(user));
            });
    }
    
    public Uni<User> verifyEmail(String token) {
        return User.<User>find("verificationToken", token)
            .firstResult()
            .chain(user -> {
                if (user == null) {
                    return Uni.createFrom().failure(
                        new ResourceNotFoundException("Invalid verification token"));
                }
                
                if (LocalDateTime.now().isAfter(user.getVerificationTokenExpiry())) {
                    return Uni.createFrom().failure(
                        new IllegalStateException("Verification token expired"));
                }
                
                user.setEmailVerified(true);
                user.setVerificationToken(null);
                user.setVerificationTokenExpiry(null);
                
                return user.persist();
            });
    }
    
    public Uni<User> initiatePasswordReset(String email) {
        return User.<User>find("email", email)
            .firstResult()
            .chain(user -> {
                if (user == null) {
                    return Uni.createFrom().failure(
                        new ResourceNotFoundException("User not found"));
                }
                
                user.setResetToken(UUID.randomUUID().toString());
                user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
                
                return user.persist()
                    .chain(savedUser -> {
                        emailService.sendPasswordResetEmail(savedUser);
                        return Uni.createFrom().item(savedUser);
                    });
            });
    }
    
    public Uni<User> resetPassword(String token, String newPassword) {
        return User.<User>find("resetToken", token)
            .firstResult()
            .chain(user -> {
                if (user == null) {
                    return Uni.createFrom().failure(
                        new ResourceNotFoundException("Invalid reset token"));
                }
                
                if (LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
                    return Uni.createFrom().failure(
                        new IllegalStateException("Reset token expired"));
                }
                
                user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
                user.setResetToken(null);
                user.setResetTokenExpiry(null);
                
                return user.persist();
            });
    }
}