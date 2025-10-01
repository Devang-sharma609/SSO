package com.devang.authentication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "client_apps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientApp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Column(unique = true, nullable = false)
    private String clientAppApiKey;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @OneToMany(mappedBy = "clientApp", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    private void generateApiKey() {
        if (this.clientAppApiKey == null) {
            this.clientAppApiKey = "app_" + UUID.randomUUID().toString().replace("-", "");
        }
    }
}