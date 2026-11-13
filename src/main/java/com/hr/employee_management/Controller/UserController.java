package com.hr.employee_management.Controller;

import com.hr.employee_management.Dto.user.CreateHrUserRequest;
import com.hr.employee_management.Dto.user.UpdateUserRequest;
import com.hr.employee_management.Dto.user.UserResponse;
import com.hr.employee_management.Dto.user.UserStatusUpdateRequest;
import com.hr.employee_management.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Administration", description = "ADMIN-only management of system accounts (creating HR users, updates, status toggles)")
public class UserController {
    private final UserService userService;

    @PostMapping("/hr")
    @Operation(summary = "Create an HR user account", description = "Only ADMIN can create HR user accounts. Regular public registration is disabled.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "HR account created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
            @ApiResponse(responseCode = "409", description = "Conflict - Username or email already exists")
    })
    public ResponseEntity<UserResponse> createHrUser(@Valid @RequestBody CreateHrUserRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createHrUser(request));
    }

    @GetMapping
    @Operation(summary = "List all user accounts", description = "ADMIN-only paginated listing of system users.")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@ParameterObject @PageableDefault(size = 10, sort = "id")Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user account by ID", description = "ADMIN-only endpoint to view user details.")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user account details", description = "ADMIN-only endpoint with guard against deactivating or demoting the last active ADMIN.")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request){
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Toggle user active status", description = "ADMIN-only endpoint to activate or deactivate a user account.")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id, @Valid @RequestBody UserStatusUpdateRequest request){
        return ResponseEntity.ok(userService.updateUserStatus(id, request.getActive()));
    }
}
