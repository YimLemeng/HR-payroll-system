package com.hr.employee_management.Dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PayrollSummaryResponse {
    private int month;
    private int year;
    private long totalRecords;
    private BigDecimal totalGrossSalary;
    private BigDecimal totalNetSalary;
    private BigDecimal totalTax;
    private BigDecimal totalDeductions;
    private BigDecimal totalOvertimePay;
    private BigDecimal totalAllowances;
    private BigDecimal totalBonus;
    private Map<String, Long> statusCounts;
}
