package com.devang.authentication.repository;

import com.devang.authentication.entity.ClientApp;
import com.devang.authentication.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientAppRepository extends JpaRepository<ClientApp, UUID> {
    
    Optional<ClientApp> findByClientAppApiKey(String clientAppApiKey);
    
    List<ClientApp> findByOrganization(Organization organization);
    
    List<ClientApp> findByOrganizationId(UUID organizationId);
    
    Optional<ClientApp> findByNameAndOrganization(String name, Organization organization);
    
    boolean existsByNameAndOrganization(String name, Organization organization);
    
    boolean existsByClientAppApiKey(String clientAppApiKey);
}