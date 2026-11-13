package com.hr.employee_management.Controller;

import com.hr.employee_management.Dto.attendance.AttendanceSummaryResponse;
import com.hr.employee_management.Dto.payroll.PayrollSummaryResponse;
import com.hr.employee_management.Dto.report.DepartmentHeadcountReport;
import com.hr.employee_management.Dto.report.EmployeeSummaryReport;
import com.hr.employee_management.Service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
@Tag(name = "Reports & Analytics", description = "High-level managerial reports for headcount, attendance distribution, and payroll costs")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/employees/summary")
    @Operation(summary = "Get employee workforce summary", description = "Aggregated count of active, inactive, terminated, and on-leave employees across departments.")
    public ResponseEntity<EmployeeSummaryReport> getEmployeeSummary() {
        return ResponseEntity.ok(reportService.getEmployeeSummary());
    }

    @GetMapping("/attendance/summary")
    @Operation(summary = "Get monthly attendance report summary", description = "Aggregated monthly attendance metrics including presence, absence, and total overtime hours.")
    public ResponseEntity<AttendanceSummaryResponse> getAttendanceSummary(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(reportService.getAttendanceReportSummary(month, year));
    }

    @GetMapping("/payroll/summary")
    @Operation(summary = "Get monthly payroll report summary", description = "Aggregated financial breakdown of gross salaries, net payouts, tax liabilities, and deductions.")
    public ResponseEntity<PayrollSummaryResponse> getPayrollSummary(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(reportService.getPayrollReportSummary(month, year));
    }

    @GetMapping("/departments/headcount")
    @Operation(summary = "Get department headcount report", description = "Department-by-department employee count, total basic salary expenditure, and average salary.")
    public ResponseEntity<List<DepartmentHeadcountReport>> getDepartmentHeadcount() {
        return ResponseEntity.ok(reportService.getDepartmentHeadcountReport());
    }
}
