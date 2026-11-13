package com.hr.employee_management.Repository;

import com.hr.employee_management.Entity.Employee;
import com.hr.employee_management.enums.EmploymentStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByEmail(String email);
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmployeeCodeAndIdNot(String employeeCode, Long id);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByDepartmentId(Long departmentId);
    long countByDepartmentId(Long departmentId);
    long countByEmploymentStatus(EmploymentStatus status);
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);
    Page<Employee> findByEmploymentStatus(EmploymentStatus status, Pageable pageable);
    Page<Employee> findByDepartmentIdAndEmploymentStatus(Long departmentId, EmploymentStatus status, Pageable pageable);

    @Query("select e from Employee e where " +
            "lower(e.firstName) like lower(concat('%', :kw, '%')) or " +
            "lower(e.lastName) like lower(concat('%', :kw, '%')) or " +
            "lower(e.employeeCode) like lower(concat('%', :kw, '%')) or " +
            "lower(e.email) like lower(concat('%', :kw, '%')) or " +
            "lower(e.jobTitle) like lower(concat('%', :kw, '%'))")
    Page<Employee> searchEmployees(@Param("kw") String keyword, Pageable pageable);
    List<Employee> findByEmploymentStatus(EmploymentStatus status);
    List<Employee> findByDepartmentIdAndEmploymentStatus(Long departmentId, EmploymentStatus status);
}
