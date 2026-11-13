package com.hr.employee_management.Mapper;

import com.hr.employee_management.Dto.payroll.PayrollResponse;
import com.hr.employee_management.Entity.Department;
import com.hr.employee_management.Entity.Employee;
import com.hr.employee_management.Entity.Payroll;
import org.springframework.stereotype.Component;

@Component
public class PayrollMapper {
    public PayrollResponse toResponse(Payroll p) {
        if (p == null) return null;

        Employee emp = p.getEmployee();
        String deptName = emp != null && emp.getDepartment() != null ? emp.getDepartment().getDepartmentName() : null;
        return PayrollResponse.builder()
                .id(p.getId())
                .employeeId(emp != null ? emp.getId() : null)
                .employeeCode(emp != null ? emp.getEmployeeCode() : null)
                .employeeName(emp != null ? (emp.getFirstName() + " " + emp.getLastName()) : null)
                .departmentName(deptName)
                .payrollMonth(p.getPayrollMonth())
                .payrollYear(p.getPayrollYear())
                .basicSalary(p.getBasicSalary())
                .allowances(p.getAllowances())
                .overtimePay(p.getOvertimePay())
                .bonus(p.getBonus())
                .deductions(p.getDeductions())
                .tax(p.getTax())
                .grossSalary(p.getGrossSalary())
                .netSalary(p.getNetSalary())
                .status(p.getStatus())
                .calculatedAt(p.getCalculatedAt())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
