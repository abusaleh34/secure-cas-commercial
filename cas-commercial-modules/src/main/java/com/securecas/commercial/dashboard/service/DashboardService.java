package com.securecas.commercial.dashboard.service;

import com.securecas.commercial.reporting.model.AuditLog;
import com.securecas.commercial.reporting.model.SystemMetrics;
import com.securecas.commercial.reporting.service.MetricsCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    
    private final MetricsCollector metricsCollector;
    
    public SystemMetrics getSystemMetrics() {
        return metricsCollector.collectSystemMetrics();
    }
    
    public List<Map<String, Object>> getRecentLogins() {
        List<Map<String, Object>> logins = new ArrayList<>();
        // This would be populated from actual authentication events
        for (int i = 0; i < 10; i++) {
            Map<String, Object> login = new HashMap<>();
            login.put("username", "user" + i);
            login.put("timestamp", LocalDateTime.now().minusMinutes(i * 5));
            login.put("ipAddress", "192.168.1." + (100 + i));
            login.put("service", "Service " + (i % 3 + 1));
            login.put("success", i % 4 != 0);
            logins.add(login);
        }
        return logins;
    }
    
    public List<Map<String, Object>> getActiveServices() {
        List<Map<String, Object>> services = new ArrayList<>();
        // This would be populated from actual service registry
        String[] serviceNames = {"HR Portal", "Finance System", "Email Service", "Document Management", "Oracle EBS"};
        for (int i = 0; i < serviceNames.length; i++) {
            Map<String, Object> service = new HashMap<>();
            service.put("id", 1000 + i);
            service.put("name", serviceNames[i]);
            service.put("url", "https://service" + i + ".example.com");
            service.put("enabled", true);
            service.put("ssoEnabled", true);
            service.put("activeUsers", (i + 1) * 25);
            services.add(service);
        }
        return services;
    }
    
    public List<Map<String, Object>> getActiveUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        // This would be populated from actual user sessions
        for (int i = 0; i < 20; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("username", "user" + i);
            user.put("fullName", "User " + i);
            user.put("email", "user" + i + "@example.com");
            user.put("department", i % 2 == 0 ? "IT" : "Finance");
            user.put("lastLogin", LocalDateTime.now().minusHours(i));
            user.put("activeSessions", i % 3 + 1);
            users.add(user);
        }
        return users;
    }
    
    public List<Map<String, Object>> getRegisteredServices() {
        return getActiveServices();
    }
    
    public List<AuditLog> getRecentAuditLogs() {
        List<AuditLog> logs = new ArrayList<>();
        String[] actions = {"AUTHENTICATION_SUCCESS", "AUTHENTICATION_FAILED", "TICKET_GRANTING_TICKET_CREATED", 
                           "SERVICE_TICKET_CREATED", "LOGOUT", "SERVICE_ACCESS_DENIED"};
        
        for (int i = 0; i < 50; i++) {
            AuditLog log = new AuditLog();
            log.setId((long) i);
            log.setTimestamp(LocalDateTime.now().minusMinutes(i * 2));
            log.setAction(actions[i % actions.length]);
            log.setPrincipal("user" + (i % 10));
            log.setClientIp("192.168.1." + (100 + i % 50));
            log.setService(i % 2 == 0 ? "Service " + (i % 3 + 1) : null);
            log.setSuccess(i % 5 != 0);
            logs.add(log);
        }
        return logs;
    }
    
    public Map<String, Object> getAnalyticsData() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Authentication trends
        List<Map<String, Object>> authTrends = new ArrayList<>();
        for (int i = 7; i >= 0; i--) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", LocalDateTime.now().minusDays(i).toLocalDate());
            dayData.put("successful", 150 + (int)(Math.random() * 50));
            dayData.put("failed", 10 + (int)(Math.random() * 20));
            authTrends.add(dayData);
        }
        analytics.put("authenticationTrends", authTrends);
        
        // Service usage
        Map<String, Integer> serviceUsage = new HashMap<>();
        getActiveServices().forEach(service -> 
            serviceUsage.put((String) service.get("name"), (Integer) service.get("activeUsers"))
        );
        analytics.put("serviceUsage", serviceUsage);
        
        // User distribution by department
        Map<String, Integer> departmentDistribution = new HashMap<>();
        departmentDistribution.put("IT", 45);
        departmentDistribution.put("Finance", 32);
        departmentDistribution.put("HR", 28);
        departmentDistribution.put("Operations", 25);
        analytics.put("departmentDistribution", departmentDistribution);
        
        return analytics;
    }
}