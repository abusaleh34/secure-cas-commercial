package com.securecas.commercial.reporting.repository;

import com.securecas.commercial.reporting.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByPrincipal(String principal);
    
    List<AuditLog> findByAction(String action);
    
    List<AuditLog> findBySuccess(boolean success);
    
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :start ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentLogs(@Param("start") LocalDateTime start);
    
    @Query("SELECT a FROM AuditLog a WHERE a.action LIKE :pattern")
    List<AuditLog> findByActionPattern(@Param("pattern") String pattern);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action AND a.timestamp >= :since")
    long countByActionSince(@Param("action") String action, @Param("since") LocalDateTime since);
}