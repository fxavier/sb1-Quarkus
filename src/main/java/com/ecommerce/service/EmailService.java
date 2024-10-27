package com.ecommerce.service;

import com.ecommerce.domain.model.Order;
import com.ecommerce.domain.model.OrderItem;
import com.ecommerce.domain.model.User;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class EmailService {
    
    @ConfigProperty(name = "sendgrid.api-key")
    String sendGridApiKey;
    
    @ConfigProperty(name = "app.base-url")
    String baseUrl;
    
    public void sendVerificationEmail(User user) {
        try {
            Email from = new Email("noreply@ecommerce.com", "E-commerce");
            Email to = new Email(user.getEmail());
            String subject = "Verify Your Email Address";
            
            String htmlContent = buildVerificationEmail(user);
            
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);
            
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("Failed to send verification email: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error sending verification email", e);
        }
    }
    
    private String buildVerificationEmail(User user) {
        StringBuilder html = new StringBuilder();
        
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>");
        html.append("<div style='background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); padding: 30px;'>");
        
        // Header
        html.append("<div style='text-align: center; margin-bottom: 30px;'>");
        html.append("<h1 style='color: #333333; margin-bottom: 10px;'>Welcome to E-commerce!</h1>");
        html.append("<p style='color: #666666; font-size: 16px;'>Please verify your email address to get started</p>");
        html.append("</div>");
        
        // Verification Button
        html.append("<div style='text-align: center; margin: 30px 0;'>");
        html.append("<a href='").append(baseUrl).append("/api/auth/verify-email?token=")
            .append(user.getVerificationToken())
            .append("' style='background-color: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; ")
            .append("border-radius: 4px; font-size: 16px; display: inline-block;'>Verify Email Address</a>");
        html.append("</div>");
        
        // Alternative Link
        html.append("<div style='text-align: center; margin-top: 20px;'>");
        html.append("<p style='color: #666666; font-size: 14px;'>If the button doesn't work, copy and paste this link:</p>");
        html.append("<p style='color: #1a73e8; word-break: break-all;'>")
            .append(baseUrl).append("/api/auth/verify-email?token=").append(user.getVerificationToken())
            .append("</p>");
        html.append("</div>");
        
        // Footer
        html.append("<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eeeeee; text-align: center;'>");
        html.append("<p style='color: #666666; font-size: 14px;'>This link will expire in 24 hours.</p>");
        html.append("<p style='color: #666666; font-size: 14px;'>If you didn't create an account, you can safely ignore this email.</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</div>");
        
        return html.toString();
    }
    
    public void sendOrderConfirmation(Order order, User user) {
        try {
            Email from = new Email("orders@ecommerce.com", "E-commerce Orders");
            Email to = new Email(user.getEmail());
            String subject = "Order Confirmation - Order #" + order.getId();
            
            String htmlContent = buildOrderConfirmationEmail(order);
            
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);
            
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("Failed to send order confirmation: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error sending order confirmation", e);
        }
    }
    
    private String buildOrderConfirmationEmail(Order order) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        StringBuilder html = new StringBuilder();
        
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>");
        html.append("<div style='background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); padding: 30px;'>");
        
        // Header
        html.append("<div style='text-align: center; margin-bottom: 30px;'>");
        html.append("<h1 style='color: #333333; margin-bottom: 10px;'>Order Confirmation</h1>");
        html.append("<p style='color: #666666; font-size: 16px;'>Thank you for your order!</p>");
        html.append("</div>");
        
        // Order Details
        html.append("<div style='background-color: #f8f9fa; padding: 20px; border-radius: 4px; margin: 20px 0;'>");
        html.append("<h2 style='color: #333333; margin-bottom: 15px; font-size: 18px;'>Order Details</h2>");
        html.append("<p style='margin: 5px 0; color: #666666;'>Order Number: <strong>#").append(order.getId()).append("</strong></p>");
        html.append("<p style='margin: 5px 0; color: #666666;'>Order Date: <strong>").append(order.getOrderDate().format(formatter)).append("</strong></p>");
        html.append("<p style='margin: 5px 0; color: #666666;'>Status: <strong>").append(order.getStatus()).append("</strong></p>");
        html.append("</div>");
        
        // Products Table
        html.append("<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>");
        html.append("<thead>");
        html.append("<tr style='background-color: #f8f9fa;'>");
        html.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #dee2e6;'>Product</th>");
        html.append("<th style='padding: 12px; text-align: right; border-bottom: 2px solid #dee2e6;'>Quantity</th>");
        html.append("<th style='padding: 12px; text-align: right; border-bottom: 2px solid #dee2e6;'>Price</th>");
        html.append("<th style='padding: 12px; text-align: right; border-bottom: 2px solid #dee2e6;'>Subtotal</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");
        
        for (OrderItem item : order.getItems()) {
            html.append("<tr>");
            html.append("<td style='padding: 12px; border-bottom: 1px solid #dee2e6;'>")
                .append(item.getProduct().getName())
                .append("</td>");
            html.append("<td style='padding: 12px; text-align: right; border-bottom: 1px solid #dee2e6;'>")
                .append(item.getQuantity())
                .append("</td>");
            html.append("<td style='padding: 12px; text-align: right; border-bottom: 1px solid #dee2e6;'>$")
                .append(formatPrice(item.getPrice()))
                .append("</td>");
            html.append("<td style='padding: 12px; text-align: right; border-bottom: 1px solid #dee2e6;'>$")
                .append(formatPrice(item.getSubtotal()))
                .append("</td>");
            html.append("</tr>");
        }
        
        html.append("</tbody>");
        html.append("<tfoot>");
        html.append("<tr>");
        html.append("<td colspan='3' style='padding: 12px; text-align: right; font-weight: bold;'>Total:</td>");
        html.append("<td style='padding: 12px; text-align: right; font-weight: bold;'>$")
            .append(formatPrice(order.getTotalAmount()))
            .append("</td>");
        html.append("</tr>");
        html.append("</tfoot>");
        html.append("</table>");
        
        // Shipping Address
        if (order.getShippingAddress() != null) {
            html.append("<div style='background-color: #f8f9fa; padding: 20px; border-radius: 4px; margin: 20px 0;'>");
            html.append("<h2 style='color: #333333; margin-bottom: 15px; font-size: 18px;'>Shipping Address</h2>");
            html.append("<p style='margin: 5px 0; color: #666666;'>").append(order.getShippingAddress().getFullName()).append("</p>");
            html.append("<p style='margin: 5px 0; color: #666666;'>").append(order.getShippingAddress().getAddressLine1()).append("</p>");
            if (order.getShippingAddress().getAddressLine2() != null) {
                html.append("<p style='margin: 5px 0; color: #666666;'>").append(order.getShippingAddress().getAddressLine2()).append("</p>");
            }
            html.append("<p style='margin: 5px 0; color: #666666;'>")
                .append(order.getShippingAddress().getCity()).append(", ")
                .append(order.getShippingAddress().getState()).append(" ")
                .append(order.getShippingAddress().getPostalCode())
                .append("</p>");
            html.append("<p style='margin: 5px 0; color: #666666;'>").append(order.getShippingAddress().getCountry()).append("</p>");
            html.append("</div>");
        }
        
        // Order Tracking
        html.append("<div style='text-align: center; margin: 30px 0;'>");
        html.append("<a href='").append(baseUrl).append("/orders/").append(order.getId())
            .append("' style='background-color: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; ")
            .append("border-radius: 4px; font-size: 16px; display: inline-block;'>Track Your Order</a>");
        html.append("</div>");
        
        // Footer
        html.append("<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eeeeee; text-align: center;'>");
        html.append("<p style='color: #666666; font-size: 14px;'>If you have any questions, please contact our support team.</p>");
        html.append("<p style='color: #666666; font-size: 14px;'>Thank you for shopping with us!</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</div>");
        
        return html.toString();
    }
    
    private String formatPrice(BigDecimal price) {
        return price.setScale(2).toString();
    }
    
    public void sendPasswordResetEmail(User user) {
        try {
            Email from = new Email("noreply@ecommerce.com", "E-commerce");
            Email to = new Email(user.getEmail());
            String subject = "Reset Your Password";
            
            String htmlContent = buildPasswordResetEmail(user);
            
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);
            
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("Failed to send password reset email: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error sending password reset email", e);
        }
    }
    
    private String buildPasswordResetEmail(User user) {
        StringBuilder html = new StringBuilder();
        
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>");
        html.append("<div style='background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); padding: 30px;'>");
        
        // Header
        html.append("<div style='text-align: center; margin-bottom: 30px;'>");
        html.append("<h1 style='color: #333333; margin-bottom: 10px;'>Reset Your Password</h1>");
        html.append("<p style='color: #666666; font-size: 16px;'>Click the button below to reset your password</p>");
        html.append("</div>");
        
        // Reset Button
        html.append("<div style='text-align: center; margin: 30px 0;'>");
        html.append("<a href='").append(baseUrl).append("/reset-password?token=")
            .append(user.getResetToken())
            .append("' style='background-color: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; ")
            .append("border-radius: 4px; font-size: 16px; display: inline-block;'>Reset Password</a>");
        html.append("</div>");
        
        // Alternative Link
        html.append("<div style='text-align: center; margin-top: 20px;'>");
        html.append("<p style='color: #666666; font-size: 14px;'>If the button doesn't work, copy and paste this link:</p>");
        html.append("<p style='color: #1a73e8; word-break: break-all;'>")
            .append(baseUrl).append("/reset-password?token=").append(user.getResetToken())
            .append("</p>");
        html.append("</div>");
        
        // Footer
        html.append("<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eeeeee; text-align: center;'>");
        html.append("<p style='color: #666666; font-size: 14px;'>This link will expire in 1 hour.</p>");
        html.append("<p style='color: #666666; font-size: 14px;'>If you didn't request a password reset, you can safely ignore this email.</p>");
        html.append("</div>");
        
        html.append("</div>");
        html.append("</div>");
        
        return html.toString();
    }
}