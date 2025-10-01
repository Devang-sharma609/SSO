package com.devang.authentication.repository;

import com.devang.authentication.entity.OrgOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrgOwnerRepository extends JpaRepository<OrgOwner, UUID> {
    
    Optional<OrgOwner> findByUsername(String username);
    
    Optional<OrgOwner> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}