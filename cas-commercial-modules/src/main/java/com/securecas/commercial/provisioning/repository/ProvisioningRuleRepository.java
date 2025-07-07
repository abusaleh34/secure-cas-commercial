package com.securecas.commercial.provisioning.repository;

import com.securecas.commercial.provisioning.model.ProvisionedUser;
import com.securecas.commercial.provisioning.model.ProvisioningRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProvisioningRuleRepository extends JpaRepository<ProvisioningRule, Long> {
    
    List<ProvisioningRule> findByEnabledTrueOrderByOrderAsc();
    
    List<ProvisioningRule> findBySourceTypeAndEnabledTrueOrderByOrderAsc(ProvisionedUser.ProvisionSource sourceType);
    
    List<ProvisioningRule> findByNameContainingIgnoreCase(String name);
}