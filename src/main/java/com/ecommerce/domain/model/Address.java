package com.ecommerce.domain.model;

import lombok.Data;
import jakarta.persistence.Embeddable;

@Data
@Embeddable
public class Address {
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}