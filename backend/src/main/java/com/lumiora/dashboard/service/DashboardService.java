package com.lumiora.dashboard.service;

import org.springframework.security.core.Authentication;

import com.lumiora.dashboard.dto.DashboardResponse;
import com.lumiora.dashboard.dto.FinanceDashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard(
            Authentication authentication
    );

    FinanceDashboardResponse getFinanceDashboard(
            Authentication authentication
    );
}