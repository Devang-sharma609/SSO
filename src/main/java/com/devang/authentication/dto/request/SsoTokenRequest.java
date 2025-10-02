package com.devang.authentication.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SsoTokenRequest {
    
    @NotBlank(message = "Current access token is required")
    private String currentAccessToken;
    
    @NotBlank(message = "Target client app API key is required")
    private String targetClientAppApiKey;
}
