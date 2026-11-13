package com.hr.employee_management.Dto.employee;

import com.hr.employee_management.enums.EmploymentStatus;
import com.hr.employee_management.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String email;
    private String phone;
    private String address;
    private LocalDate hireDate;
    private String jobTitle;
    private Long departmentId;
    private String departmentName;
    private BigDecimal basicSalary;
    private EmploymentStatus employmentStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
