package com.hr.employee_management.Service.impl;

import com.hr.employee_management.Dto.auth.AuthRequest;
import com.hr.employee_management.Dto.auth.AuthResponse;
import com.hr.employee_management.Dto.auth.ChangePasswordRequest;
import com.hr.employee_management.Dto.user.UserResponse;
import com.hr.employee_management.Entity.User;
import com.hr.employee_management.Exception.BusinessRuleException;
import com.hr.employee_management.Exception.ResourceNotFoundException;
import com.hr.employee_management.Mapper.UserMapper;
import com.hr.employee_management.Repository.UserRepository;
import com.hr.employee_management.Security.JwtService;
import com.hr.employee_management.Security.UserPrincipal;
import com.hr.employee_management.Service.AuthService;
import com.hr.employee_management.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail()).orElseThrow(() ->
        new BadCredentialsException("Invalid credentials"));
        if (!user.isActive()) {
            log.warn("Login attempt for deactivated user: {}", request.getUsernameOrEmail());
            throw new DisabledException("User account is inactive or disable");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        request.getPassword()
                )
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", principal.getId());
        extraClaims.put("role", principal.getRole());
        extraClaims.put("email", principal.getEmail());

        String jwt = jwtService.generateToken(principal, extraClaims);
        log.info("User '{}' with role '{}' successfully authenticated", principal.getUsername(), principal.getRole());
        return AuthResponse.builder()
                .token(jwt)
                .tokenType("Bearer")
                .expiresInMs(jwtService.getJwtExpirationMs())
                .userId(principal.getId())
                .userName(principal.getUsername())
                .email(principal.getEmail())
                .role(principal.getRole())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(){
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new ResourceNotFoundException("No authenticated user session found");
        }
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User", "id", userId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessRuleException("New password and confirm password do not match");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new ResourceNotFoundException("No authenticated user found to update password");
        }

        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User", "id", userId));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessRuleException("Current password does not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessRuleException("New password cannot be the same as the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password successfully changed for user '{}'", user.getUsername());
    }

    @Override
    public void logout(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer")) {
            String token = authHeader.substring(7);
            jwtService.revokeToken(token);
            log.info("Token successfully revoked on logout");
        }
    }
}
