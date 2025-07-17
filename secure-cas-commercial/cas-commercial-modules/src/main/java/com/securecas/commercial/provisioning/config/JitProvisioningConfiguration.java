package com.securecas.commercial.provisioning.config;

import com.securecas.commercial.config.CommercialModuleProperties;
import com.securecas.commercial.provisioning.cas.JitProvisioningAuthenticationPostProcessor;
import com.securecas.commercial.provisioning.service.JitProvisioningService;
import com.securecas.commercial.provisioning.service.ProvisioningRuleService;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(JitProvisioningProperties.class)
@EnableScheduling
@ConditionalOnProperty(name = "securecas.commercial.jit.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class JitProvisioningConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    
    @Autowired
    private JitProvisioningAuthenticationPostProcessor jitProvisioningAuthenticationPostProcessor;
    
    @Override
    public void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan) {
        log.info("Registering JIT provisioning authentication post processor");
        plan.registerAuthenticationPostProcessor(jitProvisioningAuthenticationPostProcessor);
    }
    
    @Bean
    public ProvisioningRuleService provisioningRuleService() {
        return new ProvisioningRuleService();
    }
}