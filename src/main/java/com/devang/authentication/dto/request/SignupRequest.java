package com.devang.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Map;

@Data
public class SignupRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @Email(message = "Valid email is required")
    private String email;
    
    private String firstName;
    
    private String lastName;
    
    // Optional organization fields supplied at signup
    private String organizationName;

    private String organizationDescription;
    
    // Additional user metadata (for client-app users)
    private Map<String, Object> user_metadata;
}