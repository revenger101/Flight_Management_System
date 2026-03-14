package com.example.flight_management_system.Controller;

import com.example.flight_management_system.dto.PricingConfigDTO;
import com.example.flight_management_system.service.PricingAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PricingAdminController {

    private final PricingAdminService pricingAdminService;

    @GetMapping("/fare-rules")
    public ResponseEntity<List<PricingConfigDTO.FareRuleDTO>> getFareRules() {
        return ResponseEntity.ok(pricingAdminService.getFareRules());
    }

    @PostMapping("/fare-rules")
    public ResponseEntity<PricingConfigDTO.FareRuleDTO> createFareRule(@Valid @RequestBody PricingConfigDTO.FareRuleDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingAdminService.upsertFareRule(null, dto));
    }

    @PutMapping("/fare-rules/{id}")
    public ResponseEntity<PricingConfigDTO.FareRuleDTO> updateFareRule(@PathVariable Long id, @Valid @RequestBody PricingConfigDTO.FareRuleDTO dto) {
        return ResponseEntity.ok(pricingAdminService.upsertFareRule(id, dto));
    }

    @DeleteMapping("/fare-rules/{id}")
    public ResponseEntity<Void> deleteFareRule(@PathVariable Long id) {
        pricingAdminService.deleteFareRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<PricingConfigDTO.CampaignDTO>> getCampaigns() {
        return ResponseEntity.ok(pricingAdminService.getCampaigns());
    }

    @PostMapping("/campaigns")
    public ResponseEntity<PricingConfigDTO.CampaignDTO> createCampaign(@Valid @RequestBody PricingConfigDTO.CampaignDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingAdminService.upsertCampaign(null, dto));
    }

    @PutMapping("/campaigns/{id}")
    public ResponseEntity<PricingConfigDTO.CampaignDTO> updateCampaign(@PathVariable Long id, @Valid @RequestBody PricingConfigDTO.CampaignDTO dto) {
        return ResponseEntity.ok(pricingAdminService.upsertCampaign(id, dto));
    }

    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        pricingAdminService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/promo-codes")
    public ResponseEntity<List<PricingConfigDTO.PromoCodeDTO>> getPromoCodes() {
        return ResponseEntity.ok(pricingAdminService.getPromoCodes());
    }

    @PostMapping("/promo-codes")
    public ResponseEntity<PricingConfigDTO.PromoCodeDTO> createPromoCode(@Valid @RequestBody PricingConfigDTO.PromoCodeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingAdminService.upsertPromoCode(null, dto));
    }

    @PutMapping("/promo-codes/{id}")
    public ResponseEntity<PricingConfigDTO.PromoCodeDTO> updatePromoCode(@PathVariable Long id, @Valid @RequestBody PricingConfigDTO.PromoCodeDTO dto) {
        return ResponseEntity.ok(pricingAdminService.upsertPromoCode(id, dto));
    }

    @DeleteMapping("/promo-codes/{id}")
    public ResponseEntity<Void> deletePromoCode(@PathVariable Long id) {
        pricingAdminService.deletePromoCode(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/corporate-rates")
    public ResponseEntity<List<PricingConfigDTO.CorporateRateDTO>> getCorporateRates() {
        return ResponseEntity.ok(pricingAdminService.getCorporateRates());
    }

    @PostMapping("/corporate-rates")
    public ResponseEntity<PricingConfigDTO.CorporateRateDTO> createCorporateRate(@Valid @RequestBody PricingConfigDTO.CorporateRateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingAdminService.upsertCorporateRate(null, dto));
    }

    @PutMapping("/corporate-rates/{id}")
    public ResponseEntity<PricingConfigDTO.CorporateRateDTO> updateCorporateRate(@PathVariable Long id, @Valid @RequestBody PricingConfigDTO.CorporateRateDTO dto) {
        return ResponseEntity.ok(pricingAdminService.upsertCorporateRate(id, dto));
    }

    @DeleteMapping("/corporate-rates/{id}")
    public ResponseEntity<Void> deleteCorporateRate(@PathVariable Long id) {
        pricingAdminService.deleteCorporateRate(id);
        return ResponseEntity.noContent().build();
    }
}
