package com.devang.authentication.service;

import com.devang.authentication.dto.request.LoginRequest;
import com.devang.authentication.dto.request.SignupRequest;
import com.devang.authentication.dto.response.AuthResponse;
import com.devang.authentication.entity.*;
import com.devang.authentication.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrgOwnerRepository orgOwnerRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ClientAppRepository clientAppRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtilService jwtUtilService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse signup(SignupRequest request, String apiKey) {
        // If apiKey is null -> org owner signup
        if (apiKey == null) {
            if (orgOwnerRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already exists");
            }

            OrgOwner owner = new OrgOwner();
            owner.setUsername(request.getUsername());
            owner.setPassword(passwordEncoder.encode(request.getPassword()));
            owner.setEmail(request.getEmail());
            owner.setFirstName(request.getFirstName());
            owner.setLastName(request.getLastName());

            // create organization and attach. Allow user to supply organization name/description.
            String orgName = request.getOrganizationName();
            String orgDesc = request.getOrganizationDescription();

            if (orgName == null || orgName.isBlank()) {
                orgName = request.getUsername() + "-org";
            }

            // Ensure organization name is unique
            if (organizationRepository.existsByName(orgName)) {
                throw new RuntimeException("Organization name already exists");
            }

            if (orgDesc == null) {
                orgDesc = request.getUsername() + "'s organization";
            }

            Organization org = new Organization();
            org.setName(orgName);
            org.setDescription(orgDesc);
            org = organizationRepository.save(org);

            owner.setOrganization(org);
            owner = orgOwnerRepository.save(owner);

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", owner.getId());
            claims.put("username", owner.getUsername());
            claims.put("userType", "ORG_OWNER");
            claims.put("organizationId", org.getId());
            claims.put("organizationName", org.getName());

            String access = jwtUtilService.generateAccessToken(claims);
            String refresh = jwtUtilService.generateRefreshToken();

            RefreshToken rt = new RefreshToken();
            rt.setToken(refresh);
            rt.setUser(null);
            rt.setExpiryDate(LocalDateTime.now().plusSeconds(jwtUtilService.getRefreshExpirationSeconds()));
            refreshTokenRepository.save(rt);

            return AuthResponse.builder()
                    .accessToken(access)
                    .refreshToken(refresh)
                    .expiresIn(jwtUtilService.getAccessExpirationSeconds())
            .userClaims(claims)
            .orgOwnerApiKey(org.getOrgOwnerApiKey())
                    .build();
        }

        // If apiKey starts with app_ -> client app user signup
        if (apiKey.startsWith("app_")) {
            ClientApp clientApp = clientAppRepository.findByClientAppApiKey(apiKey)
                    .orElseThrow(() -> new RuntimeException("Invalid client app API key"));

            if (userRepository.existsByUsernameAndClientApp(request.getUsername(), clientApp)) {
                throw new RuntimeException("Username already exists for this client app");
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setUserMetadata(request.getUser_metadata());
            user.setOrganization(clientApp.getOrganization());
            user.setClientApp(clientApp);

            user = userRepository.save(user);

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("username", user.getUsername());
            claims.put("userType", "CLIENT_USER");
            claims.put("organizationId", clientApp.getOrganization().getId());
            claims.put("organizationName", clientApp.getOrganization().getName());
            claims.put("clientAppId", clientApp.getId());
            if (user.getUserMetadata() != null) {
                claims.put("user_metadata", user.getUserMetadata());
            }

            String access = jwtUtilService.generateAccessToken(claims);
            String refresh = jwtUtilService.generateRefreshToken();

            RefreshToken rt = new RefreshToken();
            rt.setToken(refresh);
            rt.setUser(user);
            rt.setExpiryDate(LocalDateTime.now().plusSeconds(jwtUtilService.getRefreshExpirationSeconds()));
            refreshTokenRepository.save(rt);

            return AuthResponse.builder()
                    .accessToken(access)
                    .refreshToken(refresh)
                    .expiresIn(jwtUtilService.getAccessExpirationSeconds())
            .userClaims(claims)
            .clientAppApiKey(clientApp.getClientAppApiKey())
                    .build();
        }

        throw new RuntimeException("Unsupported API key type for signup");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, String apiKey) {
        // org owner login (no API key) or client app user login
        if (apiKey == null) {
            OrgOwner owner = orgOwnerRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            if (!passwordEncoder.matches(request.getPassword(), owner.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", owner.getId());
            claims.put("username", owner.getUsername());
            claims.put("userType", "ORG_OWNER");

            Organization org = owner.getOrganization();
            if (org != null) {
                claims.put("organizationId", org.getId());
                claims.put("organizationName", org.getName());
            }

            String access = jwtUtilService.generateAccessToken(claims);
            String refresh = jwtUtilService.generateRefreshToken();

            RefreshToken rt = new RefreshToken();
            rt.setToken(refresh);
            rt.setUser(null);
            rt.setExpiryDate(LocalDateTime.now().plusSeconds(jwtUtilService.getRefreshExpirationSeconds()));
            refreshTokenRepository.save(rt);

            return AuthResponse.builder()
                    .accessToken(access)
                    .refreshToken(refresh)
                    .expiresIn(jwtUtilService.getAccessExpirationSeconds())
                .userClaims(claims)
                .orgOwnerApiKey(org.getOrgOwnerApiKey())
                    .build();
        }

        if (apiKey.startsWith("app_")) {
            ClientApp clientApp = clientAppRepository.findByClientAppApiKey(apiKey)
                    .orElseThrow(() -> new RuntimeException("Invalid client app API key"));

            User user = userRepository.findByUsernameAndClientApp(request.getUsername(), clientApp)
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("username", user.getUsername());
            claims.put("userType", "CLIENT_USER");
            claims.put("organizationId", clientApp.getOrganization().getId());
            claims.put("organizationName", clientApp.getOrganization().getName());
            claims.put("clientAppId", clientApp.getId());
            if (user.getUserMetadata() != null) {
                claims.put("user_metadata", user.getUserMetadata());
            }

            String access = jwtUtilService.generateAccessToken(claims);
            String refresh = jwtUtilService.generateRefreshToken();

            RefreshToken rt = new RefreshToken();
            rt.setToken(refresh);
            rt.setUser(user);
            rt.setExpiryDate(LocalDateTime.now().plusSeconds(jwtUtilService.getRefreshExpirationSeconds()));
            refreshTokenRepository.save(rt);

            return AuthResponse.builder()
                    .accessToken(access)
                    .refreshToken(refresh)
                    .expiresIn(jwtUtilService.getAccessExpirationSeconds())
                .userClaims(claims)
                .clientAppApiKey(clientApp.getClientAppApiKey())
                    .build();
        }

        throw new RuntimeException("Unsupported API key type for login");
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken rt = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (rt.isExpired() || rt.isRevoked()) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        // Build claims from user or owner
        Map<String, Object> claims = new HashMap<>();
        if (rt.getUser() != null) {
            User user = rt.getUser();
            claims.put("userId", user.getId());
            claims.put("username", user.getUsername());
            claims.put("userType", "CLIENT_USER");
            claims.put("organizationId", user.getOrganization().getId());
            claims.put("organizationName", user.getOrganization().getName());
            claims.put("clientAppId", user.getClientApp().getId());
            if (user.getUserMetadata() != null) {
                claims.put("user_metadata", user.getUserMetadata());
            }
        }

        String newAccess = jwtUtilService.generateAccessToken(claims);
        String newRefresh = jwtUtilService.generateRefreshToken();

        // Revoke old and save new
        refreshTokenRepository.revokeToken(refreshToken);

        RefreshToken newRt = new RefreshToken();
        newRt.setToken(newRefresh);
        newRt.setUser(rt.getUser());
        newRt.setExpiryDate(LocalDateTime.now().plusSeconds(jwtUtilService.getRefreshExpirationSeconds()));
        refreshTokenRepository.save(newRt);

        return AuthResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .expiresIn(jwtUtilService.getAccessExpirationSeconds())
                .userClaims(claims)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.revokeToken(refreshToken);
    }

}

