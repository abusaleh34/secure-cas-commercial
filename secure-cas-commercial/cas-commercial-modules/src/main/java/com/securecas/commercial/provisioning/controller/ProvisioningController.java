package com.securecas.commercial.provisioning.controller;

import com.securecas.commercial.provisioning.dto.ProvisioningRuleDto;
import com.securecas.commercial.provisioning.dto.ProvisioningStatsDto;
import com.securecas.commercial.provisioning.model.ProvisionedUser;
import com.securecas.commercial.provisioning.model.ProvisioningRule;
import com.securecas.commercial.provisioning.service.JitProvisioningService;
import com.securecas.commercial.provisioning.service.ProvisioningRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/provisioning")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "JIT Provisioning", description = "Just-In-Time user provisioning management")
@SecurityRequirement(name = "bearerAuth")
public class ProvisioningController {
    
    private final JitProvisioningService jitProvisioningService;
    private final ProvisioningRuleService provisioningRuleService;
    
    @GetMapping("/users")
    @Operation(summary = "Get provisioned users", description = "Retrieve a paginated list of JIT provisioned users")
    public Page<ProvisionedUser> getProvisionedUsers(
            @RequestParam(required = false) ProvisionedUser.ProvisionSource source,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return provisioningRuleService.searchProvisionedUsers(source, active, search, pageable);
    }
    
    @GetMapping("/users/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve a specific provisioned user")
    public ResponseEntity<ProvisionedUser> getUser(@PathVariable String username) {
        return provisioningRuleService.findUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/users/{username}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivate a JIT provisioned user")
    public ResponseEntity<Void> deactivateUser(@PathVariable String username) {
        jitProvisioningService.deactivateUser(username);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/users/{username}/activate")
    @Operation(summary = "Activate user", description = "Reactivate a deactivated user")
    public ResponseEntity<Void> activateUser(@PathVariable String username) {
        provisioningRuleService.activateUser(username);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/rules")
    @Operation(summary = "Get provisioning rules", description = "Retrieve all provisioning rules")
    public List<ProvisioningRule> getProvisioningRules(
            @RequestParam(required = false) ProvisionedUser.ProvisionSource source,
            @RequestParam(required = false) Boolean enabled) {
        return provisioningRuleService.getProvisioningRules(source, enabled);
    }
    
    @GetMapping("/rules/{id}")
    @Operation(summary = "Get rule by ID", description = "Retrieve a specific provisioning rule")
    public ResponseEntity<ProvisioningRule> getRule(@PathVariable Long id) {
        return provisioningRuleService.findRuleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/rules")
    @Operation(summary = "Create provisioning rule", description = "Create a new provisioning rule")
    public ResponseEntity<ProvisioningRule> createRule(@Valid @RequestBody ProvisioningRuleDto ruleDto) {
        ProvisioningRule rule = provisioningRuleService.createRule(ruleDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(rule);
    }
    
    @PutMapping("/rules/{id}")
    @Operation(summary = "Update provisioning rule", description = "Update an existing provisioning rule")
    public ResponseEntity<ProvisioningRule> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody ProvisioningRuleDto ruleDto) {
        return provisioningRuleService.updateRule(id, ruleDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/rules/{id}")
    @Operation(summary = "Delete provisioning rule", description = "Delete a provisioning rule")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        provisioningRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/rules/{id}/enable")
    @Operation(summary = "Enable rule", description = "Enable a provisioning rule")
    public ResponseEntity<Void> enableRule(@PathVariable Long id) {
        provisioningRuleService.setRuleEnabled(id, true);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/rules/{id}/disable")
    @Operation(summary = "Disable rule", description = "Disable a provisioning rule")
    public ResponseEntity<Void> disableRule(@PathVariable Long id) {
        provisioningRuleService.setRuleEnabled(id, false);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get provisioning statistics", description = "Retrieve JIT provisioning statistics")
    public ProvisioningStatsDto getProvisioningStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return provisioningRuleService.getProvisioningStats(startDate, endDate);
    }
    
    @PostMapping("/test-rule")
    @Operation(summary = "Test provisioning rule", description = "Test a provisioning rule against sample attributes")
    public ResponseEntity<Map<String, Object>> testRule(
            @RequestBody ProvisioningRuleDto ruleDto,
            @RequestBody Map<String, Object> sampleAttributes) {
        Map<String, Object> result = provisioningRuleService.testRule(ruleDto, sampleAttributes);
        return ResponseEntity.ok(result);
    }
}