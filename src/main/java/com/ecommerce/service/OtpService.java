package com.ecommerce.service;

import com.ecommerce.domain.model.User;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@ApplicationScoped
public class OtpService {
    
    @Inject
    SmsService smsService;
    
    private final SecureRandom random = new SecureRandom();
    
    public Uni<String> generateAndSendOtp(String phoneNumber) {
        String otp = generateOtp();
        
        return User.<User>find("phoneNumber", phoneNumber)
            .firstResult()
            .chain(user -> {
                if (user == null) {
                    return Uni.createFrom().failure(
                        new IllegalStateException("Phone number not registered"));
                }
                
                user.setVerificationToken(otp);
                user.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(5));
                
                return user.persist()
                    .chain(savedUser -> {
                        smsService.sendOtp(phoneNumber, otp);
                        return Uni.createFrom().item(otp);
                    });
            });
    }
    
    public Uni<Boolean> verifyOtp(String phoneNumber, String otp) {
        return User.<User>find("phoneNumber = ?1 and verificationToken = ?2",
                phoneNumber, otp)
            .firstResult()
            .chain(user -> {
                if (user == null) {
                    return Uni.createFrom().item(false);
                }
                
                if (LocalDateTime.now().isAfter(user.getVerificationTokenExpiry())) {
                    return Uni.createFrom().item(false);
                }
                
                user.setPhoneVerified(true);
                user.setVerificationToken(null);
                user.setVerificationTokenExpiry(null);
                
                return user.persist()
                    .map(savedUser -> true);
            });
    }
    
    private String generateOtp() {
        return String.format("%06d", random.nextInt(1000000));
    }
}