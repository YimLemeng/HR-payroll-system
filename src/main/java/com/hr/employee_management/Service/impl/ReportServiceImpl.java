package com.hr.employee_management.Service.impl;

import com.hr.employee_management.Dto.attendance.AttendanceSummaryResponse;
import com.hr.employee_management.Dto.payroll.PayrollSummaryResponse;
import com.hr.employee_management.Dto.report.DepartmentHeadcountReport;
import com.hr.employee_management.Dto.report.EmployeeSummaryReport;
import com.hr.employee_management.Entity.Department;
import com.hr.employee_management.Entity.Employee;
import com.hr.employee_management.Repository.DepartmentRepository;
import com.hr.employee_management.Repository.EmployeeRepository;
import com.hr.employee_management.Service.AttendanceService;
import com.hr.employee_management.Service.PayrollService;
import com.hr.employee_management.Service.ReportService;
import com.hr.employee_management.enums.EmploymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceService attendanceService;
    private final PayrollService payrollService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "reports", key = "'employee_summary'")
    public EmployeeSummaryReport getEmployeeSummary(){
        List<Employee> allEmployees = employeeRepository.findAll();
        long active = 0;
        long inactive = 0;
        long terminated = 0;
        long onLeave = 0;
        Map<String, Long> byDepartment = new HashMap<>();

        for (Employee emp : allEmployees) {
            if (emp.getEmploymentStatus() == EmploymentStatus.ACTIVE) active++;
            else if (emp.getEmploymentStatus() == EmploymentStatus.INACTIVE) inactive++;
            else if (emp.getEmploymentStatus() == EmploymentStatus.TERMINATED) terminated++;
            else if (emp.getEmploymentStatus() == EmploymentStatus.ON_LEAVE) onLeave++;

            String deptName = emp.getDepartment() != null ? emp.getDepartment().getDepartmentName() : "Unassigned";
            byDepartment.put(deptName, byDepartment.getOrDefault(deptName, 0L) + 1);
        }

        return EmployeeSummaryReport.builder()
                .totalEmployees(allEmployees.size())
                .activeEmployees(active)
                .inactiveEmployees(inactive)
                .terminatedEmployees(terminated)
                .onLeaveEmployees(onLeave)
                .employeesByDepartment(byDepartment)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getAttendanceReportSummary(int month, int year){
        return attendanceService.getAttendanceSummary(month, year);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollSummaryResponse getPayrollReportSummary(int month, int year){
        return payrollService.getPayrollSummary(month, year);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "reports", key = "'dept_headcount'")
    public List<DepartmentHeadcountReport> getDepartmentHeadcountReport(){
        List<Department> departments = departmentRepository.findAll();
        List<Employee> allEmployees = employeeRepository.findAll();
        Map<Long, List<Employee>> deptEmployees = new HashMap<>();

        for (Employee emp : allEmployees) {
            if (emp.getDepartment() != null){
                deptEmployees.computeIfAbsent(emp.getDepartment().getId(), k -> new ArrayList<>()).add(emp);
            }
        }

        List<DepartmentHeadcountReport> reports = new ArrayList<>();
        for (Department dept : departments) {
            List<Employee> employee = deptEmployees.getOrDefault(dept.getId(), Collections.emptyList());
            long count = employee.size();

            BigDecimal totalSalary = employee.stream()
                    .map(Employee::getBasicSalary)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal avgSalary = count > 0
                    ? totalSalary.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            reports.add(DepartmentHeadcountReport.builder()
                    .departmentId(dept.getId())
                    .departmentName(dept.getDepartmentName())
                    .headcount(count)
                    .totalBasicSalary(totalSalary)
                    .averageBasicSalary(avgSalary)
                    .build());
        }
        return reports;
    }
}
