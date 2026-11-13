package com.hr.employee_management.Controller;

import com.hr.employee_management.Dto.auth.AuthRequest;
import com.hr.employee_management.Dto.auth.AuthResponse;
import com.hr.employee_management.Dto.auth.ChangePasswordRequest;
import com.hr.employee_management.Dto.user.UserResponse;
import com.hr.employee_management.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for login, current user profile, password change, and logout")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and obtain JWT token", description = "Public login endpoint using username or email along with password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive account")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile", description = "Returns authenticated user id, username, email, and role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    public ResponseEntity<UserResponse> getCurrentUser(){
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change current user's password", description = "Allows the authenticated user (ADMIN or HR) to update their own password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password or mismatch"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request){
        authService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate token", description = "Revokes the active JWT on server denylist and instructs client to purge token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader){
        authService.logout(authHeader);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully. Token has been invalidated."));
    }
}
