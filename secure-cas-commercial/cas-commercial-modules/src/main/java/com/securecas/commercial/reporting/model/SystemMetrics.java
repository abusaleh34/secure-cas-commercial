package com.securecas.commercial.reporting.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemMetrics {
    private LocalDateTime timestamp = LocalDateTime.now();
    private long totalUsers;
    private long activeUsers;
    private long totalServices;
    private long activeServices;
    private long totalAuthenticationAttempts;
    private long successfulAuthentications;
    private long failedAuthentications;
    private double authenticationSuccessRate;
    private long activeSessions;
    private long totalTicketsCreated;
    private double systemCpuUsage;
    private double systemMemoryUsage;
    private long systemUptime;
    private String casVersion;
    private String javaVersion;
}