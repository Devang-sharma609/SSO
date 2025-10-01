package com.devang.authentication.service;

import com.devang.authentication.dto.request.CreateClientAppRequest;
import com.devang.authentication.dto.request.CreateOrganizationRequest;
import com.devang.authentication.dto.response.ClientAppResponse;
import com.devang.authentication.dto.response.OrganizationResponse;
import com.devang.authentication.entity.ClientApp;
import com.devang.authentication.entity.Organization;
import com.devang.authentication.repository.ClientAppRepository;
import com.devang.authentication.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationService {
    
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ClientAppRepository clientAppRepository;
    
    
    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        if (organizationRepository.existsByName(request.getName())) {
            throw new RuntimeException("Organization name already exists");
        }
        
        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        
        organization = organizationRepository.save(organization);
        
        return mapToResponse(organization);
    }
    
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(UUID id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        return mapToResponse(organization);
    }
    
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationByApiKey(String apiKey) {
        Organization organization = organizationRepository.findByOrgOwnerApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        return mapToResponse(organization);
    }
    
    @Transactional
    public OrganizationResponse updateOrganization(UUID id, CreateOrganizationRequest request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        // Check if name is already taken by another organization
        if (!organization.getName().equals(request.getName()) && 
            organizationRepository.existsByName(request.getName())) {
            throw new RuntimeException("Organization name already exists");
        }
        
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());
        
        organization = organizationRepository.save(organization);
        
        return mapToResponse(organization);
    }
    
    @Transactional
    public void deleteOrganization(UUID id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        organizationRepository.delete(organization);
    }
    
    @Transactional
    public ClientAppResponse createClientApp(UUID organizationId, CreateClientAppRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        if (clientAppRepository.existsByNameAndOrganization(request.getName(), organization)) {
            throw new RuntimeException("Client app name already exists in this organization");
        }
        
        ClientApp clientApp = new ClientApp();
        clientApp.setName(request.getName());
        clientApp.setDescription(request.getDescription());
        clientApp.setOrganization(organization);
        
        clientApp = clientAppRepository.save(clientApp);
        
        return mapToClientAppResponse(clientApp);
    }
    
    @Transactional(readOnly = true)
    public List<ClientAppResponse> getClientAppsByOrganization(UUID organizationId) {
        return clientAppRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::mapToClientAppResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ClientAppResponse getClientAppById(UUID id) {
        ClientApp clientApp = clientAppRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client app not found"));
        
        return mapToClientAppResponse(clientApp);
    }
    
    @Transactional(readOnly = true)
    public ClientAppResponse getClientAppByApiKey(String apiKey) {
        ClientApp clientApp = clientAppRepository.findByClientAppApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("Client app not found"));
        
        return mapToClientAppResponse(clientApp);
    }
    
    @Transactional
    public ClientAppResponse updateClientApp(UUID id, CreateClientAppRequest request) {
        ClientApp clientApp = clientAppRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client app not found"));
        
        // Check if name is already taken by another client app in the same organization
        if (!clientApp.getName().equals(request.getName()) && 
            clientAppRepository.existsByNameAndOrganization(request.getName(), clientApp.getOrganization())) {
            throw new RuntimeException("Client app name already exists in this organization");
        }
        
        clientApp.setName(request.getName());
        clientApp.setDescription(request.getDescription());
        
        clientApp = clientAppRepository.save(clientApp);
        
        return mapToClientAppResponse(clientApp);
    }
    
    @Transactional
    public void deleteClientApp(UUID id) {
        ClientApp clientApp = clientAppRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client app not found"));
        
        clientAppRepository.delete(clientApp);
    }
    
    private OrganizationResponse mapToResponse(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .orgOwnerApiKey(organization.getOrgOwnerApiKey())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .build();
    }
    
    private ClientAppResponse mapToClientAppResponse(ClientApp clientApp) {
        return ClientAppResponse.builder()
                .id(clientApp.getId())
                .name(clientApp.getName())
                .description(clientApp.getDescription())
                .clientAppApiKey(clientApp.getClientAppApiKey())
                .organizationId(clientApp.getOrganization().getId())
                .organizationName(clientApp.getOrganization().getName())
                .createdAt(clientApp.getCreatedAt())
                .updatedAt(clientApp.getUpdatedAt())
                .build();
    }
}