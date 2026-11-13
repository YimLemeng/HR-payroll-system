package com.hr.employee_management.Mapper;

import com.hr.employee_management.Dto.department.DepartmentRequest;
import com.hr.employee_management.Dto.department.DepartmentResponse;
import com.hr.employee_management.Entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {
    public DepartmentResponse toResponse(Department department, long employeeCount) {
        if (department == null) return null;
        return DepartmentResponse.builder()
                .id(department.getId())
                .departmentName(department.getDepartmentName())
                .description(department.getDescription())
                .employeeCount(employeeCount)
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }

    public Department toEntity(DepartmentRequest request) {
        if (request == null) return null;
        return Department.builder()
                .departmentName(request.getDepartmentName())
                .description(request.getDescription())
                .build();
    }
}
