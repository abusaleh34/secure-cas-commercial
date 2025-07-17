package com.securecas.commercial.provisioning.service;

import com.securecas.commercial.provisioning.dto.ProvisioningRuleDto;
import com.securecas.commercial.provisioning.dto.ProvisioningStatsDto;
import com.securecas.commercial.provisioning.model.ProvisionedUser;
import com.securecas.commercial.provisioning.model.ProvisioningRule;
import com.securecas.commercial.provisioning.repository.ProvisionedUserRepository;
import com.securecas.commercial.provisioning.repository.ProvisioningRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProvisioningRuleService {
    
    private final ProvisionedUserRepository userRepository;
    private final ProvisioningRuleRepository ruleRepository;
    private final EntityManager entityManager;
    
    public Page<ProvisionedUser> searchProvisionedUsers(ProvisionedUser.ProvisionSource source,
                                                       Boolean active, String search, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProvisionedUser> query = cb.createQuery(ProvisionedUser.class);
        Root<ProvisionedUser> root = query.from(ProvisionedUser.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (source != null) {
            predicates.add(cb.equal(root.get("provisionSource"), source));
        }
        
        if (active != null) {
            predicates.add(cb.equal(root.get("active"), active));
        }
        
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("username")), searchPattern),
                cb.like(cb.lower(root.get("email")), searchPattern),
                cb.like(cb.lower(root.get("displayName")), searchPattern)
            ));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        
        TypedQuery<ProvisionedUser> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<ProvisionedUser> results = typedQuery.getResultList();
        long total = getTotalCount(predicates);
        
        return new PageImpl<>(results, pageable, total);
    }
    
    private long getTotalCount(List<Predicate> predicates) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProvisionedUser> root = countQuery.from(ProvisionedUser.class);
        
        countQuery.select(cb.count(root));
        countQuery.where(predicates.toArray(new Predicate[0]));
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
    
    public Optional<ProvisionedUser> findUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }
    
    public void activateUser(String username) {
        userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
            user.setActive(true);
            userRepository.save(user);
        });
    }
    
    public List<ProvisioningRule> getProvisioningRules(ProvisionedUser.ProvisionSource source, Boolean enabled) {
        if (source != null && enabled != null) {
            return ruleRepository.findBySourceTypeAndEnabledTrueOrderByOrderAsc(source);
        } else if (enabled != null && enabled) {
            return ruleRepository.findByEnabledTrueOrderByOrderAsc();
        } else {
            return ruleRepository.findAll();
        }
    }
    
    public Optional<ProvisioningRule> findRuleById(Long id) {
        return ruleRepository.findById(id);
    }
    
    public ProvisioningRule createRule(ProvisioningRuleDto dto) {
        ProvisioningRule rule = new ProvisioningRule();
        mapDtoToRule(dto, rule);
        return ruleRepository.save(rule);
    }
    
    public Optional<ProvisioningRule> updateRule(Long id, ProvisioningRuleDto dto) {
        return ruleRepository.findById(id).map(rule -> {
            mapDtoToRule(dto, rule);
            return ruleRepository.save(rule);
        });
    }
    
    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }
    
    public void setRuleEnabled(Long id, boolean enabled) {
        ruleRepository.findById(id).ifPresent(rule -> {
            rule.setEnabled(enabled);
            ruleRepository.save(rule);
        });
    }
    
    private void mapDtoToRule(ProvisioningRuleDto dto, ProvisioningRule rule) {
        rule.setName(dto.getName());
        rule.setDescription(dto.getDescription());
        rule.setEnabled(dto.isEnabled());
        rule.setOrder(dto.getOrder());
        rule.setSourceType(dto.getSourceType());
        rule.setConditionType(dto.getConditionType());
        rule.setConditionAttribute(dto.getConditionAttribute());
        rule.setConditionOperator(dto.getConditionOperator());
        rule.setConditionValue(dto.getConditionValue());
        rule.setAssignedRoles(dto.getAssignedRoles());
        rule.setAssignedGroups(dto.getAssignedGroups());
    }
    
    public ProvisioningStatsDto getProvisioningStats(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findByActive(true).size();
        long autoProvisionedUsers = userRepository.findByAutoProvisioned(true).size();
        
        // Users by source
        Map<String, Long> usersBySource = Arrays.stream(ProvisionedUser.ProvisionSource.values())
            .collect(Collectors.toMap(
                Enum::name,
                source -> (long) userRepository.findByProvisionSource(source).size()
            ));
        
        // Users by role
        Map<String, Long> usersByRole = new HashMap<>();
        List<ProvisionedUser> allUsers = userRepository.findAll();
        for (ProvisionedUser user : allUsers) {
            for (String role : user.getRoles()) {
                usersByRole.merge(role, 1L, Long::sum);
            }
        }
        
        // Provisioning rules stats
        long totalRules = ruleRepository.count();
        long activeRules = ruleRepository.findByEnabledTrueOrderByOrderAsc().size();
        
        Map<String, Long> rulesBySource = Arrays.stream(ProvisionedUser.ProvisionSource.values())
            .collect(Collectors.toMap(
                Enum::name,
                source -> (long) ruleRepository.findBySourceTypeAndEnabledTrueOrderByOrderAsc(source).size()
            ));
        
        return ProvisioningStatsDto.builder()
            .totalUsers(totalUsers)
            .activeUsers(activeUsers)
            .inactiveUsers(totalUsers - activeUsers)
            .autoProvisionedUsers(autoProvisionedUsers)
            .manuallyCreatedUsers(totalUsers - autoProvisionedUsers)
            .usersBySource(usersBySource)
            .usersByRole(usersByRole)
            .totalProvisioningRules(totalRules)
            .activeRules(activeRules)
            .rulesBySource(rulesBySource)
            .reportGeneratedAt(LocalDateTime.now())
            .periodStart(startDate)
            .periodEnd(endDate)
            .build();
    }
    
    public Map<String, Object> testRule(ProvisioningRuleDto ruleDto, Map<String, Object> sampleAttributes) {
        Map<String, Object> result = new HashMap<>();
        
        ProvisioningRule rule = new ProvisioningRule();
        mapDtoToRule(ruleDto, rule);
        
        // Test rule evaluation logic
        boolean matches = false;
        String evaluationDetails = "";
        
        try {
            // This would use the same logic as JitProvisioningService.evaluateRule
            // For now, returning a simple response
            matches = true;
            evaluationDetails = "Rule would match the provided attributes";
            
            result.put("matches", matches);
            result.put("evaluationDetails", evaluationDetails);
            result.put("wouldAssignRoles", rule.getAssignedRoles());
            result.put("wouldAssignGroups", rule.getAssignedGroups());
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("matches", false);
        }
        
        return result;
    }
}