package com.securecas.commercial.provisioning.service;

import com.securecas.commercial.provisioning.event.UserProvisionedEvent;
import com.securecas.commercial.provisioning.model.ProvisionedUser;
import com.securecas.commercial.provisioning.model.ProvisioningRule;
import com.securecas.commercial.provisioning.repository.ProvisionedUserRepository;
import com.securecas.commercial.provisioning.repository.ProvisioningRuleRepository;
import com.securecas.commercial.reporting.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JitProvisioningServiceTest {

    @Mock
    private ProvisionedUserRepository userRepository;

    @Mock
    private ProvisioningRuleRepository ruleRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private JitProvisioningService jitProvisioningService;

    private Map<String, Object> testAttributes;

    @BeforeEach
    void setUp() {
        testAttributes = new HashMap<>();
        testAttributes.put("mail", "test@example.com");
        testAttributes.put("givenName", "Test");
        testAttributes.put("sn", "User");
        testAttributes.put("displayName", "Test User");
        testAttributes.put("department", "IT");
        testAttributes.put("employeeNumber", "12345");
        testAttributes.put("memberOf", Arrays.asList("CN=Developers,OU=Groups,DC=example,DC=com"));
    }

    @Test
    void testProvisionNewUser() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.empty());
        when(ruleRepository.findBySourceTypeAndEnabledTrueOrderByOrderAsc(any())).thenReturn(Collections.emptyList());
        when(userRepository.save(any(ProvisionedUser.class))).thenAnswer(invocation -> {
            ProvisionedUser user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        ProvisionedUser result = jitProvisioningService.provisionUser(username, testAttributes, 
                                                                     ProvisionedUser.ProvisionSource.LDAP);

        // Then
        assertNotNull(result);
        assertEquals(username.toLowerCase(), result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("Test User", result.getDisplayName());
        assertEquals("IT", result.getDepartment());
        assertTrue(result.isActive());
        assertTrue(result.isAutoProvisioned());
        assertTrue(result.getRoles().contains("ROLE_USER"));

        verify(userRepository).save(any(ProvisionedUser.class));
        verify(auditLogRepository).save(any());
        verify(eventPublisher).publishEvent(any(UserProvisionedEvent.class));
    }

    @Test
    void testUpdateExistingUser() {
        // Given
        String username = "testuser";
        ProvisionedUser existingUser = new ProvisionedUser();
        existingUser.setId(1L);
        existingUser.setUsername(username);
        existingUser.setEmail("old@example.com");
        existingUser.setProvisionSource(ProvisionedUser.ProvisionSource.LDAP);

        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(existingUser));
        when(ruleRepository.findBySourceTypeAndEnabledTrueOrderByOrderAsc(any())).thenReturn(Collections.emptyList());
        when(userRepository.save(any(ProvisionedUser.class))).thenReturn(existingUser);

        // When
        ProvisionedUser result = jitProvisioningService.provisionUser(username, testAttributes,
                                                                     ProvisionedUser.ProvisionSource.LDAP);

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertNotNull(result.getLastLoginTimestamp());

        verify(userRepository).save(any(ProvisionedUser.class));
        verify(eventPublisher).publishEvent(any(UserProvisionedEvent.class));
    }

    @Test
    void testProvisioningRuleApplication() {
        // Given
        String username = "testuser";
        ProvisioningRule rule = createTestRule();
        
        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.empty());
        when(ruleRepository.findBySourceTypeAndEnabledTrueOrderByOrderAsc(any()))
            .thenReturn(Collections.singletonList(rule));
        when(userRepository.save(any(ProvisionedUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ProvisionedUser result = jitProvisioningService.provisionUser(username, testAttributes,
                                                                     ProvisionedUser.ProvisionSource.LDAP);

        // Then
        assertTrue(result.getRoles().contains("ROLE_DEVELOPER"));
        assertTrue(result.getGroups().contains("Development"));
    }

    @Test
    void testEmailDomainRule() {
        // Given
        String username = "testuser";
        ProvisioningRule rule = new ProvisioningRule();
        rule.setName("Email Domain Rule");
        rule.setConditionType(ProvisioningRule.ConditionType.EMAIL_DOMAIN);
        rule.setConditionValue("example.com");
        rule.setAssignedRoles(Set.of("ROLE_INTERNAL"));

        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.empty());
        when(ruleRepository.findBySourceTypeAndEnabledTrueOrderByOrderAsc(any()))
            .thenReturn(Collections.singletonList(rule));
        when(userRepository.save(any(ProvisionedUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ProvisionedUser result = jitProvisioningService.provisionUser(username, testAttributes,
                                                                     ProvisionedUser.ProvisionSource.LDAP);

        // Then
        assertTrue(result.getRoles().contains("ROLE_INTERNAL"));
    }

    @Test
    void testDeactivateUser() {
        // Given
        String username = "testuser";
        ProvisionedUser user = new ProvisionedUser();
        user.setUsername(username);
        user.setActive(true);

        when(userRepository.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(user));

        // When
        jitProvisioningService.deactivateUser(username);

        // Then
        assertFalse(user.isActive());
        verify(userRepository).save(user);
        verify(auditLogRepository).save(any());
    }

    private ProvisioningRule createTestRule() {
        ProvisioningRule rule = new ProvisioningRule();
        rule.setName("Developer Rule");
        rule.setConditionType(ProvisioningRule.ConditionType.MEMBEROF_GROUP);
        rule.setConditionValue("Developers");
        rule.setAssignedRoles(Set.of("ROLE_DEVELOPER"));
        rule.setAssignedGroups(Set.of("Development"));
        return rule;
    }
}