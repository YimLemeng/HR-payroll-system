package com.hr.employee_management.Controller;


import com.hr.employee_management.Dto.employee.EmployeeRequest;
import com.hr.employee_management.Dto.employee.EmployeeResponse;
import com.hr.employee_management.Dto.employee.EmployeeStatusUpdateRequest;
import com.hr.employee_management.Service.EmployeeService;
import com.hr.employee_management.enums.EmploymentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
@Tag(name = "Employee Management", description = "Endpoints for managing employee master records, status, and search")
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    @Operation(summary = "Create an employee record", description = "Creates a new employee record. Accessible by ADMIN and HR. Does not create a login account.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Duplicate employeeCode or email")
    })
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(request));
    }

    @GetMapping
    @Operation(summary = "List employees with pagination and filters", description = "Supports filtering by departmentId, employmentStatus, and pagination/sorting.")
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmploymentStatus status,
            @PageableDefault(size = 10, sort = "firstName") Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(departmentId, status, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search employees", description = "Search by keyword across employee code, name, email, or job title.")
    public ResponseEntity<Page<EmployeeResponse>> searchEmployees(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(employeeService.searchEmployees(keyword, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Retrieve single employee record by ID.")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee record", description = "Updates full details of an existing employee.")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update employee employment status", description = "Updates employment status (e.g. ACTIVE, INACTIVE, TERMINATED, ON_LEAVE).")
    public ResponseEntity<EmployeeResponse> updateEmployeeStatus(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeStatusUpdateRequest request) {
        return ResponseEntity.ok(employeeService.updateEmployeeStatus(id, request.getEmploymentStatus()));
    }
}
