package com.lumiora.leadconversion.service;

import com.lumiora.leadconversion.dto.LeadConversionRequest;
import com.lumiora.leadconversion.dto.LeadConversionResponse;

public interface LeadConversionService {

    LeadConversionResponse convertLead(
            Long leadId,
            LeadConversionRequest request
    );
}