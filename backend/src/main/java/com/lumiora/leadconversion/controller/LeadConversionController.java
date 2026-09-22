package com.lumiora.leadconversion.controller;

import com.lumiora.leadconversion.dto.LeadConversionRequest;
import com.lumiora.leadconversion.dto.LeadConversionResponse;
import com.lumiora.leadconversion.service.LeadConversionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
public class LeadConversionController {

    private final LeadConversionService leadConversionService;

    public LeadConversionController(
            LeadConversionService leadConversionService
    ) {
        this.leadConversionService = leadConversionService;
    }

    @PostMapping("/{leadId}/convert")
    public ResponseEntity<LeadConversionResponse> convertLead(
            @PathVariable Long leadId,
            @Valid @RequestBody LeadConversionRequest request
    ) {

        LeadConversionResponse response =
                leadConversionService.convertLead(
                        leadId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}