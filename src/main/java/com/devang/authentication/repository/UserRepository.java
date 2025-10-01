package com.devang.authentication.repository;

import com.devang.authentication.entity.ClientApp;
import com.devang.authentication.entity.Organization;
import com.devang.authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameAndClientApp(String username, ClientApp clientApp);
    
    List<User> findByOrganization(Organization organization);
    
    List<User> findByClientApp(ClientApp clientApp);
    
    List<User> findByOrganizationId(UUID organizationId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsernameAndClientApp(String username, ClientApp clientApp);
}