package com.hr.employee_management.Service.impl;

import com.hr.employee_management.Dto.employee.EmployeeRequest;
import com.hr.employee_management.Dto.employee.EmployeeResponse;
import com.hr.employee_management.Entity.Department;
import com.hr.employee_management.Entity.Employee;
import com.hr.employee_management.Exception.BusinessRuleException;
import com.hr.employee_management.Exception.DuplicateResourceException;
import com.hr.employee_management.Exception.ResourceNotFoundException;
import com.hr.employee_management.Mapper.EmployeeMapper;
import com.hr.employee_management.Repository.DepartmentRepository;
import com.hr.employee_management.Repository.EmployeeRepository;
import com.hr.employee_management.Service.EmployeeService;
import com.hr.employee_management.enums.EmploymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    @CacheEvict(value = "reports", allEntries = true)
    public EmployeeResponse createEmployee(EmployeeRequest request){
        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())){
            throw new DuplicateResourceException("Employee code '" + request.getEmployeeCode() + "' is already assigned");
        }
        if (employeeRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("Employee email '" + request.getEmail() + "' is already registered");
        }
        validateSalary(request.getBasicSalary());
        Department department = findDepartmentIfExists(request.getDepartmentId());

        Employee employee = employeeMapper.toEntity(request, department);
        Employee saved = employeeRepository.save(employee);
        log.info("Employee created with code {} and ID {}", request.getEmployeeCode(), saved.getId());
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAllEmployees(Long departmentId, EmploymentStatus status, Pageable pageable){
        if (departmentId != null && status != null){
            return employeeRepository.findByDepartmentIdAndEmploymentStatus(departmentId, status, pageable)
                    .map(employeeMapper::toResponse);
        } else if (departmentId != null){
            return employeeRepository.findByDepartmentId(departmentId, pageable).map(employeeMapper::toResponse);
        } else if (status != null){
            return employeeRepository.findByEmploymentStatus(status, pageable).map(employeeMapper::toResponse);
        } else {
            return employeeRepository.findAll(pageable).map(employeeMapper::toResponse);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    @CacheEvict(value = "reports", allEntries = true)
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request){
        Employee emp = employeeRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Employee", "id", id));
        if (employeeRepository.existsByEmployeeCodeAndIdNot(request.getEmployeeCode(), id)){
            throw new DuplicateResourceException("Employee code '" + request.getEmployeeCode() + "' is already assigned to another employee");
        }
        if (employeeRepository.existsByEmailAndIdNot(request.getEmail(), id)){
            throw new DuplicateResourceException("Employee email '" + request.getEmail() + "' is already registered to another employee");
        }
        validateSalary(request.getBasicSalary());
        Department department = findDepartmentIfExists(request.getDepartmentId());

        emp.setEmployeeCode(request.getEmployeeCode().trim().toUpperCase());
        emp.setFirstName(request.getFirstName().trim());
        emp.setLastName(request.getLastName().trim());
        emp.setGender(request.getGender());
        emp.setDateOfBirth(request.getDateOfBirth());
        emp.setEmail(request.getEmail().trim());
        emp.setPhone(request.getPhone().trim());
        emp.setAddress(request.getAddress());
        emp.setHireDate(request.getHireDate());
        emp.setJobTitle(request.getJobTitle().trim());
        emp.setDepartment(department);
        emp.setBasicSalary(request.getBasicSalary());
        if (request.getEmploymentStatus() != null){
            emp.setEmploymentStatus(request.getEmploymentStatus());
        }

        Employee updated = employeeRepository.save(emp);
        log.info("Employee ID {} updated", id);
        return employeeMapper.toResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "reports", allEntries = true)
    public EmployeeResponse updateEmployeeStatus(Long id, EmploymentStatus status){
        Employee emp = employeeRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Employee", "id", id));
        emp.setEmploymentStatus(status);
        Employee updated = employeeRepository.save(emp);
        log.info("Employee ID {} status updated to {}", id, status);
        return employeeMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchEmployees(String keyword, Pageable pageable){
        return employeeRepository.searchEmployees(keyword, pageable).map(employeeMapper::toResponse);
    }

    private void validateSalary(BigDecimal salary) {
        if (salary != null && salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Basic salary cannot be negative");
        }
    }

    private Department findDepartmentIfExists(Long departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId).orElseThrow(() ->
                new ResourceNotFoundException("Department", "id", departmentId));
    }
}
