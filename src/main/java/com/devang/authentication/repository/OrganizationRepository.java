package com.devang.authentication.repository;

import com.devang.authentication.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    Optional<Organization> findByName(String name);
    
    Optional<Organization> findByOrgOwnerApiKey(String orgOwnerApiKey);
    
    boolean existsByName(String name);
    
    boolean existsByOrgOwnerApiKey(String orgOwnerApiKey);
}