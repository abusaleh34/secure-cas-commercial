package com.securecas.commercial.provisioning.repository;

import com.securecas.commercial.provisioning.model.ProvisionedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProvisionedUserRepository extends JpaRepository<ProvisionedUser, Long> {
    
    Optional<ProvisionedUser> findByUsername(String username);
    
    Optional<ProvisionedUser> findByUsernameIgnoreCase(String username);
    
    Optional<ProvisionedUser> findByEmail(String email);
    
    Optional<ProvisionedUser> findByExternalId(String externalId);
    
    List<ProvisionedUser> findByProvisionSource(ProvisionedUser.ProvisionSource source);
    
    List<ProvisionedUser> findByActive(boolean active);
    
    List<ProvisionedUser> findByAutoProvisioned(boolean autoProvisioned);
    
    @Query("SELECT u FROM ProvisionedUser u WHERE u.provisionTimestamp >= :startDate AND u.provisionTimestamp <= :endDate")
    List<ProvisionedUser> findUsersProvisionedBetween(@Param("startDate") LocalDateTime startDate, 
                                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT u FROM ProvisionedUser u WHERE :role MEMBER OF u.roles")
    List<ProvisionedUser> findByRole(@Param("role") String role);
    
    @Query("SELECT u FROM ProvisionedUser u WHERE :group MEMBER OF u.groups")
    List<ProvisionedUser> findByGroup(@Param("group") String group);
    
    @Query("SELECT COUNT(u) FROM ProvisionedUser u WHERE u.provisionSource = :source AND u.provisionTimestamp >= :since")
    long countProvisionedUsersSince(@Param("source") ProvisionedUser.ProvisionSource source, 
                                   @Param("since") LocalDateTime since);
    
    @Query("SELECT u FROM ProvisionedUser u WHERE u.lastLoginTimestamp < :threshold AND u.active = true")
    List<ProvisionedUser> findInactiveUsers(@Param("threshold") LocalDateTime threshold);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}