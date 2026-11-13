package com.hr.employee_management.Controller;

import com.hr.employee_management.Dto.leave.LeaveRequestDto;
import com.hr.employee_management.Dto.leave.LeaveResponse;
import com.hr.employee_management.Dto.leave.LeaveReviewRequest;
import com.hr.employee_management.Service.LeaveRequestService;
import com.hr.employee_management.enums.LeaveStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
@Tag(name = "Leave Management", description = "Endpoints for leave requests submission, reviews, approvals, rejections, and cancellations")
public class LeaveRequestController {
    private final LeaveRequestService leaveRequestService;

    @PostMapping
    @Operation(summary = "Submit a leave request", description = "Submits a leave request for an employee. End date cannot precede start date.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Leave request created"),
            @ApiResponse(responseCode = "400", description = "Validation failed or overlapping approved leave")
    })
    public ResponseEntity<LeaveResponse> submitLeaveRequest(@Valid @RequestBody LeaveRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.submitLeaveRequest(request));
    }

    @GetMapping
    @Operation(summary = "List all leave requests", description = "Paginated listing of leaves with optional status filter (e.g. PENDING, APPROVED).")
    public ResponseEntity<Page<LeaveResponse>> getAllLeaves(
            @RequestParam(required = false) LeaveStatus status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(leaveRequestService.getAllLeaves(status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leave request by ID", description = "Retrieve a specific leave request.")
    public ResponseEntity<LeaveResponse> getLeaveById(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.getLeaveById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get leave requests by employee ID", description = "Paginated list of leave requests for an employee.")
    public ResponseEntity<Page<LeaveResponse>> getLeavesByEmployee(
            @PathVariable Long employeeId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(leaveRequestService.getLeavesByEmployee(employeeId, pageable));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve a leave request", description = "Approves a pending leave request and records the reviewer information.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave approved"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition or overlapping approved leave")
    })
    public ResponseEntity<LeaveResponse> approveLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveReviewRequest reviewRequest) {
        return ResponseEntity.ok(leaveRequestService.approveLeave(id, reviewRequest));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject a leave request", description = "Rejects a pending leave request with optional review comments.")
    public ResponseEntity<LeaveResponse> rejectLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveReviewRequest reviewRequest) {
        return ResponseEntity.ok(leaveRequestService.rejectLeave(id, reviewRequest));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a leave request", description = "Cancels a pending or approved leave request.")
    public ResponseEntity<LeaveResponse> cancelLeave(@PathVariable Long id) {
        return ResponseEntity.ok(leaveRequestService.cancelLeave(id));
    }
}
