package com.hr.employee_management.Dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentHeadcountReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long departmentId;
    private String departmentName;
    private long headcount;
    private BigDecimal totalBasicSalary;
    private BigDecimal averageBasicSalary;
}
