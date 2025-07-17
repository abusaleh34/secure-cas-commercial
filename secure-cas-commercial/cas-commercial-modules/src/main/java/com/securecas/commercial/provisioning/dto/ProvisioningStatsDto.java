package com.securecas.commercial.provisioning.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ProvisioningStatsDto {
    
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long autoProvisionedUsers;
    private long manuallyCreatedUsers;
    
    private Map<String, Long> usersBySource;
    private Map<String, Long> usersByRole;
    private Map<String, Long> usersByGroup;
    private Map<String, Long> provisioningTrend;
    
    private long totalProvisioningRules;
    private long activeRules;
    private Map<String, Long> rulesBySource;
    
    private LocalDateTime reportGeneratedAt;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}