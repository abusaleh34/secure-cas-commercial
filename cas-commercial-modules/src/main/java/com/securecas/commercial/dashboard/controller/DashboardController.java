package com.securecas.commercial.dashboard.controller;

import com.securecas.commercial.dashboard.service.DashboardService;
import com.securecas.commercial.reporting.model.SystemMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("metrics", dashboardService.getSystemMetrics());
        model.addAttribute("recentLogins", dashboardService.getRecentLogins());
        model.addAttribute("activeServices", dashboardService.getActiveServices());
        return "dashboard/index";
    }
    
    @GetMapping("/metrics")
    @ResponseBody
    public SystemMetrics getMetrics() {
        return dashboardService.getSystemMetrics();
    }
    
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", dashboardService.getActiveUsers());
        return "dashboard/users";
    }
    
    @GetMapping("/services")
    public String services(Model model) {
        model.addAttribute("services", dashboardService.getRegisteredServices());
        return "dashboard/services";
    }
    
    @GetMapping("/audit")
    public String audit(Model model) {
        model.addAttribute("auditLogs", dashboardService.getRecentAuditLogs());
        return "dashboard/audit";
    }
    
    @GetMapping("/analytics")
    @ResponseBody
    public Map<String, Object> getAnalytics() {
        return dashboardService.getAnalyticsData();
    }
}