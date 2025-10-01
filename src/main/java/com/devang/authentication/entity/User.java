package com.devang.authentication.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column
    private String email;
    
    @Column
    private String firstName;
    
    @Column
    private String lastName;
    
    @Column(name = "user_metadata", columnDefinition = "TEXT")
    private String userMetadataJson;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_app_id", nullable = false)
    private ClientApp clientApp;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RefreshToken> refreshTokens;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // JSON helper methods for data field
    @Transient
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> getUserMetadata() {
        if (userMetadataJson == null || userMetadataJson.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(userMetadataJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
    
    public void setUserMetadata(Map<String, Object> userMetadata) {
        if (userMetadata == null) {
            this.userMetadataJson = null;
        } else {
            try {
                this.userMetadataJson = objectMapper.writeValueAsString(userMetadata);
            } catch (JsonProcessingException e) {
                this.userMetadataJson = null;
            }
        }
    }
}