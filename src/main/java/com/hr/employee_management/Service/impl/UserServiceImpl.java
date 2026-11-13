package com.hr.employee_management.Service.impl;

import com.hr.employee_management.Dto.user.CreateHrUserRequest;
import com.hr.employee_management.Dto.user.UpdateUserRequest;
import com.hr.employee_management.Dto.user.UserResponse;
import com.hr.employee_management.Entity.User;
import com.hr.employee_management.Exception.BusinessRuleException;
import com.hr.employee_management.Exception.DuplicateResourceException;
import com.hr.employee_management.Exception.ResourceNotFoundException;
import com.hr.employee_management.Mapper.UserMapper;
import com.hr.employee_management.Repository.UserRepository;
import com.hr.employee_management.Service.UserService;
import com.hr.employee_management.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createHrUser(CreateHrUserRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessRuleException("Password and confirm password do not match");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered");
        }
        User hrUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.HR)
                .active(true)
                .build();

        User saved = userRepository.save(hrUser);
        log.info("New HR user account created: {}", saved.getUsername());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getAllUsers(Pageable pageable){
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(Long id, UpdateUserRequest request){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already in use by another user");
        }
        if (user.getRole() == Role.ADMIN && user.isActive()){
            boolean wouldDemoteOrDeactivate = (request.getRole() != Role.ADMIN || (!request.getActive()));
            if (wouldDemoteOrDeactivate){
                long activeAdminCount = userRepository.countByRoleAndActiveTrue(Role.ADMIN);
                if (activeAdminCount <= 1){
                    throw new BusinessRuleException("Cannot deactivate or demote the final active ADMIN account in the system");
                }
            }
        }
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setActive(request.getActive());

        User updatedUser = userRepository.save(user);
        log.info("User id {} updated by ADMIN", id);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUserStatus(Long id, Boolean active){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        if (!active && user.getRole() == Role.ADMIN && user.isActive()) {
            long activeAdminCount = userRepository.countByRoleAndActiveTrue(Role.ADMIN);
            if (activeAdminCount <= 1){
                throw new BusinessRuleException("Cannot deactivate the final active ADMIN account in the system");
            }
        }
        user.setActive(active);
        User updatedStatus = userRepository.save(user);
        log.info("User id {} active status changed to {}", id, active);
        return userMapper.toResponse(updatedStatus);
    }
}
