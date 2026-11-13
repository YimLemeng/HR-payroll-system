package com.hr.employee_management.Controller;

import com.hr.employee_management.Dto.payroll.PayrollResponse;
import com.hr.employee_management.Dto.payroll.PayrollSummaryResponse;
import com.hr.employee_management.Service.PayrollService;
import com.hr.employee_management.enums.PayrollStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
@Tag(name = "Payroll Management", description = "Endpoints for monthly payroll calculations, approvals, disbursement tracking, and summaries")
public class PayrollController {
    private final PayrollService payrollService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate monthly payroll", description = "Calculates or updates payroll records for eligible active employees for the specified month and year, optionally filtered by department.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payroll calculation executed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid month or year parameters")
    })
    public ResponseEntity<List<PayrollResponse>> calculatePayroll(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(payrollService.calculateMonthlyPayroll(month, year, departmentId));
    }

    @GetMapping
    @Operation(summary = "List payroll records", description = "Retrieve paginated payroll records, optionally filtered by month, year, and status.")
    public ResponseEntity<Page<PayrollResponse>> getPayrolls(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) PayrollStatus status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(payrollService.getPayrolls(month, year, status, pageable));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get payroll summary for a period", description = "Returns financial totals (gross, net, tax, deductions, overtime) and status breakdowns.")
    public ResponseEntity<PayrollSummaryResponse> getPayrollSummary(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getPayrollSummary(month, year));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payroll record by ID", description = "Retrieve a single payroll record by ID.")
    public ResponseEntity<PayrollResponse> getPayrollById(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.getPayrollById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get payroll records by employee ID", description = "Retrieve payroll history for a specific employee.")
    public ResponseEntity<Page<PayrollResponse>> getPayrollsByEmployee(
            @PathVariable Long employeeId,
            @PageableDefault(size = 10, sort = "payrollYear") Pageable pageable) {
        return ResponseEntity.ok(payrollService.getPayrollsByEmployee(employeeId, pageable));
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve payroll record", description = "Transitions payroll status from CALCULATED to APPROVED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payroll approved"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition or already paid")
    })
    public ResponseEntity<PayrollResponse> approvePayroll(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.approvePayroll(id));
    }

    @PatchMapping("/{id}/mark-paid")
    @Operation(summary = "Mark payroll as paid", description = "Transitions payroll status from APPROVED to PAID, stamping payment timestamp.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payroll marked as paid"),
            @ApiResponse(responseCode = "400", description = "Payroll not in APPROVED state")
    })
    public ResponseEntity<PayrollResponse> markPayrollPaid(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.markPayrollPaid(id));
    }
}
