package com.devang.authentication.security;

import com.devang.authentication.repository.ClientAppRepository;
import com.devang.authentication.repository.OrganizationRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final OrganizationRepository organizationRepository;
    private final ClientAppRepository clientAppRepository;

    public ApiKeyAuthenticationFilter(
            OrganizationRepository organizationRepository,
            ClientAppRepository clientAppRepository) {
        this.organizationRepository = organizationRepository;
        this.clientAppRepository = clientAppRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String apiKey = request.getHeader("apikey");
        String requestURI = request.getRequestURI();
        
        // Skip authentication for auth endpoints
        if (requestURI.startsWith("/api/auth/")) {
            // Set authentication context based on API key presence
            if (apiKey != null && !apiKey.isEmpty()) {
                setAuthenticationContext(apiKey, request);
            } else {
                // No API key means potential org owner signup/login
                setOrgOwnerContext(request);
            }
        } else {
            // For other endpoints, API key is required
            if (apiKey == null || apiKey.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"API key is required\"}");
                return;
            }
            
            if (!setAuthenticationContext(apiKey, request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Invalid API key\"}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean setAuthenticationContext(String apiKey, HttpServletRequest request) {
        try {
            // Check if it's an organization owner API key
            if (apiKey.startsWith("org_")) {
                return organizationRepository.findByOrgOwnerApiKey(apiKey)
                        .map(org -> {
                            ApiKeyAuthenticationToken authentication = 
                                new ApiKeyAuthenticationToken(
                                    apiKey, 
                                    "ORG_OWNER", 
                                    org.getId().toString(),
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ORG_OWNER"))
                                );
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            return true;
                        })
                        .orElse(false);
            }
            
            // Check if it's a client app API key
            if (apiKey.startsWith("app_")) {
                return clientAppRepository.findByClientAppApiKey(apiKey)
                        .map(clientApp -> {
                            ApiKeyAuthenticationToken authentication = 
                                new ApiKeyAuthenticationToken(
                                    apiKey, 
                                    "CLIENT_APP", 
                                    clientApp.getOrganization().getId().toString(),
                                    clientApp.getId().toString(),
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT_APP"))
                                );
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            return true;
                        })
                        .orElse(false);
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void setOrgOwnerContext(HttpServletRequest request) {
        // Set a special authentication token for potential org owner operations
        ApiKeyAuthenticationToken authentication = 
            new ApiKeyAuthenticationToken(
                null, 
                "POTENTIAL_ORG_OWNER", 
                null,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_POTENTIAL_ORG_OWNER"))
            );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}