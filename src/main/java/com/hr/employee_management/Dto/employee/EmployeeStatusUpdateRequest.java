package com.hr.employee_management.Dto.employee;

import com.hr.employee_management.enums.EmploymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeStatusUpdateRequest {

    @NotNull(message = "Employment status is required")
    private EmploymentStatus employmentStatus;
}
