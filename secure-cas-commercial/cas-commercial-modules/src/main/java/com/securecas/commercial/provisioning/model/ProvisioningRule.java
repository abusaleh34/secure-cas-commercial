package com.securecas.commercial.provisioning.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cas_provisioning_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvisioningRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @Column(name = "rule_order", nullable = false)
    private int order = 0;
    
    @Column(name = "source_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ProvisionedUser.ProvisionSource sourceType;
    
    @Column(name = "condition_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConditionType conditionType;
    
    @Column(name = "condition_attribute", length = 100)
    private String conditionAttribute;
    
    @Column(name = "condition_operator", length = 20)
    @Enumerated(EnumType.STRING)
    private ConditionOperator conditionOperator;
    
    @Column(name = "condition_value", length = 255)
    private String conditionValue;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "cas_rule_roles",
        joinColumns = @JoinColumn(name = "rule_id")
    )
    @Column(name = "role")
    private Set<String> assignedRoles = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "cas_rule_groups",
        joinColumns = @JoinColumn(name = "rule_id")
    )
    @Column(name = "group_name")
    private Set<String> assignedGroups = new HashSet<>();
    
    public enum ConditionType {
        ALWAYS,
        ATTRIBUTE_EQUALS,
        ATTRIBUTE_CONTAINS,
        ATTRIBUTE_MATCHES,
        ATTRIBUTE_EXISTS,
        MEMBEROF_GROUP,
        EMAIL_DOMAIN
    }
    
    public enum ConditionOperator {
        EQUALS,
        NOT_EQUALS,
        CONTAINS,
        NOT_CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        MATCHES_REGEX,
        IN,
        NOT_IN
    }
}