package com.devang.authentication.controller;

import com.devang.authentication.dto.request.CreateClientAppRequest;
import com.devang.authentication.dto.request.CreateOrganizationRequest;
import com.devang.authentication.dto.response.ApiResponse;
import com.devang.authentication.dto.response.ClientAppResponse;
import com.devang.authentication.dto.response.OrganizationResponse;
import com.devang.authentication.security.ApiKeyAuthenticationToken;
import com.devang.authentication.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organization")
@PreAuthorize("hasRole('ORG_OWNER')")
public class OrganizationController {
    
    @Autowired
    private OrganizationService organizationService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {
        try {
            OrganizationResponse response = organizationService.createOrganization(request);
            return ResponseEntity.ok(ApiResponse.success("Organization created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create organization", e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getAllOrganizations() {
        try {
            List<OrganizationResponse> organizations = organizationService.getAllOrganizations();
            return ResponseEntity.ok(ApiResponse.success(organizations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch organizations", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(@PathVariable UUID id) {
        try {
            OrganizationResponse response = organizationService.getOrganizationById(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Organization not found", e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getMyOrganization() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication instanceof ApiKeyAuthenticationToken) {
                ApiKeyAuthenticationToken apiKeyAuth = (ApiKeyAuthenticationToken) authentication;
                String apiKey = apiKeyAuth.getApiKey();
                
                OrganizationResponse response = organizationService.getOrganizationByApiKey(apiKey);
                return ResponseEntity.ok(ApiResponse.success(response));
            }
            
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid authentication"));
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch organization", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable UUID id, 
            @Valid @RequestBody CreateOrganizationRequest request) {
        try {
            // Verify the organization belongs to the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof ApiKeyAuthenticationToken) {
                ApiKeyAuthenticationToken apiKeyAuth = (ApiKeyAuthenticationToken) authentication;
                String organizationId = apiKeyAuth.getOrganizationId();
                
                if (!id.toString().equals(organizationId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Access denied", "You can only update your own organization"));
                }
            }
            
            OrganizationResponse response = organizationService.updateOrganization(id, request);
            return ResponseEntity.ok(ApiResponse.success("Organization updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update organization", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteOrganization(@PathVariable UUID id) {
        try {
            // Verify the organization belongs to the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof ApiKeyAuthenticationToken) {
                ApiKeyAuthenticationToken apiKeyAuth = (ApiKeyAuthenticationToken) authentication;
                String organizationId = apiKeyAuth.getOrganizationId();
                
                if (!id.toString().equals(organizationId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Access denied", "You can only delete your own organization"));
                }
            }
            
            organizationService.deleteOrganization(id);
            return ResponseEntity.ok(ApiResponse.success("Organization deleted successfully", "Success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete organization", e.getMessage()));
        }
    }
    
    // Client App Management
    
    @PostMapping("/{orgId}/client-apps")
    public ResponseEntity<ApiResponse<ClientAppResponse>> createClientApp(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateClientAppRequest request) {
        try {
            // Verify the organization belongs to the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof ApiKeyAuthenticationToken) {
                ApiKeyAuthenticationToken apiKeyAuth = (ApiKeyAuthenticationToken) authentication;
                String organizationId = apiKeyAuth.getOrganizationId();
                
                if (!orgId.toString().equals(organizationId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Access denied", "You can only create client apps for your own organization"));
                }
            }
            
            ClientAppResponse response = organizationService.createClientApp(orgId, request);
            return ResponseEntity.ok(ApiResponse.success("Client app created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create client app", e.getMessage()));
        }
    }
    
    @GetMapping("/{orgId}/client-apps")
    public ResponseEntity<ApiResponse<List<ClientAppResponse>>> getClientApps(@PathVariable UUID orgId) {
        try {
            // Verify the organization belongs to the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof ApiKeyAuthenticationToken) {
                ApiKeyAuthenticationToken apiKeyAuth = (ApiKeyAuthenticationToken) authentication;
                String organizationId = apiKeyAuth.getOrganizationId();
                
                if (!orgId.toString().equals(organizationId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Access denied", "You can only view client apps for your own organization"));
                }
            }
            
            List<ClientAppResponse> clientApps = organizationService.getClientAppsByOrganization(orgId);
            return ResponseEntity.ok(ApiResponse.success(clientApps));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch client apps", e.getMessage()));
        }
    }
    
    @GetMapping("/client-apps/{id}")
    public ResponseEntity<ApiResponse<ClientAppResponse>> getClientAppById(@PathVariable UUID id) {
        try {
            ClientAppResponse response = organizationService.getClientAppById(id);
            
            // Verify the client app belongs to the authenticated user's organization
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof ApiKeyAuthenticationToken) {
                ApiKeyAuthenticationToken apiKeyAuth = (ApiKeyAuthenticationToken) authentication;
                String organizationId = apiKeyAuth.getOrganizationId();
                
                if (!response.getOrganizationId().toString().equals(organizationId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Access denied", "You can only view client apps from your own organization"));
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Client app not found", e.getMessage()));
        }
    }
    
    @PutMapping("/client-apps/{id}")
    public ResponseEntity<ApiResponse<ClientAppResponse>> updateClientApp(
            @PathVariable UUID id,
            @Valid @RequestBody CreateClientAppRequest request) {
        try {
            ClientAppResponse existingApp = organizationService.getClientAppById(id);
            
            // Verify the client app belongs to the authenticated user's organization
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof ApiKeyAuthenticationToken) {
                ApiKeyAuthenticationToken apiKeyAuth = (ApiKeyAuthenticationToken) authentication;
                String organizationId = apiKeyAuth.getOrganizationId();
                
                if (!existingApp.getOrganizationId().toString().equals(organizationId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Access denied", "You can only update client apps from your own organization"));
                }
            }
            
            ClientAppResponse response = organizationService.updateClientApp(id, request);
            return ResponseEntity.ok(ApiResponse.success("Client app updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update client app", e.getMessage()));
        }
    }
    
    @DeleteMapping("/client-apps/{id}")
    public ResponseEntity<ApiResponse<String>> deleteClientApp(@PathVariable UUID id) {
        try {
            ClientAppResponse existingApp = organizationService.getClientAppById(id);
            
            // Verify the client app belongs to the authenticated user's organization
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof ApiKeyAuthenticationToken) {
                ApiKeyAuthenticationToken apiKeyAuth = (ApiKeyAuthenticationToken) authentication;
                String organizationId = apiKeyAuth.getOrganizationId();
                
                if (!existingApp.getOrganizationId().toString().equals(organizationId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Access denied", "You can only delete client apps from your own organization"));
                }
            }
            
            organizationService.deleteClientApp(id);
            return ResponseEntity.ok(ApiResponse.success("Client app deleted successfully", "Success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete client app", e.getMessage()));
        }
    }
}