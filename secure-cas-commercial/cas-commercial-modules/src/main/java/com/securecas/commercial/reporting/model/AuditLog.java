package com.securecas.commercial.reporting.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cas_audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private String action;
    
    @Column(nullable = false)
    private String principal;
    
    private String service;
    
    @Column(name = "client_ip")
    private String clientIp;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    private boolean success;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}