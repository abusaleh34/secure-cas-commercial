package com.securecas.commercial.provisioning.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "securecas.commercial.jit")
public class JitProvisioningProperties {
    
    /**
     * Enable JIT provisioning
     */
    private boolean enabled = true;
    
    /**
     * Enable automatic user deactivation for inactive users
     */
    private boolean autoDeactivateEnabled = false;
    
    /**
     * Days of inactivity before user is deactivated
     */
    private int inactiveDaysThreshold = 90;
    
    /**
     * Default roles assigned to all JIT provisioned users
     */
    private List<String> defaultRoles = new ArrayList<>(List.of("ROLE_USER"));
    
    /**
     * Enable attribute synchronization on each login
     */
    private boolean syncAttributesOnLogin = true;
    
    /**
     * Attribute mappings from external sources to internal user model
     */
    private AttributeMappings attributeMappings = new AttributeMappings();
    
    @Data
    public static class AttributeMappings {
        private String username = "uid";
        private String email = "mail";
        private String firstName = "givenName";
        private String lastName = "sn";
        private String displayName = "displayName";
        private String phoneNumber = "telephoneNumber";
        private String department = "department";
        private String employeeId = "employeeNumber";
        private String groups = "memberOf";
    }
}