package com.securecas.commercial.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "securecas.commercial")
public class CommercialModuleProperties {
    
    private boolean enabled = true;
    
    private Dashboard dashboard = new Dashboard();
    private Reporting reporting = new Reporting();
    private Mfa mfa = new Mfa();
    private Integration integration = new Integration();
    
    @Data
    public static class Dashboard {
        private boolean enabled = true;
        private String adminRole = "ROLE_ADMIN";
        private String baseUrl = "/admin";
        private int sessionTimeout = 3600;
    }
    
    @Data
    public static class Reporting {
        private boolean enabled = true;
        private int retentionDays = 90;
        private boolean realTimeAnalytics = true;
        private String exportPath = "/var/cas/reports";
    }
    
    @Data
    public static class Mfa {
        private boolean smsEnabled = true;
        private boolean emailEnabled = true;
        private String twilioAccountSid;
        private String twilioAuthToken;
        private String twilioFromNumber;
        private int otpLength = 6;
        private int otpValiditySeconds = 300;
    }
    
    @Data
    public static class Integration {
        private OracleEbs oracleEbs = new OracleEbs();
        
        @Data
        public static class OracleEbs {
            private boolean enabled = false;
            private String endpoint;
            private String username;
            private String password;
            private int connectionTimeout = 30000;
            private int readTimeout = 60000;
        }
    }
}