package com.hr.employee_management.Service;

import com.hr.employee_management.Dto.auth.AuthRequest;
import com.hr.employee_management.Dto.auth.AuthResponse;
import com.hr.employee_management.Dto.auth.ChangePasswordRequest;
import com.hr.employee_management.Dto.user.UserResponse;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    UserResponse getCurrentUser();
    void changePassword(ChangePasswordRequest request);
    void logout(String authHeader);
}
