package com.ecommerce.domain.model;

import lombok.Data;

import jakarta.persistence.Embeddable;

@Data
@Embeddable
public class ShippingAddress {
    private String fullName;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;
}