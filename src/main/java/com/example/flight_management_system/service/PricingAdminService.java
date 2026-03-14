package com.example.flight_management_system.service;

import com.example.flight_management_system.dto.PricingConfigDTO;
import com.example.flight_management_system.entity.*;
import com.example.flight_management_system.exception.NotFoundException;
import com.example.flight_management_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PricingAdminService {

    private final FareRuleRepository fareRuleRepository;
    private final PricingCampaignRepository pricingCampaignRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final CorporateRateRepository corporateRateRepository;
    private final AirportRepository airportRepository;

    public List<PricingConfigDTO.FareRuleDTO> getFareRules() {
        return fareRuleRepository.findAll().stream().map(this::toFareRuleDTO).toList();
    }

    @Transactional
    public PricingConfigDTO.FareRuleDTO upsertFareRule(Long id, PricingConfigDTO.FareRuleDTO dto) {
        FareRule entity = id == null
                ? new FareRule()
                : fareRuleRepository.findById(id).orElseThrow(() -> new NotFoundException("Fare rule not found"));

        entity.setDepartureAirport(dto.getDepartureAirportId() != null ? airportRepository.findById(dto.getDepartureAirportId())
                .orElseThrow(() -> new NotFoundException("Departure airport not found")) : null);
        entity.setArrivalAirport(dto.getArrivalAirportId() != null ? airportRepository.findById(dto.getArrivalAirportId())
                .orElseThrow(() -> new NotFoundException("Arrival airport not found")) : null);
        entity.setBookingType(dto.getBookingType());
        entity.setBaseFare(dto.getBaseFare());
        entity.setBaseFareMultiplier(dto.getBaseFareMultiplier());
        entity.setRefundable(dto.isRefundable());
        entity.setChangeFee(dto.getChangeFee());
        entity.setIncludedBaggageKg(dto.getIncludedBaggageKg());
        entity.setExtraBaggageFeePerKg(dto.getExtraBaggageFeePerKg());
        entity.setCurrency(dto.getCurrency().toUpperCase());

        return toFareRuleDTO(fareRuleRepository.save(entity));
    }

    @Transactional
    public void deleteFareRule(Long id) {
        fareRuleRepository.deleteById(id);
    }

    public List<PricingConfigDTO.CampaignDTO> getCampaigns() {
        return pricingCampaignRepository.findAll().stream().map(this::toCampaignDTO).toList();
    }

    @Transactional
    public PricingConfigDTO.CampaignDTO upsertCampaign(Long id, PricingConfigDTO.CampaignDTO dto) {
        PricingCampaign entity = id == null
                ? new PricingCampaign()
                : pricingCampaignRepository.findById(id).orElseThrow(() -> new NotFoundException("Campaign not found"));

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setDiscountType(dto.getDiscountType());
        entity.setDiscountValue(dto.getDiscountValue());
        entity.setActive(dto.isActive());
        entity.setStartsAt(dto.getStartsAt());
        entity.setEndsAt(dto.getEndsAt());
        entity.setBookingType(dto.getBookingType());
        entity.setDepartureAirport(dto.getDepartureAirportId() != null ? airportRepository.findById(dto.getDepartureAirportId())
                .orElseThrow(() -> new NotFoundException("Departure airport not found")) : null);
        entity.setArrivalAirport(dto.getArrivalAirportId() != null ? airportRepository.findById(dto.getArrivalAirportId())
                .orElseThrow(() -> new NotFoundException("Arrival airport not found")) : null);

        return toCampaignDTO(pricingCampaignRepository.save(entity));
    }

    @Transactional
    public void deleteCampaign(Long id) {
        pricingCampaignRepository.deleteById(id);
    }

    public List<PricingConfigDTO.PromoCodeDTO> getPromoCodes() {
        return promoCodeRepository.findAll().stream().map(this::toPromoDTO).toList();
    }

    @Transactional
    public PricingConfigDTO.PromoCodeDTO upsertPromoCode(Long id, PricingConfigDTO.PromoCodeDTO dto) {
        PromoCode entity = id == null
                ? new PromoCode()
                : promoCodeRepository.findById(id).orElseThrow(() -> new NotFoundException("Promo code not found"));

        entity.setCode(dto.getCode().trim().toUpperCase());
        entity.setDescription(dto.getDescription());
        entity.setDiscountType(dto.getDiscountType());
        entity.setDiscountValue(dto.getDiscountValue());
        entity.setMinSubtotal(dto.getMinSubtotal());
        entity.setMaxUses(dto.getMaxUses());
        entity.setUsedCount(dto.getUsedCount() != null ? dto.getUsedCount() : 0);
        entity.setActive(dto.isActive());
        entity.setStartsAt(dto.getStartsAt());
        entity.setEndsAt(dto.getEndsAt());

        return toPromoDTO(promoCodeRepository.save(entity));
    }

    @Transactional
    public void deletePromoCode(Long id) {
        promoCodeRepository.deleteById(id);
    }

    public List<PricingConfigDTO.CorporateRateDTO> getCorporateRates() {
        return corporateRateRepository.findAll().stream().map(this::toCorporateDTO).toList();
    }

    @Transactional
    public PricingConfigDTO.CorporateRateDTO upsertCorporateRate(Long id, PricingConfigDTO.CorporateRateDTO dto) {
        CorporateRate entity = id == null
                ? new CorporateRate()
                : corporateRateRepository.findById(id).orElseThrow(() -> new NotFoundException("Corporate rate not found"));

        entity.setCorporateCode(dto.getCorporateCode().trim().toUpperCase());
        entity.setCompanyName(dto.getCompanyName());
        entity.setDiscountPercent(dto.getDiscountPercent());
        entity.setActive(dto.isActive());
        entity.setStartsAt(dto.getStartsAt());
        entity.setEndsAt(dto.getEndsAt());
        entity.setBookingType(dto.getBookingType());
        entity.setDepartureAirport(dto.getDepartureAirportId() != null ? airportRepository.findById(dto.getDepartureAirportId())
                .orElseThrow(() -> new NotFoundException("Departure airport not found")) : null);
        entity.setArrivalAirport(dto.getArrivalAirportId() != null ? airportRepository.findById(dto.getArrivalAirportId())
                .orElseThrow(() -> new NotFoundException("Arrival airport not found")) : null);

        return toCorporateDTO(corporateRateRepository.save(entity));
    }

    @Transactional
    public void deleteCorporateRate(Long id) {
        corporateRateRepository.deleteById(id);
    }

    private PricingConfigDTO.FareRuleDTO toFareRuleDTO(FareRule entity) {
        return PricingConfigDTO.FareRuleDTO.builder()
                .id(entity.getId())
                .departureAirportId(entity.getDepartureAirport() != null ? entity.getDepartureAirport().getId() : null)
                .arrivalAirportId(entity.getArrivalAirport() != null ? entity.getArrivalAirport().getId() : null)
                .bookingType(entity.getBookingType())
                .baseFare(entity.getBaseFare())
                .baseFareMultiplier(entity.getBaseFareMultiplier())
                .refundable(entity.isRefundable())
                .changeFee(entity.getChangeFee())
                .includedBaggageKg(entity.getIncludedBaggageKg())
                .extraBaggageFeePerKg(entity.getExtraBaggageFeePerKg())
                .currency(entity.getCurrency())
                .build();
    }

    private PricingConfigDTO.CampaignDTO toCampaignDTO(PricingCampaign entity) {
        return PricingConfigDTO.CampaignDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .discountType(entity.getDiscountType())
                .discountValue(entity.getDiscountValue())
                .active(entity.isActive())
                .startsAt(entity.getStartsAt())
                .endsAt(entity.getEndsAt())
                .departureAirportId(entity.getDepartureAirport() != null ? entity.getDepartureAirport().getId() : null)
                .arrivalAirportId(entity.getArrivalAirport() != null ? entity.getArrivalAirport().getId() : null)
                .bookingType(entity.getBookingType())
                .build();
    }

    private PricingConfigDTO.PromoCodeDTO toPromoDTO(PromoCode entity) {
        return PricingConfigDTO.PromoCodeDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .description(entity.getDescription())
                .discountType(entity.getDiscountType())
                .discountValue(entity.getDiscountValue())
                .minSubtotal(entity.getMinSubtotal())
                .maxUses(entity.getMaxUses())
                .usedCount(entity.getUsedCount())
                .active(entity.isActive())
                .startsAt(entity.getStartsAt())
                .endsAt(entity.getEndsAt())
                .build();
    }

    private PricingConfigDTO.CorporateRateDTO toCorporateDTO(CorporateRate entity) {
        return PricingConfigDTO.CorporateRateDTO.builder()
                .id(entity.getId())
                .corporateCode(entity.getCorporateCode())
                .companyName(entity.getCompanyName())
                .discountPercent(entity.getDiscountPercent())
                .active(entity.isActive())
                .startsAt(entity.getStartsAt())
                .endsAt(entity.getEndsAt())
                .departureAirportId(entity.getDepartureAirport() != null ? entity.getDepartureAirport().getId() : null)
                .arrivalAirportId(entity.getArrivalAirport() != null ? entity.getArrivalAirport().getId() : null)
                .bookingType(entity.getBookingType())
                .build();
    }
}
