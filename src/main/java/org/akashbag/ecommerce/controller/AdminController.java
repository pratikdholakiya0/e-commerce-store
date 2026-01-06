package org.akashbag.ecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.akashbag.ecommerce.dto.response.DashBoardResponse;
import org.akashbag.ecommerce.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashBoardResponse> getDashboardStats() {
        return ResponseEntity.ok(analyticsService.getAnalytics());
    }
}