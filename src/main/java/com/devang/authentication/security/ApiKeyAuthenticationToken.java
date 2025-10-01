package com.devang.authentication.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
    
    private final String apiKey;
    private final String userType;
    private final String organizationId;
    private final String clientAppId;
    
    public ApiKeyAuthenticationToken(String apiKey, 
                                   String userType, 
                                   String organizationId, 
                                   String clientAppId,
                                   Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.apiKey = apiKey;
        this.userType = userType;
        this.organizationId = organizationId;
        this.clientAppId = clientAppId;
        setAuthenticated(true);
    }
    
    @Override
    public Object getCredentials() {
        return apiKey;
    }
    
    @Override
    public Object getPrincipal() {
        return apiKey;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public String getOrganizationId() {
        return organizationId;
    }
    
    public String getClientAppId() {
        return clientAppId;
    }
}