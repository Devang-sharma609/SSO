package com.devang.authentication.controller;

import com.devang.authentication.dto.request.LoginRequest;
import com.devang.authentication.dto.request.RefreshTokenRequest;
import com.devang.authentication.dto.request.SignupRequest;
import com.devang.authentication.dto.request.SsoTokenRequest;
import com.devang.authentication.dto.response.ApiResponse;
import com.devang.authentication.dto.response.AuthResponse;
import com.devang.authentication.security.ApiKeyAuthenticationToken;
import com.devang.authentication.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String apiKey = null;
            if (authentication instanceof ApiKeyAuthenticationToken) {
                apiKey = ((ApiKeyAuthenticationToken) authentication).getApiKey();
            }

            AuthResponse response = authService.signup(request, apiKey);
            return ResponseEntity.ok(ApiResponse.success("Signup successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Signup failed", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String apiKey = null;
            if (authentication instanceof ApiKeyAuthenticationToken) {
                apiKey = ((ApiKeyAuthenticationToken) authentication).getApiKey();
            }

            AuthResponse response = authService.login(request, apiKey);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Login failed", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refresh(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Refresh failed", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            authService.logout(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Logged out", "Success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Logout failed", e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Object>> validate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid authentication"));
        }
        return ResponseEntity.ok(ApiResponse.success(authentication));
    }

    @PostMapping("/sso-exchange")
    public ResponseEntity<ApiResponse<AuthResponse>> ssoExchange(@Valid @RequestBody SsoTokenRequest request) {
        try {
            AuthResponse response = authService.exchangeTokenForClientApp(
                    request.getCurrentAccessToken(),
                    request.getTargetClientAppApiKey()
            );
            return ResponseEntity.ok(ApiResponse.success("SSO token exchange successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("SSO token exchange failed", e.getMessage()));
        }
    }
}

