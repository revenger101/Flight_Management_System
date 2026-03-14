package com.example.flight_management_system.dto;

import com.example.flight_management_system.entity.enums.BookingType;
import com.example.flight_management_system.entity.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class PricingConfigDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FareRuleDTO {
        private Long id;
        private Long departureAirportId;
        private Long arrivalAirportId;
        private BookingType bookingType;
        @DecimalMin(value = "0.0", inclusive = false)
        private Double baseFare;
        @DecimalMin(value = "0.0", inclusive = false)
        private Double baseFareMultiplier;
        private boolean refundable;
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        private Double changeFee;
        @NotNull
        private Integer includedBaggageKg;
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        private Double extraBaggageFeePerKg;
        @NotBlank
        @Size(min = 3, max = 3)
        private String currency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CampaignDTO {
        private Long id;
        @NotBlank
        private String name;
        private String description;
        @NotNull
        private DiscountType discountType;
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private Double discountValue;
        private boolean active;
        @NotNull
        private LocalDateTime startsAt;
        @NotNull
        @Future
        private LocalDateTime endsAt;
        private Long departureAirportId;
        private Long arrivalAirportId;
        private BookingType bookingType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PromoCodeDTO {
        private Long id;
        @NotBlank
        @Size(max = 40)
        private String code;
        private String description;
        @NotNull
        private DiscountType discountType;
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private Double discountValue;
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        private Double minSubtotal;
        @NotNull
        private Integer maxUses;
        private Integer usedCount;
        private boolean active;
        @NotNull
        private LocalDateTime startsAt;
        @NotNull
        @Future
        private LocalDateTime endsAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CorporateRateDTO {
        private Long id;
        @NotBlank
        @Size(max = 40)
        private String corporateCode;
        @NotBlank
        @Size(max = 120)
        private String companyName;
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        private Double discountPercent;
        private boolean active;
        @NotNull
        private LocalDateTime startsAt;
        @NotNull
        @Future
        private LocalDateTime endsAt;
        private Long departureAirportId;
        private Long arrivalAirportId;
        private BookingType bookingType;
    }
}
