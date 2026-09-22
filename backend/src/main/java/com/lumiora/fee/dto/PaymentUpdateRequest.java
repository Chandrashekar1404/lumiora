package com.lumiora.fee.dto;

import com.lumiora.fee.entity.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentUpdateRequest {

    private PaymentMethod paymentMethod;

    private String referenceNumber;

    private String notes;
}