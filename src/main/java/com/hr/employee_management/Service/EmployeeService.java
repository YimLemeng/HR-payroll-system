package com.hr.employee_management.Service;

import com.hr.employee_management.Dto.employee.EmployeeRequest;
import com.hr.employee_management.Dto.employee.EmployeeResponse;
import com.hr.employee_management.enums.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeRequest request);
    Page<EmployeeResponse> getAllEmployees(Long departmentId, EmploymentStatus status, Pageable pageable);
    EmployeeResponse getEmployeeById(Long id);
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
    EmployeeResponse updateEmployeeStatus(Long id, EmploymentStatus status);
    Page<EmployeeResponse> searchEmployees(String keyword, Pageable pageable);
}
