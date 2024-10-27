package com.ecommerce.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@ApplicationScoped
public class SmsService {
    
    @ConfigProperty(name = "twilio.account-sid")
    String accountSid;
    
    @ConfigProperty(name = "twilio.auth-token")
    String authToken;
    
    @ConfigProperty(name = "twilio.phone-number")
    String twilioPhoneNumber;
    
    public void sendOtp(String phoneNumber, String otp) {
        try {
            Twilio.init(accountSid, authToken);
            
            Message message = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(twilioPhoneNumber),
                "Your verification code is: " + otp
            ).create();
            
            if (!"sent".equals(message.getStatus().toString()) && 
                !"queued".equals(message.getStatus().toString())) {
                throw new RuntimeException("Failed to send SMS: " + message.getStatus());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error sending SMS", e);
        }
    }
}