package com.lumiora.exam.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultUpdateRequest {

    @DecimalMin(
            value = "0.00",
            message = "Marks cannot be negative"
    )
    private BigDecimal marks;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    private Boolean active;
}