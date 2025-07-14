package com.securecas.commercial.provisioning.cas;

import com.securecas.commercial.provisioning.model.ProvisionedUser;
import com.securecas.commercial.provisioning.service.JitProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.*;
import org.apereo.cas.authentication.principal.Principal;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JitProvisioningAuthenticationPostProcessor implements AuthenticationPostProcessor, Ordered {
    
    private final JitProvisioningService jitProvisioningService;
    
    @Override
    public void process(AuthenticationBuilder builder, AuthenticationTransaction transaction) {
        Authentication authentication = builder.build();
        Principal principal = authentication.getPrincipal();
        
        if (principal == null) {
            log.warn("No principal found in authentication, skipping JIT provisioning");
            return;
        }
        
        String username = principal.getId();
        Map<String, Object> attributes = new HashMap<>(principal.getAttributes());
        
        // Determine the provision source
        ProvisionedUser.ProvisionSource source = determineProvisionSource(authentication);
        
        try {
            // Perform JIT provisioning
            ProvisionedUser provisionedUser = jitProvisioningService.provisionUser(username, attributes, source);
            
            // Add provisioned user attributes back to the principal
            Map<String, List<Object>> updatedAttributes = new HashMap<>(principal.getAttributes());
            updatedAttributes.put("jit_provisioned", List.of(true));
            updatedAttributes.put("jit_roles", List.copyOf(provisionedUser.getRoles()));
            updatedAttributes.put("jit_groups", List.copyOf(provisionedUser.getGroups()));
            
            // Update the authentication builder with new attributes
            builder.addAttribute("jit_provisioned", true);
            builder.addAttribute("jit_user_id", provisionedUser.getId());
            
            log.info("Successfully provisioned user {} via JIT", username);
            
        } catch (Exception e) {
            log.error("Error during JIT provisioning for user {}", username, e);
            // Don't fail authentication if JIT provisioning fails
        }
    }
    
    private ProvisionedUser.ProvisionSource determineProvisionSource(Authentication authentication) {
        // Check authentication handler name
        String handlerName = authentication.getAuthenticationHandler().getName();
        
        if (handlerName.toLowerCase().contains("ldap")) {
            return ProvisionedUser.ProvisionSource.LDAP;
        } else if (handlerName.toLowerCase().contains("active") || handlerName.toLowerCase().contains("directory")) {
            return ProvisionedUser.ProvisionSource.ACTIVE_DIRECTORY;
        } else if (handlerName.toLowerCase().contains("oidc") || handlerName.toLowerCase().contains("oauth")) {
            return ProvisionedUser.ProvisionSource.OIDC;
        } else if (handlerName.toLowerCase().contains("saml")) {
            return ProvisionedUser.ProvisionSource.SAML;
        }
        
        // Default to LDAP
        return ProvisionedUser.ProvisionSource.LDAP;
    }
    
    @Override
    public boolean supports(Credential credential) {
        // Support all credential types
        return true;
    }
    
    @Override
    public int getOrder() {
        // Run after standard authentication handlers
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}