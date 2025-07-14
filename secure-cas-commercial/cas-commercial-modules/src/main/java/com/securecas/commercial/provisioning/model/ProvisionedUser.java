package com.securecas.commercial.provisioning.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cas_provisioned_users", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true),
    @Index(name = "idx_external_id", columnList = "external_id"),
    @Index(name = "idx_provision_source", columnList = "provision_source")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class ProvisionedUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String username;
    
    @Column(name = "external_id", length = 255)
    private String externalId;
    
    @Column(nullable = false, length = 255)
    private String email;
    
    @Column(name = "first_name", length = 100)
    private String firstName;
    
    @Column(name = "last_name", length = 100)
    private String lastName;
    
    @Column(name = "display_name", length = 255)
    private String displayName;
    
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;
    
    @Column(name = "department", length = 100)
    private String department;
    
    @Column(name = "employee_id", length = 50)
    private String employeeId;
    
    @Column(name = "provision_source", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ProvisionSource provisionSource;
    
    @Column(name = "provision_timestamp", nullable = false)
    private LocalDateTime provisionTimestamp;
    
    @Column(name = "last_login_timestamp")
    private LocalDateTime lastLoginTimestamp;
    
    @Column(name = "last_updated_timestamp")
    private LocalDateTime lastUpdatedTimestamp;
    
    @Column(nullable = false)
    private boolean active = true;
    
    @Column(name = "auto_provisioned", nullable = false)
    private boolean autoProvisioned = true;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "cas_user_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "cas_user_groups",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "group_name")
    private Set<String> groups = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(
        name = "cas_user_attributes",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @MapKeyColumn(name = "attribute_name")
    @Column(name = "attribute_value", columnDefinition = "TEXT")
    private Map<String, String> attributes = new HashMap<>();
    
    @PrePersist
    protected void onCreate() {
        provisionTimestamp = LocalDateTime.now();
        lastUpdatedTimestamp = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdatedTimestamp = LocalDateTime.now();
    }
    
    public enum ProvisionSource {
        LDAP,
        ACTIVE_DIRECTORY,
        OIDC,
        SAML,
        MANUAL
    }
}