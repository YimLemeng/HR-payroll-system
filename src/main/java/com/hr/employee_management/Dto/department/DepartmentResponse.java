package com.hr.employee_management.Dto.department;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String departmentName;
    private String description;
    private long employeeCount;
    private Instant createdAt;
    private Instant updatedAt;
}
