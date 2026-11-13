package com.hr.employee_management.Service;

import com.hr.employee_management.Dto.attendance.AttendanceSummaryResponse;
import com.hr.employee_management.Dto.payroll.PayrollSummaryResponse;
import com.hr.employee_management.Dto.report.DepartmentHeadcountReport;
import com.hr.employee_management.Dto.report.EmployeeSummaryReport;

import java.util.List;

public interface ReportService {
    EmployeeSummaryReport getEmployeeSummary();
    AttendanceSummaryResponse getAttendanceReportSummary(int month, int year);
    PayrollSummaryResponse getPayrollReportSummary(int month, int year);
    List<DepartmentHeadcountReport> getDepartmentHeadcountReport();
}
