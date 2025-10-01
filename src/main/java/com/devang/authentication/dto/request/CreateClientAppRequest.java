package com.devang.authentication.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateClientAppRequest {
    
    @NotBlank(message = "Client app name is required")
    private String name;
    
    private String description;
}