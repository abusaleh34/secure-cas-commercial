package com.securecas.commercial.provisioning.service;

import com.securecas.commercial.provisioning.event.UserProvisionedEvent;
import com.securecas.commercial.provisioning.model.ProvisionedUser;
import com.securecas.commercial.provisioning.model.ProvisioningRule;
import com.securecas.commercial.provisioning.repository.ProvisionedUserRepository;
import com.securecas.commercial.provisioning.repository.ProvisioningRuleRepository;
import com.securecas.commercial.reporting.model.AuditLog;
import com.securecas.commercial.reporting.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JitProvisioningService {
    
    private final ProvisionedUserRepository userRepository;
    private final ProvisioningRuleRepository ruleRepository;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public ProvisionedUser provisionUser(String username, Map<String, Object> attributes, 
                                       ProvisionedUser.ProvisionSource source) {
        log.info("Starting JIT provisioning for user: {} from source: {}", username, source);
        
        // Check if user already exists
        Optional<ProvisionedUser> existingUser = userRepository.findByUsernameIgnoreCase(username);
        
        if (existingUser.isPresent()) {
            log.debug("User {} already exists, updating attributes", username);
            return updateExistingUser(existingUser.get(), attributes, source);
        } else {
            log.info("Creating new user: {}", username);
            return createNewUser(username, attributes, source);
        }
    }
    
    private ProvisionedUser createNewUser(String username, Map<String, Object> attributes, 
                                        ProvisionedUser.ProvisionSource source) {
        ProvisionedUser user = new ProvisionedUser();
        user.setUsername(username.toLowerCase());
        user.setProvisionSource(source);
        user.setAutoProvisioned(true);
        user.setActive(true);
        
        // Map attributes
        mapAttributesToUser(user, attributes);
        
        // Apply provisioning rules
        applyProvisioningRules(user, attributes, source);
        
        // Save user
        user = userRepository.save(user);
        
        // Audit the provisioning
        auditProvisioningEvent(user, true, attributes);
        
        // Publish event
        eventPublisher.publishEvent(new UserProvisionedEvent(this, user, true, source, attributes));
        
        log.info("Successfully provisioned new user: {}", username);
        return user;
    }
    
    private ProvisionedUser updateExistingUser(ProvisionedUser user, Map<String, Object> attributes,
                                             ProvisionedUser.ProvisionSource source) {
        // Update last login
        user.setLastLoginTimestamp(LocalDateTime.now());
        
        // Update attributes if changed
        boolean attributesChanged = updateUserAttributes(user, attributes);
        
        if (attributesChanged) {
            // Re-apply provisioning rules in case of changes
            applyProvisioningRules(user, attributes, source);
        }
        
        // Save user
        user = userRepository.save(user);
        
        // Audit if attributes changed
        if (attributesChanged) {
            auditProvisioningEvent(user, false, attributes);
        }
        
        // Publish event
        eventPublisher.publishEvent(new UserProvisionedEvent(this, user, false, source, attributes));
        
        return user;
    }
    
    private void mapAttributesToUser(ProvisionedUser user, Map<String, Object> attributes) {
        // Map standard attributes
        user.setEmail(getAttributeValue(attributes, "mail", "email"));
        user.setFirstName(getAttributeValue(attributes, "givenName", "given_name", "firstName"));
        user.setLastName(getAttributeValue(attributes, "sn", "family_name", "lastName"));
        user.setDisplayName(getAttributeValue(attributes, "displayName", "cn", "name"));
        user.setPhoneNumber(getAttributeValue(attributes, "telephoneNumber", "phone_number", "mobile"));
        user.setDepartment(getAttributeValue(attributes, "department", "ou"));
        user.setEmployeeId(getAttributeValue(attributes, "employeeNumber", "employee_id"));
        user.setExternalId(getAttributeValue(attributes, "uid", "sub", "objectGUID"));
        
        // If display name is not set, construct it
        if (!StringUtils.hasText(user.getDisplayName())) {
            if (StringUtils.hasText(user.getFirstName()) && StringUtils.hasText(user.getLastName())) {
                user.setDisplayName(user.getFirstName() + " " + user.getLastName());
            } else {
                user.setDisplayName(user.getUsername());
            }
        }
        
        // Store all attributes
        Map<String, String> userAttributes = new HashMap<>();
        attributes.forEach((key, value) -> {
            if (value != null) {
                userAttributes.put(key, value.toString());
            }
        });
        user.setAttributes(userAttributes);
    }
    
    private boolean updateUserAttributes(ProvisionedUser user, Map<String, Object> attributes) {
        boolean changed = false;
        
        String newEmail = getAttributeValue(attributes, "mail", "email");
        if (!Objects.equals(user.getEmail(), newEmail)) {
            user.setEmail(newEmail);
            changed = true;
        }
        
        String newFirstName = getAttributeValue(attributes, "givenName", "given_name", "firstName");
        if (!Objects.equals(user.getFirstName(), newFirstName)) {
            user.setFirstName(newFirstName);
            changed = true;
        }
        
        String newLastName = getAttributeValue(attributes, "sn", "family_name", "lastName");
        if (!Objects.equals(user.getLastName(), newLastName)) {
            user.setLastName(newLastName);
            changed = true;
        }
        
        String newDisplayName = getAttributeValue(attributes, "displayName", "cn", "name");
        if (!Objects.equals(user.getDisplayName(), newDisplayName)) {
            user.setDisplayName(newDisplayName);
            changed = true;
        }
        
        String newDepartment = getAttributeValue(attributes, "department", "ou");
        if (!Objects.equals(user.getDepartment(), newDepartment)) {
            user.setDepartment(newDepartment);
            changed = true;
        }
        
        // Update stored attributes
        Map<String, String> newAttributes = new HashMap<>();
        attributes.forEach((key, value) -> {
            if (value != null) {
                newAttributes.put(key, value.toString());
            }
        });
        
        if (!user.getAttributes().equals(newAttributes)) {
            user.setAttributes(newAttributes);
            changed = true;
        }
        
        return changed;
    }
    
    private void applyProvisioningRules(ProvisionedUser user, Map<String, Object> attributes,
                                       ProvisionedUser.ProvisionSource source) {
        List<ProvisioningRule> rules = ruleRepository.findBySourceTypeAndEnabledTrueOrderByOrderAsc(source);
        
        for (ProvisioningRule rule : rules) {
            if (evaluateRule(rule, attributes)) {
                log.debug("Applying rule '{}' to user {}", rule.getName(), user.getUsername());
                
                // Assign roles
                if (!rule.getAssignedRoles().isEmpty()) {
                    user.getRoles().addAll(rule.getAssignedRoles());
                }
                
                // Assign groups
                if (!rule.getAssignedGroups().isEmpty()) {
                    user.getGroups().addAll(rule.getAssignedGroups());
                }
            }
        }
        
        // Apply default roles if no roles assigned
        if (user.getRoles().isEmpty()) {
            user.getRoles().add("ROLE_USER");
        }
    }
    
    private boolean evaluateRule(ProvisioningRule rule, Map<String, Object> attributes) {
        switch (rule.getConditionType()) {
            case ALWAYS:
                return true;
                
            case ATTRIBUTE_EQUALS:
                String attrValue = getAttributeValue(attributes, rule.getConditionAttribute());
                return rule.getConditionValue().equalsIgnoreCase(attrValue);
                
            case ATTRIBUTE_CONTAINS:
                attrValue = getAttributeValue(attributes, rule.getConditionAttribute());
                return attrValue != null && attrValue.toLowerCase()
                    .contains(rule.getConditionValue().toLowerCase());
                
            case ATTRIBUTE_MATCHES:
                attrValue = getAttributeValue(attributes, rule.getConditionAttribute());
                if (attrValue == null) return false;
                return Pattern.compile(rule.getConditionValue()).matcher(attrValue).matches();
                
            case ATTRIBUTE_EXISTS:
                return attributes.containsKey(rule.getConditionAttribute()) && 
                       attributes.get(rule.getConditionAttribute()) != null;
                
            case MEMBEROF_GROUP:
                Object memberOf = attributes.get("memberOf");
                if (memberOf == null) return false;
                
                Set<String> groups = new HashSet<>();
                if (memberOf instanceof Collection) {
                    groups = ((Collection<?>) memberOf).stream()
                        .map(Object::toString)
                        .collect(Collectors.toSet());
                } else {
                    groups.add(memberOf.toString());
                }
                
                return groups.stream().anyMatch(group -> 
                    group.toLowerCase().contains(rule.getConditionValue().toLowerCase()));
                
            case EMAIL_DOMAIN:
                String email = getAttributeValue(attributes, "mail", "email");
                if (email == null) return false;
                String domain = email.substring(email.indexOf("@") + 1);
                return domain.equalsIgnoreCase(rule.getConditionValue());
                
            default:
                return false;
        }
    }
    
    private String getAttributeValue(Map<String, Object> attributes, String... keys) {
        for (String key : keys) {
            Object value = attributes.get(key);
            if (value != null) {
                if (value instanceof Collection) {
                    Collection<?> collection = (Collection<?>) value;
                    if (!collection.isEmpty()) {
                        return collection.iterator().next().toString();
                    }
                } else {
                    return value.toString();
                }
            }
        }
        return null;
    }
    
    private void auditProvisioningEvent(ProvisionedUser user, boolean newUser, 
                                       Map<String, Object> attributes) {
        AuditLog audit = new AuditLog();
        audit.setAction(newUser ? "JIT_USER_CREATED" : "JIT_USER_UPDATED");
        audit.setPrincipal(user.getUsername());
        audit.setSuccess(true);
        audit.setDetails(String.format(
            "JIT provisioning %s user %s from %s. Roles: %s, Groups: %s",
            newUser ? "created" : "updated",
            user.getUsername(),
            user.getProvisionSource(),
            user.getRoles(),
            user.getGroups()
        ));
        
        auditLogRepository.save(audit);
    }
    
    public void deactivateUser(String username) {
        userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
            
            AuditLog audit = new AuditLog();
            audit.setAction("JIT_USER_DEACTIVATED");
            audit.setPrincipal(username);
            audit.setSuccess(true);
            audit.setDetails("User deactivated");
            auditLogRepository.save(audit);
        });
    }
    
    public List<ProvisionedUser> findInactiveUsers(int daysInactive) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysInactive);
        return userRepository.findInactiveUsers(threshold);
    }
}