package com.securecas.commercial.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@Configuration
@ComponentScan(basePackages = "com.securecas.commercial")
@EnableConfigurationProperties(CommercialModuleProperties.class)
@EnableAsync
@EnableScheduling
@ConditionalOnProperty(name = "securecas.commercial.enabled", havingValue = "true", matchIfMissing = true)
public class CommercialModuleConfiguration {
    
}