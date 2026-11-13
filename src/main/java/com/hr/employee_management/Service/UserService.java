package com.hr.employee_management.Service;

import com.hr.employee_management.Dto.user.CreateHrUserRequest;
import com.hr.employee_management.Dto.user.UpdateUserRequest;
import com.hr.employee_management.Dto.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserService {
    UserResponse createHrUser(CreateHrUserRequest request);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UpdateUserRequest request);
    UserResponse updateUserStatus(Long id, Boolean active);
}
