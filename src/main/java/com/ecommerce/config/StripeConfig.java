package com.ecommerce.config;

import com.stripe.Stripe;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class StripeConfig {
    
    @ConfigProperty(name = "stripe.api.key")
    String stripeApiKey;
    
    void onStart(@Observes StartupEvent ev) {
        Stripe.apiKey = stripeApiKey;
    }
}