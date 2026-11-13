package com.hr.employee_management.Dto.payroll;

import com.hr.employee_management.enums.PayrollStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayrollResponse {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private int payrollMonth;
    private int payrollYear;
    private BigDecimal basicSalary;
    private BigDecimal allowances;
    private BigDecimal overtimePay;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private BigDecimal tax;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private PayrollStatus status;
    private Instant calculatedAt;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;
}
