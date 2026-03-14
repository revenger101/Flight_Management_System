package com.example.flight_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingQuoteDTO {
    private Long flightId;
    private String currency;

    private double baseFare;
    private double demandMultiplier;
    private double seasonMultiplier;
    private double routeMultiplier;
    private double classMultiplier;
    private double bookingWindowMultiplier;

    private boolean refundable;
    private double changeFee;
    private int includedBaggageKg;
    private int requestedBaggageKg;
    private double extraBaggageFee;

    private String appliedCampaign;
    private String appliedPromoCode;
    private String appliedCorporateCode;
    private double campaignDiscount;
    private double promoDiscount;
    private double corporateDiscount;

    private double subtotal;
    private double finalFare;
}
