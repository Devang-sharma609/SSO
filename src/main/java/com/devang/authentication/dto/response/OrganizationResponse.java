package com.devang.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    
    private UUID id;
    private String name;
    private String description;
    private String orgOwnerApiKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}