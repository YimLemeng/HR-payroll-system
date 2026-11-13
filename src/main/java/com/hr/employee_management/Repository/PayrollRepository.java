package com.hr.employee_management.Repository;

import com.hr.employee_management.Entity.Payroll;
import com.hr.employee_management.enums.PayrollStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByEmployeeIdAndPayrollYearAndPayrollMonth(Long employeeId, int payrollYear, int payrollMonth);
    boolean existsByEmployeeIdAndPayrollYearAndPayrollMonth(Long employeeId, int payrollYear, int payrollMonth);
    Page<Payroll> findByPayrollYearAndPayrollMonth(int payrollYear, int payrollMonth, Pageable pageable);
    Page<Payroll> findByPayrollYearAndPayrollMonthAndStatus(int payrollYear, int payrollMonth, PayrollStatus status, Pageable pageable);
    Page<Payroll> findByEmployeeId(Long employeeId, Pageable pageable);
    List<Payroll> findByPayrollYearAndPayrollMonth(int payrollYear, int payrollMonth);
    long countByPayrollYearAndPayrollMonth(int payrollYear, int payrollMonth);
}
