package com.hr.employee_management.Dto.auth;

import com.hr.employee_management.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private long expiresInMs;
    private Long userId;
    private String userName;
    private String email;
    private Role role;
}
