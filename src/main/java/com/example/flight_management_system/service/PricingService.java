package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.PricingQuoteDTO;
import com.example.flight_management_system.dto.PricingQuoteRequestDTO;
import com.example.flight_management_system.entity.CorporateRate;
import com.example.flight_management_system.entity.FareRule;
import com.example.flight_management_system.entity.Flight;
import com.example.flight_management_system.entity.PricingCampaign;
import com.example.flight_management_system.entity.PromoCode;
import com.example.flight_management_system.entity.enums.BookingStatus;
import com.example.flight_management_system.entity.enums.BookingType;
import com.example.flight_management_system.entity.enums.DiscountType;
import com.example.flight_management_system.exception.BadRequestException;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.BookingRepository;
import com.example.flight_management_system.repository.CorporateRateRepository;
import com.example.flight_management_system.repository.FareRuleRepository;
import com.example.flight_management_system.repository.FlightRepository;
import com.example.flight_management_system.repository.PricingCampaignRepository;
import com.example.flight_management_system.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PricingService {

    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final FareRuleRepository fareRuleRepository;
    private final PricingCampaignRepository pricingCampaignRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final CorporateRateRepository corporateRateRepository;

    public PricingQuoteDTO quote(PricingQuoteRequestDTO request) {
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new NotFoundException("Flight not found with id: " + request.getFlightId()));

        LocalDate travelDate = request.getTravelDate();
        BookingType bookingType = request.getBookingType();
        int baggageKg = Math.max(0, Optional.ofNullable(request.getBaggageKg()).orElse(0));

        long confirmed = bookingRepository.countByFlightIdAndStatusIn(flight.getId(), List.of(BookingStatus.CONFIRMED));
        double demandMultiplier = demandMultiplier(confirmed, flight.getSeatCapacity());
        double seasonMultiplier = seasonMultiplier(travelDate);
        double routeMultiplier = routeMultiplier(flight.getMiles());
        double classMultiplier = classMultiplier(bookingType);
        double bookingWindowMultiplier = bookingWindowMultiplier(LocalDate.now(), travelDate);

        FareRule fareRule = resolveFareRule(flight, bookingType).orElse(null);

        double computedBaseFare = Math.max(55.0, flight.getMiles() * 0.12);
        if (fareRule != null && fareRule.getBaseFare() != null) {
            computedBaseFare = fareRule.getBaseFare();
        }
        if (fareRule != null && fareRule.getBaseFareMultiplier() != null && fareRule.getBaseFareMultiplier() > 0) {
            computedBaseFare *= fareRule.getBaseFareMultiplier();
        }

        boolean refundable = fareRule != null ? fareRule.isRefundable() : bookingType == BookingType.BUSINESS;
        double changeFee = fareRule != null ? fareRule.getChangeFee() : (bookingType == BookingType.BUSINESS ? 25.0 : 85.0);
        int includedBaggageKg = fareRule != null ? fareRule.getIncludedBaggageKg() : (bookingType == BookingType.BUSINESS ? 35 : 20);
        double extraBaggageFeePerKg = fareRule != null ? fareRule.getExtraBaggageFeePerKg() : (bookingType == BookingType.BUSINESS ? 5.5 : 7.0);
        String currency = fareRule != null ? fareRule.getCurrency() : "USD";

        double fareBeforeDiscounts = computedBaseFare
                * demandMultiplier
                * seasonMultiplier
                * routeMultiplier
                * classMultiplier
                * bookingWindowMultiplier;

        double extraBaggageFee = Math.max(0, baggageKg - includedBaggageKg) * extraBaggageFeePerKg;
        double subtotal = round2(fareBeforeDiscounts + extraBaggageFee);

        AppliedDiscount campaignDiscount = resolveCampaignDiscount(flight, bookingType, subtotal);
        double afterCampaign = Math.max(0.0, subtotal - campaignDiscount.amount());

        AppliedDiscount corporateDiscount = resolveCorporateDiscount(request.getCorporateCode(), flight, bookingType, afterCampaign);
        double afterCorporate = Math.max(0.0, afterCampaign - corporateDiscount.amount());

        AppliedDiscount promoDiscount = resolvePromoDiscount(request.getPromoCode(), afterCorporate);
        double finalFare = round2(Math.max(0.0, afterCorporate - promoDiscount.amount()));

        return PricingQuoteDTO.builder()
                .flightId(flight.getId())
                .currency(currency)
                .baseFare(round2(computedBaseFare))
                .demandMultiplier(demandMultiplier)
                .seasonMultiplier(seasonMultiplier)
                .routeMultiplier(routeMultiplier)
                .classMultiplier(classMultiplier)
                .bookingWindowMultiplier(bookingWindowMultiplier)
                .refundable(refundable)
                .changeFee(round2(changeFee))
                .includedBaggageKg(includedBaggageKg)
                .requestedBaggageKg(baggageKg)
                .extraBaggageFee(round2(extraBaggageFee))
                .appliedCampaign(campaignDiscount.code())
                .appliedCorporateCode(corporateDiscount.code())
                .appliedPromoCode(promoDiscount.code())
                .campaignDiscount(round2(campaignDiscount.amount()))
                .corporateDiscount(round2(corporateDiscount.amount()))
                .promoDiscount(round2(promoDiscount.amount()))
                .subtotal(subtotal)
                .finalFare(finalFare)
                .build();
    }

    @Transactional
    public void markPromoCodeUsage(String promoCode) {
        if (promoCode == null || promoCode.isBlank()) {
            return;
        }

        PromoCode promo = promoCodeRepository.findByCodeIgnoreCase(promoCode.trim())
                .orElseThrow(() -> new NotFoundException("Promo code not found"));

        if (promo.getUsedCount() >= promo.getMaxUses()) {
            throw new BadRequestException("Promo code usage limit reached");
        }

        promo.setUsedCount(promo.getUsedCount() + 1);
        promoCodeRepository.save(promo);
    }

    private Optional<FareRule> resolveFareRule(Flight flight, BookingType bookingType) {
        Long departureId = flight.getDepartureAirport() != null ? flight.getDepartureAirport().getId() : null;
        Long arrivalId = flight.getArrivalAirport() != null ? flight.getArrivalAirport().getId() : null;

        if (departureId == null || arrivalId == null) {
            return Optional.empty();
        }

        return fareRuleRepository.findFirstByDepartureAirportIdAndArrivalAirportIdAndBookingType(departureId, arrivalId, bookingType)
                .or(() -> fareRuleRepository.findFirstByDepartureAirportIdAndArrivalAirportIdAndBookingTypeIsNull(departureId, arrivalId));
    }

    private AppliedDiscount resolveCampaignDiscount(Flight flight, BookingType bookingType, double subtotal) {
        LocalDateTime now = LocalDateTime.now();
        return pricingCampaignRepository.findActiveCampaigns(now).stream()
                .filter(campaign -> campaignMatches(campaign, flight, bookingType))
                .map(campaign -> new AppliedDiscount(
                        campaign.getName(),
                        calculateDiscountAmount(campaign.getDiscountType(), campaign.getDiscountValue(), subtotal)
                ))
                .max(Comparator.comparingDouble(AppliedDiscount::amount))
                .orElse(AppliedDiscount.empty());
    }

    private AppliedDiscount resolvePromoDiscount(String promoCode, double subtotal) {
        if (promoCode == null || promoCode.isBlank()) {
            return AppliedDiscount.empty();
        }

        PromoCode promo = promoCodeRepository.findByCodeIgnoreCase(promoCode.trim())
                .orElseThrow(() -> new BadRequestException("Invalid promo code"));

        LocalDateTime now = LocalDateTime.now();
        if (!promo.isActive() || now.isBefore(promo.getStartsAt()) || now.isAfter(promo.getEndsAt())) {
            throw new BadRequestException("Promo code is not active");
        }

        if (promo.getUsedCount() >= promo.getMaxUses()) {
            throw new BadRequestException("Promo code has reached its usage limit");
        }

        if (subtotal < promo.getMinSubtotal()) {
            throw new BadRequestException("Promo code minimum subtotal is " + promo.getMinSubtotal());
        }

        return new AppliedDiscount(
                promo.getCode(),
                calculateDiscountAmount(promo.getDiscountType(), promo.getDiscountValue(), subtotal)
        );
    }

    private AppliedDiscount resolveCorporateDiscount(String corporateCode, Flight flight, BookingType bookingType, double subtotal) {
        if (corporateCode == null || corporateCode.isBlank()) {
            return AppliedDiscount.empty();
        }

        CorporateRate rate = corporateRateRepository.findByCorporateCodeIgnoreCase(corporateCode.trim())
                .orElseThrow(() -> new BadRequestException("Invalid corporate code"));

        LocalDateTime now = LocalDateTime.now();
        if (!rate.isActive() || now.isBefore(rate.getStartsAt()) || now.isAfter(rate.getEndsAt())) {
            throw new BadRequestException("Corporate rate is not active");
        }

        if (rate.getBookingType() != null && rate.getBookingType() != bookingType) {
            throw new BadRequestException("Corporate rate is not valid for booking type " + bookingType);
        }

        if (rate.getDepartureAirport() != null && !rate.getDepartureAirport().getId().equals(flight.getDepartureAirport().getId())) {
            throw new BadRequestException("Corporate rate is not valid for departure airport");
        }

        if (rate.getArrivalAirport() != null && !rate.getArrivalAirport().getId().equals(flight.getArrivalAirport().getId())) {
            throw new BadRequestException("Corporate rate is not valid for arrival airport");
        }

        double discountAmount = round2(subtotal * Math.max(0.0, rate.getDiscountPercent()) / 100.0);
        return new AppliedDiscount(rate.getCorporateCode(), discountAmount);
    }

    private boolean campaignMatches(PricingCampaign campaign, Flight flight, BookingType bookingType) {
        if (campaign.getBookingType() != null && campaign.getBookingType() != bookingType) {
            return false;
        }
        if (campaign.getDepartureAirport() != null && !campaign.getDepartureAirport().getId().equals(flight.getDepartureAirport().getId())) {
            return false;
        }
        return campaign.getArrivalAirport() == null || campaign.getArrivalAirport().getId().equals(flight.getArrivalAirport().getId());
    }

    private double calculateDiscountAmount(DiscountType discountType, double discountValue, double subtotal) {
        if (discountValue <= 0.0) {
            return 0.0;
        }

        if (discountType == DiscountType.FIXED) {
            return round2(Math.min(discountValue, subtotal));
        }

        double boundedPercent = Math.min(discountValue, 70.0);
        return round2(subtotal * boundedPercent / 100.0);
    }

    private double demandMultiplier(long confirmedSeats, int seatCapacity) {
        int safeCapacity = Math.max(seatCapacity, 1);
        double ratio = (double) confirmedSeats / safeCapacity;
        if (ratio < 0.40) {
            return 0.90;
        }
        if (ratio < 0.70) {
            return 1.00;
        }
        if (ratio < 0.90) {
            return 1.15;
        }
        return 1.35;
    }

    private double seasonMultiplier(LocalDate travelDate) {
        int month = travelDate.getMonthValue();
        if (month == 6 || month == 7 || month == 8 || month == 12) {
            return 1.20;
        }
        if (month == 4 || month == 5 || month == 9 || month == 10) {
            return 1.05;
        }
        return 1.00;
    }

    private double routeMultiplier(int miles) {
        if (miles >= 6000) {
            return 1.25;
        }
        if (miles >= 3000) {
            return 1.15;
        }
        if (miles >= 1000) {
            return 1.05;
        }
        return 1.00;
    }

    private double classMultiplier(BookingType bookingType) {
        return bookingType == BookingType.BUSINESS ? 1.70 : 1.00;
    }

    private double bookingWindowMultiplier(LocalDate today, LocalDate travelDate) {
        long daysAhead = Math.max(0, ChronoUnit.DAYS.between(today, travelDate));
        if (daysAhead >= 60) {
            return 0.90;
        }
        if (daysAhead >= 21) {
            return 1.00;
        }
        if (daysAhead >= 7) {
            return 1.10;
        }
        return 1.25;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record AppliedDiscount(String code, double amount) {
        private static AppliedDiscount empty() {
            return new AppliedDiscount(null, 0.0);
        }
    }
}
