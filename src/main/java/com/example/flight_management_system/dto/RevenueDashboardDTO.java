package com.example.flight_management_system.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueDashboardDTO {
    private Double totalRevenue;
    private Double totalRefunded;
    private Double netRevenue;
    private Double ancillaryRevenue;
    private List<RouteRevenueDTO> routeProfitability;
    private List<YieldDTO> occupancyVsYield;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RouteRevenueDTO {
        private String route;
        private Long bookings;
        private Double grossRevenue;
        private Double refunded;
        private Double netRevenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class YieldDTO {
        private Long flightId;
        private String route;
        private Integer seatCapacity;
        private Long confirmedBookings;
        private Double occupancyPercent;
        private Double avgYield;
    }
}
