package com.securecas.commercial.provisioning.dto;

import com.securecas.commercial.provisioning.model.ProvisionedUser;
import com.securecas.commercial.provisioning.model.ProvisioningRule;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
public class ProvisioningRuleDto {
    
    @NotBlank(message = "Rule name is required")
    private String name;
    
    private String description;
    
    private boolean enabled = true;
    
    private int order = 0;
    
    @NotNull(message = "Source type is required")
    private ProvisionedUser.ProvisionSource sourceType;
    
    @NotNull(message = "Condition type is required")
    private ProvisioningRule.ConditionType conditionType;
    
    private String conditionAttribute;
    
    private ProvisioningRule.ConditionOperator conditionOperator;
    
    private String conditionValue;
    
    private Set<String> assignedRoles = new HashSet<>();
    
    private Set<String> assignedGroups = new HashSet<>();
}