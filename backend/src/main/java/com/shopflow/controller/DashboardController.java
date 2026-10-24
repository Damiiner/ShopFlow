package com.shopflow.controller;

import com.shopflow.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> admin() {
        return dashboardService.admin();
    }

    @GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    public Map<String, Object> seller() {
        return dashboardService.seller();
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Map<String, Object> customer() {
        return dashboardService.customer();
    }
}
