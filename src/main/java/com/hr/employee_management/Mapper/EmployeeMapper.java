package com.hr.employee_management.Mapper;

import com.hr.employee_management.Dto.employee.EmployeeRequest;
import com.hr.employee_management.Dto.employee.EmployeeResponse;
import com.hr.employee_management.Entity.Department;
import com.hr.employee_management.Entity.Employee;
import com.hr.employee_management.enums.EmploymentStatus;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {
    public Employee toEntity(EmployeeRequest request, Department department) {
        if (request == null) return null;
        return Employee.builder()
                .employeeCode(request.getEmployeeCode())
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .email(request.getEmail().trim())
                .phone(request.getPhone().trim())
                .address(request.getAddress())
                .hireDate(request.getHireDate())
                .jobTitle(request.getJobTitle().trim())
                .department(department)
                .basicSalary(request.getBasicSalary())
                .employmentStatus(request.getEmploymentStatus() != null ? request.getEmploymentStatus() : EmploymentStatus.ACTIVE)
                .build();
    }

    public EmployeeResponse toResponse(Employee emp) {
        if (emp == null) return null;
        return EmployeeResponse.builder()
                .id(emp.getId())
                .employeeCode(emp.getEmployeeCode())
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .fullName(emp.getFirstName() + " " + emp.getLastName())
                .gender(emp.getGender())
                .dateOfBirth(emp.getDateOfBirth())
                .email(emp.getEmail())
                .phone(emp.getPhone())
                .address(emp.getAddress())
                .hireDate(emp.getHireDate())
                .jobTitle(emp.getJobTitle())
                .departmentId(emp.getDepartment() != null ? emp.getDepartment().getId() : null)
                .departmentName(emp.getDepartment() != null ? emp.getDepartment().getDepartmentName() : null)
                .basicSalary(emp.getBasicSalary())
                .employmentStatus(emp.getEmploymentStatus())
                .createdAt(emp.getCreatedAt())
                .updatedAt(emp.getUpdatedAt())
                .build();
    }
}
