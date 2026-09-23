package com.lumiora.dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumiora.dashboard.dto.DashboardResponse;
import com.lumiora.dashboard.dto.FinanceDashboardResponse;
import com.lumiora.dashboard.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;


    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                dashboardService.getDashboard(
                        authentication
                )
        );
    }


    @GetMapping("/finance")
    public ResponseEntity<FinanceDashboardResponse> getFinanceDashboard(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                dashboardService.getFinanceDashboard(
                        authentication
                )
        );
    }
}