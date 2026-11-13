package com.hr.employee_management.Dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeSummaryReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private long totalEmployees;
    private long activeEmployees;
    private long inactiveEmployees;
    private long terminatedEmployees;
    private long onLeaveEmployees;
    private Map<String, Long> employeesByDepartment;
}
