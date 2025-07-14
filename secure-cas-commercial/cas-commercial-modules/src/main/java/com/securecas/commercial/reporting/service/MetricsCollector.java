package com.securecas.commercial.reporting.service;

import com.securecas.commercial.reporting.model.SystemMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsCollector {
    
    public SystemMetrics collectSystemMetrics() {
        SystemMetrics metrics = new SystemMetrics();
        
        // Mock data for demonstration - in production, these would come from actual CAS metrics
        metrics.setTotalUsers(1500);
        metrics.setActiveUsers(250);
        metrics.setTotalServices(25);
        metrics.setActiveServices(20);
        metrics.setTotalAuthenticationAttempts(15000);
        metrics.setSuccessfulAuthentications(14250);
        metrics.setFailedAuthentications(750);
        metrics.setAuthenticationSuccessRate(95.0);
        metrics.setActiveSessions(180);
        metrics.setTotalTicketsCreated(45000);
        
        // System metrics
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        metrics.setSystemCpuUsage(osBean.getSystemLoadAverage() * 100 / osBean.getAvailableProcessors());
        
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        metrics.setSystemMemoryUsage((double) usedMemory / totalMemory * 100);
        
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        metrics.setSystemUptime(runtimeBean.getUptime() / 1000); // Convert to seconds
        
        metrics.setCasVersion("7.0.0");
        metrics.setJavaVersion(System.getProperty("java.version"));
        
        return metrics;
    }
}