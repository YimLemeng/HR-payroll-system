package com.hr.employee_management.Repository;

import com.hr.employee_management.Entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDepartmentNameIgnoreCase(String departmentName);
    boolean existsByDepartmentNameIgnoreCase(String departmentName);
    boolean existsByDepartmentNameIgnoreCaseAndIdNot(String departmentName, Long id);

    List<Department> id(Long id);
}
