package com.hr.employee_management.Service.impl;

import com.hr.employee_management.Dto.department.DepartmentRequest;
import com.hr.employee_management.Dto.department.DepartmentResponse;
import com.hr.employee_management.Entity.Department;
import com.hr.employee_management.Exception.BusinessRuleException;
import com.hr.employee_management.Exception.DuplicateResourceException;
import com.hr.employee_management.Exception.ResourceNotFoundException;
import com.hr.employee_management.Mapper.DepartmentMapper;
import com.hr.employee_management.Repository.DepartmentRepository;
import com.hr.employee_management.Repository.EmployeeRepository;
import com.hr.employee_management.Service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentResponse createDepartment(DepartmentRequest request){
        if (departmentRepository.existsByDepartmentNameIgnoreCase(request.getDepartmentName())){
            throw new DuplicateResourceException("Department with name '" + request.getDepartmentName() + "' already exists");
        }
        Department department = departmentMapper.toEntity(request);
        Department saved = departmentRepository.save(department);
        log.info("Department '{}' created with ID {}", saved.getDepartmentName(), saved.getId());
        return departmentMapper.toResponse(saved, 0);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "departments", key = "'all'")
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream().map(dept -> {
            long count = employeeRepository.countByDepartmentId(dept.getId());
            return departmentMapper.toResponse(dept, count);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "departments", key = "#id")
    public DepartmentResponse getDepartmentById(Long id){
        Department dept = departmentRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Department", "id", id));
        long count = employeeRepository.countByDepartmentId(dept.getId());
        return departmentMapper.toResponse(dept, count);
    }

    @Override
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request){
        Department dept = departmentRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Department", "id", id));
        if (departmentRepository.existsByDepartmentNameIgnoreCaseAndIdNot(request.getDepartmentName(), id)){
            throw new DuplicateResourceException("Department with name '" + request.getDepartmentName() + "' already exists");
        }
        dept.setDepartmentName(request.getDepartmentName());
        dept.setDescription(request.getDescription());
        Department updated = departmentRepository.save(dept);
        long count = employeeRepository.countByDepartmentId(updated.getId());
        log.info("Department ID {} updated", id);
        return departmentMapper.toResponse(updated, count);
    }

    @Override
    @Transactional
    @CacheEvict(value = "departments", allEntries = true)
    public void deleteDepartment(Long id){
        Department department = departmentRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Department", "id", id));
        if (employeeRepository.existsByDepartmentId(id)){
            throw new BusinessRuleException("Cannot delete department '" + department.getDepartmentName() +
                    "' because it currently contains assigned employees. Please reassign them first.");
        }
        departmentRepository.delete(department);
        log.info("Department ID {} deleted", id);
    }
}
